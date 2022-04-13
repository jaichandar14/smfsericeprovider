package com.smf.events.ui.quotedetailsdialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.FragmentQuoteDetailsDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.ui.quotebriefdialog.QuoteBriefDialog
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


class QuoteDetailsDialog(
    var bidRequestId: Int,
    var costingType: String,
    var bidStatus: String,
    var cost: String?,
    var latestBidValue: String?,
    var branchName: String,
    var serviceName: String
) : BaseDialogFragment<FragmentQuoteDetailsDialogBinding, QuoteDetailsDialogViewModel>(),
    QuoteDetailsDialogViewModel.CallBackInterface, Tokens.IdTokenCallBackInterface {

    lateinit var biddingQuote: BiddingQuotDto
    lateinit var file: File
    var fileName: String? = null
    var fileSize: String? = null
    var fileContent: String? = null
    lateinit var idToken: String
    var currencyTypeList = ArrayList<String>()
    var latestBidValueQuote: Int = 0

    companion object {
        const val TAG = "CustomDialogFragment"
        var currencyType = "USD($)"
        fun newInstance(
            bidRequestId: Int,
            costingType: String,
            bidStatus: String,
            cost: String?,
            latestBidValue: String?,
            branchName: String,
            serviceName: String
        ): QuoteDetailsDialog {

            return QuoteDetailsDialog(
                bidRequestId,
                costingType,
                bidStatus,
                cost,
                latestBidValue,
                branchName,
                serviceName
            )
        }
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    override fun getViewModel(): QuoteDetailsDialogViewModel =
        ViewModelProvider(this, factory).get(QuoteDetailsDialogViewModel::class.java)

    override fun getBindingVariable(): Int = BR.quoteDetailsDialogViewModel

    override fun getContentView(): Int = R.layout.fragment_quote_details_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getting IdToken
        setIdToken()
    }

    override fun onStart() {
        super.onStart()
        //Setting the dialog size
        dialogFragmentSize()
        // Token Class CallBack Initialization
        tokens.setCallBackInterface(this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding?.ForksSpoon?.text = branchName
        mDataBinding?.quoteTitleServiceName?.text = serviceName
        // Update CurrencyType ArrayList
        currencyTypeList =
            resources.getStringArray(R.array.currency_type).toList() as ArrayList<String>
        //CurrencyType SetUp
        getViewModel().getCurrencyType(mDataBinding, currencyTypeList)
        // Quote ViewModel CallBackInterface
        getViewModel().setCallBackInterface(this)
        //file uploader
        fileUploader()
        //fetching details based on Biding status
        fetchBasedOnStatus(view)
        mDataBinding?.btnCancel?.setOnClickListener {
            btnCancel()
        }
    }

    private fun btnCancel() {
        dismiss()
    }

    //fetching details based on Biding status
    private fun fetchBasedOnStatus(view: View) {
        if (bidStatus == AppConstants.PENDING_FOR_QUOTE) {
            mDataBinding!!.quotelater.visibility = View.GONE
            //I have Quotes Flow
            getViewModel().iHaveQuoteClicked(view, mDataBinding)
        } else {
            //QuoteLater Flow
            getViewModel().quoteLaterIsClicked(view, mDataBinding)
            //I have Quotes Flow
            getViewModel().iHaveQuoteClicked(view, mDataBinding)
        }
    }

    //Call back from Quote Details Dialog View Model
    override fun callBack(status: String) {
        when (status) {
            "iHaveQuote" ->
                if (mDataBinding?.costEstimationAmount?.text.isNullOrEmpty()) {
                    mDataBinding?.alertCost?.visibility = View.VISIBLE
                } else {
                    apiTokenValidationQuoteDetailsDialog("iHaveQuote")
                }
            "quoteLater" -> apiTokenValidationQuoteDetailsDialog("quoteLater")
        }
    }

    //Call back from Quote Details Dialog View Model For CurrencyType Position
    override fun getCurrencyTypePosition(position: Int) {
        currencyType = currencyTypeList[position]
    }

    //Setting the value for put Call
    private fun putQuoteDetails(bidStatus: String, idToken: String) {
        var bidValueQuote = mDataBinding?.costEstimationAmount?.text.toString()
        latestBidValueQuote = if (bidValueQuote == "") {
            0
        } else {
            bidValueQuote.toInt()
        }

        if (bidStatus == AppConstants.PENDING_FOR_QUOTE) {
            biddingQuote = BiddingQuotDto(
                bidRequestId,
                bidStatus,
                branchName,
                null,
                null,
                costingType,
                null,
                null,
                null,
                null,
                "QUOTE_DETAILS",
                latestBidValueQuote
            )
        } else {
            biddingQuote = BiddingQuotDto(
                bidRequestId,
                bidStatus,
                branchName,
                mDataBinding?.etComments?.text.toString(),
                null,
                costingType,
                currencyType,
                fileContent,
                fileName,
                fileSize,
                "QUOTE_DETAILS",
                latestBidValueQuote
            )
        }
        putQuoteApiCall(idToken)
    }

    //Put call Api For Cost and File Upload
    private fun putQuoteApiCall(idToken: String) {
        getViewModel().postQuoteDetails(idToken, bidRequestId, biddingQuote)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d("TAG", " quote for dialog Success: ${(apiResponse.response)}")

                        QuoteBriefDialog.newInstance()
                            .show(
                                (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                QuoteBriefDialog.TAG
                            )
                        dismiss()
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Main) {
            when (caller) {
                "iHaveQuote" -> putQuoteDetails(AppConstants.BID_SUBMITTED, idToken)
                "quoteLater" -> putQuoteDetails(AppConstants.PENDING_FOR_QUOTE, idToken)
            }
        }
    }

    private fun apiTokenValidationQuoteDetailsDialog(status: String) {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            status, idToken
        )
    }

    //Setting Dialog Fragment Size
    private fun dialogFragmentSize() {
        var window: Window? = dialog?.window
        var params: WindowManager.LayoutParams = window!!.attributes
        params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())

        window.attributes = params
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    private fun fileUploader() {
        var logoUploadActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    var fileUri: Uri = data?.data!!
                    var filePath = fileUri.path
                    file = File(filePath)
                    if (!filePath.isNullOrEmpty()) {
                        mDataBinding?.btnFileUpload?.text = "File Uploaded"
                        mDataBinding?.btnFileUpload?.setTextColor(Color.WHITE)
                        mDataBinding?.btnFileUpload?.setBackgroundColor(
                            ContextCompat.getColor(
                                context?.applicationContext!!, R.color.green
                            )
                        )
                    }
                    fileName = filePath?.substring(filePath.lastIndexOf("/") + 1);
                    Log.d(TAG, "logoUploader: $fileUri")
                    if (!fileUri.path.isNullOrEmpty()) {
                        convertToString(fileUri)
                    }
                }
            }
        view?.findViewById<Button>(R.id.btn_file_upload)?.setOnClickListener {
            try {
                var gallaryIntent = Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                gallaryIntent.type = "*/*"
                logoUploadActivity.launch(Intent.createChooser(gallaryIntent, "Choose a file"))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(
                    activity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private fun convertToString(uri: Uri) {
        var uriString = uri.toString()
        Log.d("data", "onActivityResult: uri$uriString")
        try {
            val input: InputStream? = activity?.contentResolver?.openInputStream(uri)
            var bytes = getBytes(input!!)
            Log.d("data", "onActivityResult: bytes size=" + bytes?.size)
            if (bytes?.size!! <= 181204) {
                Log.d(
                    "data",
                    "onActivityResult: Base64string=" + Base64.encodeToString(
                        bytes,
                        Base64.DEFAULT
                    )
                )
                fileContent = Base64.encodeToString(bytes, Base64.DEFAULT)
                fileSize = bytes.size.toString()
            } else {
                Toast.makeText(activity, "upload file below 100kb", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("error", "onActivityResult: $e")
        }
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    //Setting Id Token
    private fun setIdToken() {
        idToken = "Bearer ${sharedPreference.getSharedPreferences().getString("IdToken", "")}"
    }
}

