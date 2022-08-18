package com.smf.events.base

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ConnectionLiveData
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.SnackBar
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent

abstract class BaseActivity<T : ViewDataBinding, out V : BaseViewModel> : AppCompatActivity() {

    protected var mViewDataBinding: T? = null
    private var mViewModel: V? = null
    private lateinit var connectionLiveData: ConnectionLiveData

    abstract fun getContentView(): Int

    abstract fun getViewModel(): V?

    fun getViewDataBinding(): T? {
        return mViewDataBinding
    }

    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        connectionLiveData = ConnectionLiveData(this)
        observerMethod()
    }

    private fun netWorkObserver(){
//        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isNetworkAvailable ->
            when (isNetworkAvailable) {
                true -> {
                    Log.d("TAG", "onResume network observer: act available $isNetworkAvailable")
                    SharedPreference.isInternetConnected = true
                    RxBus.publish(RxEvent.InternetStatus("Available"))
                }
                false -> {
                    Log.d("TAG", "onResume network observer: act not available $isNetworkAvailable")
                    SharedPreference.isInternetConnected = false
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        SharedPreference.isInternetConnected = false
        netWorkObserver()
        Log.d("TAG", "onResume network observer: base act resume called")
    }


     fun observerMethod(){
        Log.d("TAG", "on toast create Base Activity befor")
        getViewModel()?.getToastMessageG?.observe(this,{ toastMessageG ->
            Log.d("TAG", "on toast create Base Activity $toastMessageG")
            SnackBar.showSnakbarTypeOne(mViewDataBinding?.root,
                toastMessageG.msg,
                this,
                toastMessageG.duration)
            //Toast(context).showCustomToast(toastMessageG.msg,requireActivity(),"Toast.LENGTH_LONG","fata")
        })
    }

    fun showToastMessage(message: String,length:Int,property: String){
        getViewModel()?.setToastMessageG(message,length,property)
        // snackBarLiveData.setSnackBarParam(BaseViewModel.ToastLayoutParam(message,length,property))
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