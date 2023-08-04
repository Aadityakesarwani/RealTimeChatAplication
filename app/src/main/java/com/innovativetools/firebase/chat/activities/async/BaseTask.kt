package com.innovativetools.firebase.chat.activities.async

import com.innovativetools.firebase.chat.activities.async.CustomCallable
import java.lang.Exception
import kotlin.Throws

abstract class BaseTask<R> : CustomCallable<R> {
    override fun setUiForLoading() {}
    override fun setDataAfterLoading(result: R) {}
    @Throws(Exception::class)
    override fun call(): R? {
        return null
    }
}