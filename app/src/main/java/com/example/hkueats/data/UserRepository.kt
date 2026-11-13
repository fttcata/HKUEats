package com.example.hkueats.data

import java.util.Locale

interface UserRepository {
    fun validateHkuEmail(email: String): UserLoginResult
}

enum class UserLoginResult {
    Success,
    InvalidEmail,
}

class HkuEmailUserRepository : UserRepository {
    override fun validateHkuEmail(email: String): UserLoginResult {
        val emailDomain = email.substringAfterLast('@').lowercase(Locale.ROOT)
        return if (emailDomain == "connect.hku.hk" || emailDomain == "hku.hk") {
            UserLoginResult.Success
        } else {
            UserLoginResult.InvalidEmail
        }
    }
}
