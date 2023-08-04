package com.innovativetools.firebase.chat.activities.async

interface iOnDataFetched {
    fun showProgressBar(progress: Int)
    fun hideProgressBar()
    fun setDataInPageWithResult(result: Any?)
}