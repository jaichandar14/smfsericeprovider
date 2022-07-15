package com.smf.events.ui.quotedetailsdialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
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
import androidx.core.os.bundleOf
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
    var serviceName: String,
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
    var displayName: String? = null

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
            serviceName: String,
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
        // getting IdToken
        setIdToken()
    }

    override fun onStart() {
        super.onStart()
        // Setting the dialog size
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
        // CurrencyType SetUp
        getViewModel().getCurrencyType(mDataBinding, currencyTypeList)
        // Quote ViewModel CallBackInterface
        getViewModel().setCallBackInterface(this)
        // file uploader
        fileUploader()
        // fetching details based on Biding status
        fetchBasedOnStatus(view)
        mDataBinding?.btnCancel?.setOnClickListener {
            btnCancel()
        }
    }

    private fun btnCancel() {
        dismiss()
    }

    // fetching details based on Biding status
    private fun fetchBasedOnStatus(view: View) {
        if (bidStatus == AppConstants.PENDING_FOR_QUOTE) {
            mDataBinding!!.quotelater.visibility = View.GONE
            // I have Quotes Flow
            getViewModel().iHaveQuoteClicked(view, mDataBinding)
        } else {
            // QuoteLater Flow
            getViewModel().quoteLaterIsClicked(view, mDataBinding)
            // I have Quotes Flow
            getViewModel().iHaveQuoteClicked(view, mDataBinding)
        }
    }

    // Call back from Quote Details Dialog View Model
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

    // Call back from Quote Details Dialog View Model For CurrencyType Position
    override fun getCurrencyTypePosition(position: Int) {
        currencyType = currencyTypeList[position]
    }

    // Setting the value for put Call
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
            Log.d(TAG, "putQuoteDetailsnewjc: $displayName")
            biddingQuote = BiddingQuotDto(
                bidRequestId,
                bidStatus,
                branchName,
                mDataBinding?.etComments?.text.toString(),
                null,
                costingType,
                currencyType,
                fileContent,
                displayName,
                fileSize,
                "QUOTE_DETAILS",
                latestBidValueQuote
            )
        }
        putQuoteApiCall(idToken)
    }

    // Put call Api For Cost and File Upload
    private fun putQuoteApiCall(idToken: String) {
        getViewModel().postQuoteDetails(idToken, bidRequestId, biddingQuote)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        if (biddingQuote.bidStatus != AppConstants.PENDING_FOR_QUOTE) {
                            QuoteBriefDialog.newInstance(bidRequestId)
                                .show(
                                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                    QuoteBriefDialog.TAG
                                )
                        }
                        actionDetailsFragmentListUpdate()
                        dismiss()
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }  // Method For Send Data To actionDetails Fragment

    private fun actionDetailsFragmentListUpdate() {
        // Result to Send ActionDetails Fragment
        parentFragmentManager.setFragmentResult(
            "3", // Same request key ActionDetailsFragment used to register its listener
            bundleOf("key" to "value") // The data to be passed to ActionDetailsFragment
        )
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

    // Setting Dialog Fragment Size
    private fun dialogFragmentSize() {
        var window: Window? = dialog?.window
        var params: WindowManager.LayoutParams = window!!.attributes
        params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
        window.attributes = params
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // 2940 Method to Select the file from the documents
    @SuppressLint("RestrictedApi", "Range")
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
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
                        convertToString(fileUri)
                        gettingDocName(fileUri)
                    }
                }
            }
        view?.findViewById<Button>(R.id.btn_file_upload)?.setOnClickListener {
            try {
                var gallaryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                gallaryIntent.addCategory(Intent.CATEGORY_OPENABLE)
                gallaryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                // 2842 Restricting file selection
                val mimetypes =
                    arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/*|text/*", "audio/*",
                        "application/*|vnd.ms-excel/*")
                gallaryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
                gallaryIntent.type = "*/*"
                logoUploadActivity.launch(Intent.createChooser(gallaryIntent, "Choose a file"))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(
                    activity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mDataBinding?.fileImgDelete?.setOnClickListener {
            mDataBinding?.fileImgDelete?.visibility = View.GONE
            mDataBinding?.filenameTx?.visibility = View.GONE
            mDataBinding?.fileImg?.visibility = View.GONE
            fileName = null
            fileSize = null
            fileContent = null
        }
    }

    // 2940 Method to get the doc name
    @SuppressLint("Range")
    private fun gettingDocName(fileUri: Uri) {
        var cursor: Cursor? = null
        var fileEndName: String? = null
        if (fileUri.toString().startsWith("content://")) {
            try {
                cursor = requireContext().contentResolver.query(fileUri,
                    null,
                    null,
                    null,
                    null)
                if (cursor != null && cursor.moveToFirst()) {
                    displayName =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    Log.d(TAG,
                        "gettingDocName: ${displayName?.substring(displayName!!.lastIndexOf("."))}")
                    fileEndName = displayName?.substring(displayName!!.lastIndexOf("."))
                    mDataBinding?.filenameTx?.text = displayName?.take(10) + "..." + fileEndName
                }
            } finally {
                cursor!!.close()
            }
        } else if (fileUri.toString().startsWith("file://")) {
            displayName = file.name
            fileEndName = displayName?.substring(displayName!!.lastIndexOf("."))
            mDataBinding?.filenameTx?.text = displayName!!.take(10) + "..." + fileEndName
        }
    }

    // 2940 converting to string
    private fun convertToString(uri: Uri) {
        try {
            val input: InputStream? = activity?.contentResolver?.openInputStream(uri)
            var bytes = getBytes(input!!)
            if (bytes?.size!! <= 5000000) {
                mDataBinding?.fileImg?.visibility = View.VISIBLE
                mDataBinding?.filenameTx?.visibility = View.VISIBLE
                mDataBinding?.fileImgDelete?.visibility = View.VISIBLE
                fileContent = Base64.encodeToString(bytes, Base64.DEFAULT)
                fileSize = bytes.size.toString()
            } else {
                mDataBinding?.fileImg?.visibility = View.GONE
                mDataBinding?.filenameTx?.text = fileName
                fileName = null
                fileContent = null
                fileSize = null
                mDataBinding?.filenameTx?.visibility = View.GONE
                mDataBinding?.fileImgDelete?.visibility = View.GONE
                Toast.makeText(activity,
                    "File is not uploaded. File Size Should Not Exceed 5MB.",
                    Toast.LENGTH_SHORT).show()
                mDataBinding?.btnFileUpload?.setBackgroundColor(
                    ContextCompat.getColor(
                        context?.applicationContext!!, R.color.green))
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

    // Setting Id Token
    private fun setIdToken() {
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }
}

