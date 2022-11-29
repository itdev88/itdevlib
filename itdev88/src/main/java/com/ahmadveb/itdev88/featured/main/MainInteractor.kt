package com.ahmadveb.itdev88.featured.main

import android.content.Context
import androidx.annotation.NonNull
import com.ahmadveb.itdev88.models.user.User
import com.ahmadveb.itdev88.models.user.UserRestModel
import com.ahmadveb.itdev88.rest.entity.RestException
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver

class MainInteractor(var output: MainContract.InteractorOutput?) : MainContract.Interactor {
    private var disposable = CompositeDisposable()

    override fun onDestroy() {
        disposable.clear()
    }

    override fun getUser(context: Context, restModel: UserRestModel) {
        val key = ""
        val url = ""
        disposable.add(restModel.getUser(key,url).subscribeWith(object : DisposableObserver<List<User>>() {

            override fun onNext(@NonNull response: List<User>) {

            }
            override fun onError(@NonNull e: Throwable) {
                e.printStackTrace()
                var errorCode = 999
                val errorMessage: String
                if (e is RestException) {
                    errorCode = e.errorCode
                    errorMessage = e.message ?: "There is an error"
                }
                else{
                    errorMessage = e.message.toString()
                }
                output?.onFailedAPI(errorCode,errorMessage)
            }

            override fun onComplete() {

            }
        }))
    }

}