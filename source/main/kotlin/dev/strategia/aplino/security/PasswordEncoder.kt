package dev.strategia.aplino.security

/**
 * Common interface for password encoding. The passwords are represented as CharArray, because String
 * objects could be interned by the JVM (creating a copy which remains in memory). Also, accidentally logging
 * a password would produce junk string for the CharArray object.
 */
interface PasswordEncoder {

    /**
     * Encode a password.
     * @param password The raw password.
     * @param salt Password salt. If not provided will use random.
     * @return Encoded password in Base64 format.
     */
    fun encode(password: CharArray, salt: ByteArray? = null): String

    /**
     * Check if the provided password matches the previously encoded password.
     * @param password The password to be checked.
     * @param encodedPassword Previously encoded and salted password.
     * @return True if the passwords match.
     */
    fun matches(password: CharArray, encodedPassword: String): Boolean
}
