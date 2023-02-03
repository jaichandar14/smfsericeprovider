package com.smf.events.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.smf.events.MainActivity
import com.smf.events.helper.SnackBar

abstract class BaseFragment<V : ViewDataBinding, out T : BaseViewModel> : Fragment() {
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
        toastObserver()
        performDataBinding()
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

    fun showToast(msg: String) {
        Toast.makeText(activity?.applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    private fun toastObserver() {
        getViewModel()?.getToastMessageG?.observe(viewLifecycleOwner) { toastMessageG ->
            Log.d("TAG", "onResume Base Fragment $toastMessageG")
            if (toastMessageG.msg.isNotEmpty()) {
                SnackBar.showSnakbarTypeOne(
                    view,
                    toastMessageG.msg,
                    requireActivity(),
                    toastMessageG.duration
                )
            }
            //Toast(context).showCustomToast(toastMessageG.msg,requireActivity(),"Toast.LENGTH_LONG","fata")
        }
    }

    fun showToastMessage(message: String, length: Int, property: String) {
        getViewModel()?.setToastMessageG(message, length, property)
        // snackBarLiveData.setSnackBarParam(BaseViewModel.ToastLayoutParam(message,length,property))
    }

    // 2845 - Hiding Progress Bar
    fun hideKeyBoard() {
        val view: View? = requireActivity().currentFocus
        val inputManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}