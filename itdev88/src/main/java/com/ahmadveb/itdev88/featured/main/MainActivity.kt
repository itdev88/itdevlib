package com.ahmadveb.itdev88.featured.main

import android.os.Bundle
import android.view.MenuItem
import com.ahmadveb.itdev88.R
import com.ahmadveb.itdev88.base.BaseActivity

class MainActivity : BaseActivity<MainPresenter, MainContract.View>(), MainContract.View {
    override fun createPresenter(): MainPresenter {
        return MainPresenter(this, this)
    }

    override fun createLayout(): Int {
        return R.layout.activity_login
    }

    override fun startingUpActivity(savedInstanceState: Bundle?) {
        renderView()
        getPresenter()?.onViewCreated()
    }

    private fun renderView(){


    }

    override fun onDestroy() {
        super.onDestroy()
        getPresenter()?.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Login"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
        }

    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun restartMainActivity(menu: Int) {
    }

    override fun restartMainActivity(menu: Int, position: Int) {
    }


}
