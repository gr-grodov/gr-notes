package auth.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PKCEUtils {

    private val random = new SecureRandom()
    private val encoder = Base64.getUrlEncoder.withoutPadding()

    def generateState(): String = randomToken()

    def generateNonce(): String = randomToken()

    def generateVerifier(): String = randomToken()

    def generateChallenge(verifier: String): String = {
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(verifier.getBytes(StandardCharsets.US_ASCII))

        encoder.encodeToString(digest)
    }

    private def randomToken(): String = {
        val bytes = new Array[Byte](32)
        random.nextBytes(bytes)
        encoder.encodeToString(bytes)
    }
}
