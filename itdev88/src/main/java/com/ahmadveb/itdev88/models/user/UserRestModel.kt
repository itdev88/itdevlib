package com.ahmadveb.itdev88.models.user

import android.content.Context
import com.ahmadveb.itdev88.models.Message
import com.ahmadveb.itdev88.rest.RestClient
import com.ahmadveb.itdev88.rest.RestModel
import com.ahmadveb.itdev88.utils.Helper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UserRestModel(context: Context) : RestModel<UserRestInterface>(context) {

    override fun createRestInterface(): UserRestInterface {
        return RestClient.getInstance()!!.createInterface(UserRestInterface::class.java)
    }

    fun getUser(key:String,url:String): Observable<List<User>> {
        return restInterface.getUser(key,url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


}