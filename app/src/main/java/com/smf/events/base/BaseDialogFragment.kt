package com.smf.events.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.smf.events.MainActivity
import com.smf.events.helper.SnackBar

abstract class BaseDialogFragment<V : ViewDataBinding, out T : BaseDialogViewModel> :
    DialogFragment() {
    protected var mDataBinding: V? = null
    private var mViewModel: T? = null

    abstract fun getViewModel(): T?

    abstract fun getBindingVariable(): Int

    abstract fun getContentView(): Int
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBinding = DataBindingUtil.inflate(inflater, getContentView(), container, false)
        performDataBinding()
        toastObserver()
        return mDataBinding?.root
    }


    private fun performDataBinding() {
        getViewModel()?.let { viewModel ->
            mViewModel = ViewModelProvider(this).get(viewModel::class.java)
            mDataBinding?.setVariable(getBindingVariable(), mViewModel)
            mDataBinding?.lifecycleOwner = this
            mDataBinding?.executePendingBindings()
        }
    }


    private fun toastObserver() {
        getViewModel()?.getToastMessageG?.observe(viewLifecycleOwner) { toastMessageG ->
            Log.d("TAG", "onResume Base Fragment $toastMessageG")
            SnackBar.showSnakbarTypeOne(
                view,
                toastMessageG.msg,
                requireActivity(),
                toastMessageG.duration
            )
            //Toast(context).showCustomToast(toastMessageG.msg,requireActivity(),"Toast.LENGTH_LONG","fata")
        }

    }

    fun showToastMessage(message: String, length: Int, property: String) {
        getViewModel()?.setToastMessageG(message, length, property)
        // snackBarLiveData.setSnackBarParam(BaseViewModel.ToastLayoutParam(message,length,property))
    }
}