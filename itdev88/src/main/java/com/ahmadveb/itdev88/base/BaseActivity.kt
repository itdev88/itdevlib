package com.ahmadveb.itdev88.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.ahmadveb.itdev88.R
import com.ahmadveb.itdev88.ui.ext.toast
import com.ahmadveb.itdev88.utils.Grather

abstract class BaseActivity<P : BasePresenter<V>, V : BaseViewImpl> : AppCompatActivity() {

    private lateinit var presenter: P
    private lateinit var progressDialog: AlertDialog

    fun setPresenter() {
        presenter = createPresenter()
    }

    fun getPresenter(): P? {
        return presenter
    }

    abstract fun createPresenter(): P

    abstract fun createLayout(): Int

    abstract fun startingUpActivity(savedInstanceState: Bundle?)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set content view, create layout implementation
        setContentView(createLayout())
        setupProgressDialog()
        if (this is BaseViewImpl) {
            setPresenter()
            if (getPresenter() != null) {
                getPresenter()?.attachView(this as V)
            }
        }

        // init action
        startingUpActivity(savedInstanceState)
    }

    override fun onDestroy() {
        hideLoadingDialog()
        super.onDestroy()
        if (getPresenter() != null) {
            getPresenter()?.detachView()
        }
    }

    private fun setupProgressDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress_dialog)
        progressDialog = builder.create()
        progressDialog.setCancelable(false)
    }

    fun showLoadingDialog() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    fun hideLoadingDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    fun showToast(message: String) {
        toast(this, message)
    }

    fun showToast(resInt: Int) {
        showToast(getString(resInt))
    }

    fun hideKeyboard() {
        Grather.hideKeyboard(this)
    }


}