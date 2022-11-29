package com.ahmadveb.itdev88.featured.main

import android.content.Context
import com.ahmadveb.itdev88.base.BaseInteractorImpl
import com.ahmadveb.itdev88.base.BaseInteractorOutputImpl
import com.ahmadveb.itdev88.base.BasePresenterImpl
import com.ahmadveb.itdev88.base.BaseViewImpl
import com.ahmadveb.itdev88.models.user.UserRestModel

interface MainContract {

    interface View : BaseViewImpl {
    }

    interface Presenter : BasePresenterImpl<View> {
        fun onDestroy()
        fun onViewCreated()
        fun getUser()
    }

    interface Interactor : BaseInteractorImpl {
        fun onDestroy()
        fun getUser(context: Context, restModel: UserRestModel)
    }

    interface InteractorOutput : BaseInteractorOutputImpl {
        fun onFailedAPI(code:Int,msg:String)
    }

}