package com.vertice.backend.identity

import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Types

interface PasswordHasher {
    fun hash(password: CharArray): String
    fun verify(encodedHash: String, password: CharArray): Boolean
}

class Argon2idPasswordHasher : PasswordHasher {
    override fun hash(password: CharArray): String =
        Argon2Factory.create(Argon2Types.ARGON2id).hash(
            3,
            65_536,
            1,
            password,
        )

    override fun verify(encodedHash: String, password: CharArray): Boolean =
        runCatching {
            Argon2Factory.create(Argon2Types.ARGON2id).verify(encodedHash, password)
        }.getOrDefault(false)
}
