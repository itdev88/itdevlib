package com.ahmadveb.itdev88.models.user

import com.ahmadveb.itdev88.models.Message
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserRestInterface {

    @GET("profile.php")
    fun getUser(
        @Query("key") key:String,
        @Query("url") url:String): Observable<List<User>>


}