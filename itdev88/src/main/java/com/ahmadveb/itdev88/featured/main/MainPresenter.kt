package com.ahmadveb.itdev88.featured.main

import android.content.Context
import com.ahmadveb.itdev88.base.BasePresenter
import com.ahmadveb.itdev88.models.user.UserRestModel

class MainPresenter(val context: Context, val view: MainContract.View) :
    BasePresenter<MainContract.View>(),
    MainContract.Presenter, MainContract.InteractorOutput {
    private var userrestModel = UserRestModel(context)

    private var interactor: MainInteractor = MainInteractor(this)

    override fun onViewCreated() {
        getUser()
    }

    override fun getUser() {
        interactor.getUser(context,userrestModel)
    }

    override fun onFailedAPI(code: Int, msg: String) {
        view.hideLoadingDialog()
        view.showToast(msg)
    }

    override fun onDestroy() {
        interactor.onDestroy()
    }


}