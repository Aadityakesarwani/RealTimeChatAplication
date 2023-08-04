package com.innovativetools.firebase.chat.activities.async

import android.os.Handler
import android.os.Looper
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class TaskRunner {
    private val handler = Handler(Looper.getMainLooper())
    private val executor: Executor = Executors.newCachedThreadPool()
    fun <R> executeAsync(callable: CustomCallable<R?>) {
        try {
            callable.setUiForLoading()
            executor.execute(RunnableTask(handler, callable))
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    class RunnableTask<R>(private val handler: Handler, callable: CustomCallable<R?>) : Runnable {
        private val callable: CustomCallable<R?>

        init {
            this.callable = callable
        }

        override fun run() {
            try {
                val result = callable.call()
                handler.post(RunnableTaskForHandler<R?>(callable, result))
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }
    }

    class RunnableTaskForHandler<R>(
        private val callable: CustomCallable<R>,
        private val result: R
    ) : Runnable {
        override fun run() {
            callable.setDataAfterLoading(result)
        }
    }
}