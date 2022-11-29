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

    fun getProfile(key:String): Observable<List<User>> {
        return restInterface.getProfile(key)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getToken(
        req: RequestLogout,
    ): Observable<User> {
        return restInterface.getToken(req)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUser(key:String,url:String): Observable<List<User>> {
        return restInterface.getUser(key,url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateProfile(key:String,name:String,email:String,telpon:String,alamat:String,img:String?): Observable<Message> {
        return restInterface.updateProfile(
            Helper.createPartFromString(key),
            Helper.createPartFromString(name),
            Helper.createPartFromString(email),
            Helper.createPartFromString(telpon),
            Helper.createPartFromString(alamat),
            Helper.createPartFromFile(img,"img"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun changePassword(key:String,lama:String,baru:String): Observable<Message> {
        return restInterface.changePassword(key,lama,baru)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun login(user:String,password:String): Observable<List<Login>> {
        return restInterface.login(user,password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun presence(key:String,latitude:String,longitude:String,img:String?): Observable<List<User>> {
        return restInterface.presence(
            Helper.createPartFromString(key),
            Helper.createPartFromString(latitude),
            Helper.createPartFromString(longitude),
            Helper.createPartFromFile(img,"img"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun register(toko:String,currency:String,name:String,email:String,telpon:String,password:String,alamat:String,referal:String,typestore:String,decimal:String): Observable<Message> {
        return restInterface.register(toko,currency,name,email,telpon,password,alamat,referal,typestore,decimal)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun forgotPassword(email:String,phone:String): Observable<Message> {
        return restInterface.forgotPassword(email,phone)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}