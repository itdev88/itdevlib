package com.ahmadveb.itdev88.callback

/**
 * @author ahmad_itdev88
 */
fun interface ChoosePhotoCallback<T> {
    fun onChoose(photo: T?)
}