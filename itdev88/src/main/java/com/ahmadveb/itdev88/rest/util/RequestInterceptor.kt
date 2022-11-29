package com.ahmadveb.itdev88.rest.util

import com.ahmadveb.itdev88.BuildConfig
import com.ahmadveb.itdev88.MyApplication
import com.ahmadveb.itdev88.utils.Helper
import com.ahmadveb.itdev88.utils.Signature
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (!Helper.isNetworkAvailable()) {
            val maxStale = 60 * 60 * 24 * 7
            request = request
                .newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }
        else{
            val version = "B2"
            val timestamp = Signature.generateTimestamp()
            val key = "jefjigjgrghhh"
            val deviceToken: String? = "kgfg"

            request = request.newBuilder()
                .addHeader("X-VIBRI-TIMESTAMP",timestamp)
                .addHeader("X-VIBRI-VERSION","$version")
                .addHeader("X-VIBRI-KEY",key)
                .addHeader("X-VIBRI-DeviceToken", deviceToken)
                .build()

        }
        return chain.proceed(request)

    }

}
