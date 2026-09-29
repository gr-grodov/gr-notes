package auth.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PKCEUtils {

    private val random = new SecureRandom()

    def generateVerifier(): String = {
        val bytes = new Array[Byte](32)
        random.nextBytes(bytes)

        Base64.getUrlEncoder
            .withoutPadding()
            .encodeToString(bytes)
    }

    def challenge(verifier: String): String = {
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(verifier.getBytes("US-ASCII"))

        Base64.getUrlEncoder
            .withoutPadding()
            .encodeToString(digest)
    }

    def randomToken(bytes: Int = 32): String = {
        val value = new Array[Byte](bytes)
        random.nextBytes(value)

        Base64.getUrlEncoder
            .withoutPadding()
            .encodeToString(value)
    }
}
