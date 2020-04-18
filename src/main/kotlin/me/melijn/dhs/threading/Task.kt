package me.melijn.dhs.threading

import kotlinx.coroutines.runBlocking


class Task(private val func: suspend () -> Unit) : Runnable {


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