package com.innovativetools.firebase.chat.activities.async

import java.util.concurrent.Callable

interface CustomCallable<R> : Callable<R> {
    fun setDataAfterLoading(result: R)
    fun setUiForLoading()
}