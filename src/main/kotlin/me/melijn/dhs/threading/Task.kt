package me.melijn.dhs.threading

import kotlinx.coroutines.runBlocking


class Task(private val func: suspend () -> Unit) : KTRunnable {

    override suspend fun run() {
        try {
            func()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}


class RunnableTask(private val func: suspend () -> Unit) : Runnable {

    override fun run() {
        runBlocking {
            try {
                func()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

@FunctionalInterface
interface KTRunnable {
    suspend fun run()
}