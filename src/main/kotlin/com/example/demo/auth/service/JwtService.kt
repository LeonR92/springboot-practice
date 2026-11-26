package com.example.demo.auth.service

import com.example.demo.user.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    @Value("\${jwt.issuer}") private val issuer: String,
    @Value("\${jwt.expiration-seconds}") private val expirationSeconds: Long,
) {
    fun generateToken(user: User): String {
        val now = Instant.now()

        val claims =
            JwtClaimsSet
                .builder()
                .issuer(issuer)
                .claim("role", user.role.name)
                .claim("email", user.email)
                .claim("name", user.name)
                .subject(requireNotNull(user.id).toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}
