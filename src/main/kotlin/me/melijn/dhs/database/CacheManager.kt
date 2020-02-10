package me.melijn.dhs.database

import kotlinx.coroutines.runBlocking
import me.melijn.dhs.objects.components.SwitchComponent
import me.melijn.dhs.objects.components.User

class CacheManager(private val dbManager: DBManager) {

    private val users = mutableListOf<User>()
    val switchComponentList = mutableListOf<SwitchComponent>()

    suspend fun refreshCaches() {
        switchComponentList.clear()
        switchComponentList.addAll(dbManager.switchWrapper.getSwitches())
        dbManager.userWrapper.getUserTokens().forEach { (name: String, token: String) ->
            val user = User(name, token)
            user.initOverrides(dbManager)
            users.add(user)
        }
    }

    fun getUsernameFromToken(token: String): String? {
        return users.stream()
            .filter { user: User -> user.token == token }
            .findAny()
            .map<String>(User::name)
            .orElse(null)
    }

    fun getSwitchComponentById(id: Int): SwitchComponent? {
        return switchComponentList.stream()
            .filter { switchComponent: SwitchComponent -> switchComponent.id == id }
            .findAny()
            .orElse(null)
    }

    fun getUserByName(username: String): User? {
        return users.stream()
            .filter { user: User -> user.name == username }
            .findAny()
            .orElse(null)
    }

    fun getSwitchComponentByCode(code: Int): SwitchComponent? {
        return switchComponentList
            .firstOrNull { it.onCode == code || it.offCode == code }
    }

    init {
        runBlocking {
            refreshCaches()
        }
    }
}