package com.ahmadveb.itdev88.rest.callback

interface ResponseCallback<T> {

    fun onRequestSuccess(data: T)
    fun onRequestFailed(errorCode: Int, message: String?)
}