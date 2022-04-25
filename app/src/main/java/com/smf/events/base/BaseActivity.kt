package com.smf.events.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<T : ViewDataBinding, out V : BaseViewModel> : AppCompatActivity() {

    protected var mViewDataBinding: T? = null
    private var mViewModel: V? = null

    abstract fun getContentView(): Int

    abstract fun getViewModel(): V?

    fun getViewDataBinding(): T? {
        return mViewDataBinding
    }

    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
    }

    private fun performDataBinding() {
        // 2458
        getViewModel()?.let { viewModel ->
            mViewDataBinding = DataBindingUtil.setContentView(this, getContentView())
            mViewModel = ViewModelProvider(this).get(viewModel::class.java)
            mViewDataBinding?.setVariable(getBindingVariable(), mViewModel)
            mViewDataBinding?.executePendingBindings()
            mViewDataBinding?.lifecycleOwner = this@BaseActivity
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this.applicationContext, msg, Toast.LENGTH_LONG).show()
    }
}