package pt.isel.security.configs

import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID

@Configuration
class JwtConfig {
    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>) = NimbusJwtEncoder(jwkSource)

    @Bean
    fun jwtDecoder(): NimbusJwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKey().toRSAPublicKey()).build()

    @Bean
    fun jwkSource(): JWKSource<SecurityContext?> {
        val jwkSet = JWKSet(rsaKey())
        return ((JWKSource { jwkSelector: JWKSelector?, securityContext: SecurityContext? -> jwkSelector!!.select(jwkSet) }))
    }

    @Bean
    fun rsaKey(): RSAKey {
        val keyPair = keyPair();
        return RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
    }

    @Bean
    fun keyPair(): KeyPair {
        try{
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to generate RSA Key Pair", e)
        }
    }
}