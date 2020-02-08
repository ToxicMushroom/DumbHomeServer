package me.melijn.dhs.threading

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TaskManager {

    private val threadFactory = { name: String -> ThreadFactoryBuilder().setNameFormat("[$name-Pool-%d] ").build() }
    private val executorService: ExecutorService = Executors.newCachedThreadPool(threadFactory.invoke("Task"))
    private val dispatcher = executorService.asCoroutineDispatcher()

    fun async(block: suspend CoroutineScope.() -> Unit) = CoroutineScope(dispatcher).launch {
        val task = Task(Runnable {
            CoroutineScope(dispatcher).launch {
                block.invoke(this)
            }
        })
        task.run()
    }
}