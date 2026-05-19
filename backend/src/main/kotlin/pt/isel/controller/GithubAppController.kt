package pt.isel.controller

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.account.AccountType
import pt.isel.domain.account.OAuthLinkedAccount
import pt.isel.repository.ILinkedAccountRepository
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.git.GitHubAppService
import pt.isel.service.git.GitHubInstallationCandidate
import pt.isel.service.git.GitHubInstallationLinkService
import pt.isel.utils.success
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class LinkInstallationRequest(
    val installationId: Long
)

data class GitHubInstallationsStateResponse(
    val installUrl: String,
    val discoveredInstallations: List<GitHubInstallationCandidate>,
    val linkedInstallationIds: List<Long>,
    val message: String? = null
)

@RestController
@RequestMapping("/api/github/app")
@CrossOrigin(origins = ["http://localhost:8081"])
class GitHubAppController(
    private val linkedAccountRepo: ILinkedAccountRepository,
    private val installationLinkService: GitHubInstallationLinkService,
    private val gitHubAppService: GitHubAppService,
    @Value("\${app.github.install-url}") private val installUrl: String,
    @Value("\${app.github.webhook-secret}") private val webhookSecret: String
) {

    private val logger = LoggerFactory.getLogger(GitHubAppController::class.java)
    private val objectMapper = ObjectMapper()

    @GetMapping("/installations")
    fun getInstallations(
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<GitHubInstallationsStateResponse> {
        val userId = principal?.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                GitHubInstallationsStateResponse(
                    installUrl = installUrl,
                    discoveredInstallations = emptyList(),
                    linkedInstallationIds = emptyList(),
                    message = "User not authenticated"
                )
            )

        val discovered = installationLinkService.discoverForUser(userId)
        val linkedIds = installationLinkService.installationsForUser(userId).map { it.installationId }

        val message = if (discovered.isEmpty()) {
            "No GitHub App installation found. Install the app and choose repositories."
        } else {
            null
        }

        return ResponseEntity.ok(
            GitHubInstallationsStateResponse(
                installUrl = installUrl,
                discoveredInstallations = discovered,
                linkedInstallationIds = linkedIds,
                message = message
            )
        )
    }

    @PostMapping("/installations/link")
    fun linkInstallation(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestBody request: LinkInstallationRequest
    ): ResponseEntity<Any> {
        val userId = principal?.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "User not authenticated"))

        val discovered = installationLinkService.discoverForUser(userId)
        val candidate = discovered.firstOrNull { it.installationId == request.installationId }
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to "Installation not found for this user")
            )

        val saved = installationLinkService.linkUserInstallation(
            userId = userId,
            installationId = candidate.installationId,
            accountLogin = candidate.accountLogin
        )

        return ResponseEntity.ok(
            mapOf(
                "linkedInstallationId" to saved.installationId,
                "accountLogin" to saved.accountLogin
            )
        )
    }

    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestHeader("X-Hub-Signature-256", required = false) signatureHeader: String?,
        @RequestHeader("X-GitHub-Event", required = false) event: String?,
        request: HttpServletRequest
    ): ResponseEntity<String> {

        logger.info("Received webhook event: X-GitHub-Event=$event, X-Hub-Signature-256=$signatureHeader")


        val payload = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)

        if (!verifySignature(signatureHeader, payload)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature")
        }

        val json = objectMapper.readTree(payload)

        when (event) {
            "installation" -> {
                val action = json["action"]?.asText()
                val installationId = json["installation"]?.get("id")?.asLong()

                if (installationId != null) {
                    when (action) {
                        "created" -> {
                            val accountLogin = json["installation"]?.get("account")?.get("id")?.asText()
                            installationLinkService.linkByAccountLogin(installationId, accountLogin)
                        }
                        "deleted", "suspend" -> {
                            installationLinkService.unlinkInstallation(installationId)
                            gitHubAppService.invalidateInstallationToken(installationId)
                        }
                    }
                }
            }

            "installation_repositories" -> {
                val installationId = json["installation"]?.get("id")?.asLong()
                if (installationId != null) {
                    gitHubAppService.invalidateInstallationToken(installationId)
                }
            }
        }

        return ResponseEntity.ok("ok")
    }

    private fun verifySignature(signatureHeader: String?, payload: String): Boolean {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false

        return try {
            val expected = signatureHeader.removePrefix("sha256=")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(webhookSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            val computed = mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
            val computedHex = computed.joinToString("") { "%02x".format(it) }
            computedHex == expected
        } catch (_: Exception) {
            false
        }
    }
}