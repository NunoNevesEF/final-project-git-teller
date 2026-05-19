package pt.isel.service.git

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.concurrent.TimeUnit

data class InstallationAccessToken(
    val token: String,
    val expiresAt: Instant
)

@Service
class GitHubAppService(
    @Value("\${app.github.app-id}") private val appId: Long,
    @Value("\${app.github.private-key-file}") private val privateKeyFile: String,
    @Value("\${app.github.token-cache-minutes:55}") private val tokenCacheMinutes: Long
) {
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    private val cache: Cache<Long, InstallationAccessToken> = Caffeine.newBuilder()
        .expireAfterWrite(tokenCacheMinutes, TimeUnit.MINUTES)
        .build()

    private val privateKey: RSAPrivateKey by lazy { loadPrivateKey() }

    fun invalidateInstallationToken(installationId: Long) {
        cache.invalidate(installationId)
    }

    fun getInstallationToken(installationId: Long): String {
        val cached = cache.getIfPresent(installationId)
        if (cached != null && Instant.now().isBefore(cached.expiresAt.minusSeconds(30))) {
            return cached.token
        }

        val jwt = generateAppJwt()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $jwt")
            set("Accept", "application/vnd.github+json")
        }

        val response = restTemplate.exchange(
            "https://api.github.com/app/installations/$installationId/access_tokens",
            HttpMethod.POST,
            HttpEntity<Unit>(headers),
            String::class.java
        )

        val body = response.body ?: error("GitHub returned an empty installation-token response")
        val json = objectMapper.readTree(body)
        val token = json["token"]?.asText() ?: error("GitHub did not return an installation token")
        val expiresAt = Instant.parse(json["expires_at"]?.asText() ?: error("GitHub did not return expires_at"))

        cache.put(installationId, InstallationAccessToken(token, expiresAt))
        return token
    }

    private fun generateAppJwt(): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .issuer(appId.toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(9 * 60)))
            .build()

        val signed = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).build(),
            claims
        )

        signed.sign(RSASSASigner(privateKey))
        return signed.serialize()
    }

    private fun loadPrivateKey(): RSAPrivateKey {
        val path = Paths.get(privateKeyFile).toAbsolutePath().normalize()
        val pem = Files.readString(path)

        val parser = PEMParser(StringReader(pem))
        val parsed = parser.readObject()
        parser.close()

        val converter = JcaPEMKeyConverter()

        val privateKey: PrivateKey = when (parsed) {
            is PrivateKeyInfo -> converter.getPrivateKey(parsed)
            is PEMKeyPair -> converter.getKeyPair(parsed).private
            else -> error("Unsupported private key format in $path")
        }

        return privateKey as RSAPrivateKey
    }
}