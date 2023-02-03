package com.smf.events.base

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.smf.events.R
import com.smf.events.helper.AppConstants
import com.smf.events.helper.ConnectionLiveData
import com.smf.events.helper.InternetErrorDialog
import com.smf.events.helper.SnackBar
import com.smf.events.listeners.DialogTwoButtonListener
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

abstract class BaseActivity<T : ViewDataBinding, out V : BaseViewModel> : AppCompatActivity(),
    DialogTwoButtonListener {

    protected var mViewDataBinding: T? = null
    private var mViewModel: V? = null
    private lateinit var connectionLiveData: ConnectionLiveData
    var networkDialog: InternetErrorDialog? = null

    abstract fun getContentView(): Int

    abstract fun getViewModel(): V?

    fun getViewDataBinding(): T? {
        return mViewDataBinding
    }

    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        // Create instance for internet dialog
        networkDialog = InternetErrorDialog.newInstance(this)
        connectionLiveData = ConnectionLiveData(this)
        observerMethod()
        netWorkObserver()
    }

    private fun netWorkObserver() {
        connectionLiveData.observe(this) { isNetworkAvailable ->
            when (isNetworkAvailable) {
                true -> {
                    Log.d("TAG", "onResume network observer: act available $isNetworkAvailable")
                    getViewModel()?.networkState?.value = true
                    if (networkDialog?.isVisible == true) {
                        networkDialog?.dismiss()
                    }
                }
                false -> {
                    Log.d("TAG", "onResume network observer: act not available $isNetworkAvailable")
                    getViewModel()?.networkState?.value = false
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            getViewModel()?.networkStateFlow?.filterNotNull()?.filter { it }?.collectLatest {
                RxBus.publish(RxEvent.InternetStatus(true))
            }
        }
    }

    private fun observerMethod() {
        Log.d("TAG", "on toast create Base Activity befor")
        getViewModel()?.getToastMessageG?.observe(this) { toastMessageG ->
            Log.d("TAG", "on toast create Base Activity $toastMessageG")
            SnackBar.showSnakbarTypeOne(
                mViewDataBinding?.root,
                toastMessageG.msg,
                this,
                toastMessageG.duration
            )
        }
    }

    fun showToastMessage(message: String, length: Int, property: String) {
        getViewModel()?.setToastMessageG(message, length, property)
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

    open fun setStatusBarColor() {
        window.statusBarColor = getColor(R.color.theme_color)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    override fun onPositiveClick(dialogFragment: DialogFragment) {
        when {
            dialogFragment.tag.equals(AppConstants.INTERNET_DIALOG) -> {
                dialogFragment.dismiss()
                Log.d("TAG", "call: called onPositiveClick")
                RxBus.publish(RxEvent.InternetStatus(true))
            }
            else -> {
                Log.d("TAG", "call: called else onPositiveClick")
            }
        }
    }

    override fun onNegativeClick(dialogFragment: DialogFragment) {}

    fun showInternetDialog(message: String) {
        if (networkDialog?.isVisible == false
            && networkDialog?.tag != AppConstants.INTERNET_DIALOG
        ) {
            val bundle = Bundle().apply {
                putString(AppConstants.MESSAGE, message)
            }
            networkDialog?.arguments = bundle
            networkDialog!!.show(
                supportFragmentManager,
                AppConstants.INTERNET_DIALOG
            )
        }
    }
}