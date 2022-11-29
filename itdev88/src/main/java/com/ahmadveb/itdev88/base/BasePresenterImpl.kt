package com.ahmadveb.itdev88.base

interface BasePresenterImpl<V : BaseViewImpl> {

    fun attachView(view: V)

    fun detachView()
}