package me.melijn.dhs.database.user

class UserWrapper(val userDao: UserDao) {

    suspend fun getUserTokens(): Map<String, String> {
        return userDao.getUserTokens()
    }
}