package com.smf.events.ui.quotebriefdialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotebriefdialog.model.Datas
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class QuoteBriefDialog(var status: Int) :
    BaseDialogFragment<QuoteBriefDialogBinding, QuoteBriefDialogViewModel>(),
    Tokens.IdTokenCallBackInterface {

    var bidRequestId: Int? = 0
    lateinit var idToken: String
    var expand = false
    lateinit var bidStatus: String
    var serviceName: String = "null"
    var branchName: String = ""
    lateinit var file: File
    private var fileName: String? = null
    private var fileContent: String? = null
    private var fileSize: String? = null
    var isViewQuoteClicked = false

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(status: Int): QuoteBriefDialog {
            return QuoteBriefDialog(status)
        }
    }

    init {
        bidRequestId = status
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    override fun getViewModel(): QuoteBriefDialogViewModel =
        ViewModelProvider(this, factory).get(QuoteBriefDialogViewModel::class.java)

    override fun getBindingVariable(): Int = BR.quoteBriefDialogViewModel

    override fun getContentView(): Int = R.layout.quote_brief_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        // Initialize Local Variables
        setIdTokenAndBidReqId()
    }

    override fun onStart() {
        super.onStart()
        apiTokenValidationQuoteBrief("quoteDetails")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mDataBinding?.quoteBriefDialog?.visibility = View.INVISIBLE
        mDataBinding?.progressBar?.visibility = View.VISIBLE

        // token CallBackInterface
        tokens.setCallBackInterface(this)
        // Back Button Pressed
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
        // Expandable view
        getViewModel().expandableView(mDataBinding, expand)
        // 2962 View Quotes
        viewQuotes()
    }

    // 2962 view Quotes
    private fun viewQuotes() {
        mDataBinding?.viewQuote?.setOnClickListener {
            mDataBinding?.quoteBriefDialogLayout?.visibility = View.GONE
            mDataBinding?.viewQuotes?.visibility = View.VISIBLE
            isViewQuoteClicked = true
            mDataBinding?.txCatering?.text = "Quote details for $serviceName"
            apiTokenValidationQuoteBrief("viewQuotes")
            mDataBinding?.btnBack?.setOnClickListener {
                mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
                mDataBinding?.viewQuotes?.visibility = View.GONE

                onResume()
            }
        }

    }

    // 2962 Api call View Quotes
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getViewQuotes(idToken: String) {
        getViewModel().getViewQuote(idToken, status)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "getViewQuotes: ${apiResponse.response.data}")
                        val data = apiResponse.response.data
                        setViewQuote(data)
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                        mDataBinding?.progressBar?.visibility = View.INVISIBLE
                    }
                    else -> {
                    }
                }
            })
    }

    // 2962 Method to set the view Quotes
    private fun setViewQuote(data: Datas) {
        fileName = data.fileName
        fileSize = data.fileSize
        fileContent = data.fileContent
        // Set costing type
        mDataBinding?.costingType?.text = data.costingType
        // Set filename
        mDataBinding?.filenameTx?.text = data.fileName
        // Visible the Documents
        if (!data.fileContent.isNullOrEmpty()) {
            mDataBinding?.filenameTx?.visibility = View.VISIBLE
            mDataBinding?.fileImg?.visibility = View.VISIBLE
            mDataBinding?.fileImgDelete?.visibility = View.VISIBLE
        }
        // Showing the currency type
        if (data.currencyType.isNullOrEmpty()) {
            if (data.costingType == "Bidding") {
                mDataBinding?.costEstimationAmount?.text = data.latestBidValue
            } else mDataBinding?.costEstimationAmount?.text = "$ " + data.cost
        } else {
            if (data.costingType == "Bidding") {
                val currencyType = when (data.currencyType) {
                    "USD($)" -> "$ "
                    "GBP(\u00a3)" -> "\u00a3 "
                    "INR(\u20B9)" -> "₹ "
                    else -> {
                        "$ "
                    }
                }
                mDataBinding?.costEstimationAmount?.text =
                    currencyType + data.latestBidValue
            } else mDataBinding?.costEstimationAmount?.text = "$ " + data.cost
        }
        if (!data.comment.isNullOrEmpty()) {
            mDataBinding?.etComments?.text = data.comment
        }
//       if (checkPermission()) {
//           Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
//       } else {
//           requestPermission()
//       }


        mDataBinding?.fileImgDelete?.setOnClickListener {
            //generatePDF()
            //writeFileExternalStorage()
            //generateNoteOnSD(context,fileName,fileContent)
            //  writeResponseBodyToDisk(fileName,fileContent,fileSize)
        }
    }

    private fun requestPermission() {
        // requesting permissions if not provided.
    }

    override fun onResume() {
        super.onResume()
        // 2962
        if (isViewQuoteClicked == true) {
            mDataBinding?.viewQuotes?.visibility = View.GONE
        } else {
            mDataBinding?.viewQuotes?.visibility = View.GONE
        }
        mDataBinding?.txCatering?.text = "${serviceName}-${branchName}"
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
    }

    // Back Button Pressed
    private fun backButtonClickListener() {
        parentFragmentManager.setFragmentResult(
            "1", // Same request key DashBoardFragment used to register its listener
            bundleOf("key" to "value") // The data to be passed to DashBoardFragment
        )
        dismiss()
    }

    // Setting Bid Submitted Quote
    private fun setBidSubmitQuoteBrief(response: QuoteBrief) {
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        // 2835
        serviceName = response.data.serviceName
        branchName = response.data.branchName
        mDataBinding?.serviceName?.text = response.data.serviceName
        mDataBinding?.branchName?.text = response.data.branchName
        mDataBinding?.txJobTitle?.text = response.data.eventName
        // 2962
        mDataBinding?.txJobTitleVo?.text = response.data.eventName
        mDataBinding?.serviceNameVo?.text = response.data.serviceName
        mDataBinding?.branchNameVo?.text = response.data.branchName
        mDataBinding?.txJobTitleVo?.text = response.data.eventName
        mDataBinding?.txJobIdnumVo?.text = response.data.eventServiceDescriptionId.toString()
        // 2354
        val currencyType = setCurrencyType(response)
        if (response.data.costingType == AppConstants.BIDDING) {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.latestBidValue}"
        } else {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.cost}"
        }
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = (response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = (response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = (response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = (response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"

        mDataBinding?.progressBar?.visibility = View.INVISIBLE

    }

    // 2354 - Method For Setting CurrencyType
    private fun setCurrencyType(response: QuoteBrief): String {
        val currencyType = if (response.data.currencyType == null) {
            "$"
        } else {
            when (response.data.currencyType) {
                "USD($)" -> "$"
                "GBP(\u00a3)" -> "\u00a3"
                "INR(\u20B9)" -> "₹"
                else -> {
                    "$"
                }
            }
        }
        return currencyType
    }

    // Setting Bid Pending Quote
    @SuppressLint("SetTextI18n")
    private fun setPendingQuoteBrief(response: QuoteBrief) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility = View.INVISIBLE
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        mDataBinding?.serviceName?.text = response.data.serviceName
        mDataBinding?.branchName?.text = response.data.branchName
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txJobAmount?.visibility = View.INVISIBLE
        mDataBinding?.viewQuote?.visibility = View.INVISIBLE
        mDataBinding?.spnBidAccepted?.text = "Pending For Quote"
        mDataBinding?.check1?.visibility = View.INVISIBLE
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = (response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = (response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = (response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = (response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"
    }

    // Setting IDToken
    private fun setIdTokenAndBidReqId() {
        bidRequestId = sharedPreference.getInt(SharedPreference.BID_REQUEST_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    // 2904 Get Api Call for getting the Quote Brief
    private fun quoteBriefApiCall(idToken: String) {
        getViewModel().getQuoteBrief(idToken, status)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        bidStatus = apiResponse.response.data.bidStatus
                        when (bidStatus) {
                            AppConstants.BID_SUBMITTED -> {
                                setBidSubmitQuoteBrief(apiResponse.response)
                                mDataBinding?.spnBidAccepted?.text =
                                    AppConstants.BIDDING_IN_PROGRESS
                            }
                            AppConstants.PENDING_FOR_QUOTE -> setPendingQuoteBrief(apiResponse.response)
                            // 2904 Won Bid Flow for Start Sevice
                            AppConstants.WON_BID -> {
                                widgetWonBid(apiResponse)
                            }
                            AppConstants.SERVICE_IN_PROGRESS -> {
                                widgetServiceProgress(apiResponse)
                            }
                            AppConstants.SERVICE_DONE -> {
                                widgetServiceCloser(apiResponse)
                            }
                        }
                        mDataBinding?.quoteBriefDialog?.visibility = View.VISIBLE
                        mDataBinding?.progressBar?.visibility = View.INVISIBLE
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                        mDataBinding?.progressBar?.visibility = View.INVISIBLE
                    }
                    else -> {
                    }
                }
            })
    }

    // 2904 Method for won bid service start flow
    fun widgetWonBid(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.BID_WON_SMALL
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.inprogress)
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // 2904 Method for Initiate closer flow
    fun widgetServiceProgress(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.inprogress)
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceProgress?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_IN_PROGRESS_SMALL
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    fun widgetServiceCloser(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.green_check)
        mDataBinding?.check5?.setImageResource(R.drawable.green_check)
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceProgress?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceCompleted?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow5?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_COMPLETED
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // Call Back From Token Class
    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        if (caller == "viewQuotes") {
            withContext(Dispatchers.Main) {
                getViewQuotes(idToken)
            }
        } else {
            withContext(Dispatchers.Main) {
                quoteBriefApiCall(idToken)

            }
        }
    }

    // Api Token Validation For Quote Brief Api Call
    private fun apiTokenValidationQuoteBrief(s: String) {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            s, idToken
        )
    }
//    private fun checkPermission(): Boolean {
//        // checking of permissions.
//        val permission1 =
//            ContextCompat.checkSelfPermission(ApplicationProvider.getApplicationContext<Context>(),
//                WRITE_EXTERNAL_STORAGE)
//        val permission2 =
//            ContextCompat.checkSelfPermission(ApplicationProvider.getApplicationContext<Context>(),
//                READ_EXTERNAL_STORAGE)
//        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
//    }


//    @RequiresApi(Build.VERSION_CODES.R)
//    fun generateNoteOnSD(context: Context?, sFileName: String?, sBody: String?) {
//        try {
//            val root:File = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
//            if (!root.exists()) {
//                root.mkdirs()
//            }
//// String encode = Base64.encodeToString(edit_query.getText().toString().getBytes(), Base64.DEFAULT);
////               text.setText(encode);
////               byte[] data = Base64.decode(encode, Base64.DEFAULT);
////               String text = new String(data, StandardCharsets.UTF_8);
//            val pdfAsBytes: ByteArray = Base64.decode(sBody, Base64.DEFAULT)
//            Log.d(TAG, "generateNoteOnSD:$sBody ${root.absolutePath} ")
//            val input: InputStream?=sBody?.byteInputStream(StandardCharsets.UTF_8)
//            var bytes = getBytes(input!!)
//            val data: ByteArray = Base64.decode(bytes, Base64.DEFAULT)
//            val fileContext = String(data)
//            val gpxfile = File(root, sFileName)
//            val writer = FileWriter(gpxfile)
//            writer.append(fileContext)
//            writer.flush()
//            writer.close()
//            Log.d(TAG, "generateNoteOnSD:$fileContext ${bytes} ")
//            Toast.makeText(context, "Saved ${root.absolutePath}", Toast.LENGTH_SHORT).show()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//      }
//    @Throws(IOException::class)
//    fun getBytes(inputStream: InputStream): ByteArray? {
//        val byteBuffer = ByteArrayOutputStream()
//        val bufferSize = 1024
//        val buffer = ByteArray(bufferSize)
//        var len = 0
//        while (inputStream.read(buffer).also { len = it } != -1) {
//            byteBuffer.write(buffer, 0, len)
//        }
//        return byteBuffer.toByteArray()
//    }
//
//
//
//private  fun generatePDF() {
//    // declaring width and height
//    // for our PDF file.
//    // declaring width and height
//    // for our PDF file.
//    val pageHeight = 1120
//    val pagewidth = 792
//    // creating an object variable
//    // for our PDF document.
//    val pdfDocument = PdfDocument()
//
//    // two variables for paint "paint" is used
//    // for drawing shapes and we will use "title"
//    // for adding text in our PDF file.
//    val paint = Paint()
//    val title = Paint()
//    val bmp: Bitmap
//    val scaledbmp: Bitmap
//    // we are adding page info to our PDF file
//    // in which we will be passing our pageWidth,
//    // pageHeight and number of pages and after that
//    // we are calling it to create our PDF.
//    val mypageInfo = PageInfo.Builder(pagewidth, pageHeight, 1).create()
//    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.festo_login_logo);
//    scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);
//    // below line is used for setting
//    // start page for our PDF file.
//    val myPage = pdfDocument.startPage(mypageInfo)
//
//    // creating a variable for canvas
//    // from our page of PDF.
//    val canvas: Canvas = myPage.canvas
//
//    // below line is used to draw our image on our PDF file.
//    // the first parameter of our drawbitmap method is
//    // our bitmap
//    // second parameter is position from left
//    // third parameter is position from top and last
//    // one is our variable for paint.
//  //  canvas.drawBitmap(scaledbmp, 56, 40, paint)
//
//    // below line is used for adding typeface for
//    // our text which we will be adding in our PDF file.
//    title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
//
//    // below line is used for setting text size
//    // which we will be displaying in our PDF file.
//
//
//    // below line is sued for setting color
//    // of our text inside our PDF file.
//    title.setColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
//
//    // below line is used to draw text in our PDF file.
//    // the first parameter is our text, second parameter
//    // is position from start, third parameter is position from top
//    // and then we are passing our variable of paint which is title.
//    canvas.drawText("A portal for IT professionals.", 209F, 100F, title)
//    canvas.drawText("Geeks for Geeks", 209F, 80F, title)
//
//    // similarly we are creating another text and in this
//    // we are aligning this text to center of our PDF file.
//    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
//    title.setColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
//    title.setTextSize(15F)
//
//    // below line is used for setting
//    // our text to center of PDF.
//    title.setTextAlign(Paint.Align.CENTER)
//    canvas.drawText(fileContent.toString(), 396F, 560F, title)
//
//    // after adding all attributes to our
//    // PDF file we will be finishing our page.
//    pdfDocument.finishPage(myPage)
//
//    // below line is used to set the name of
//    // our PDF file and its path.
//    val file = File(requireContext().getExternalFilesDir(null), fileName)
//    try {
//        // after creating a file name we will
//        // write our PDF file to that location.
//        pdfDocument.writeTo(FileOutputStream(file))
//
//        // below line is to print toast message
//        // on completion of PDF generation.
//        Toast.makeText(requireContext(), "PDF file generated successfully. ", Toast.LENGTH_SHORT)
//            .show()
//    } catch (e: IOException) {
//        // below line is used
//        // to handle error
//        e.printStackTrace()
//    }
//    // after storing our pdf to that
//    // location we are closing our PDF file.
//    pdfDocument.close()
//}
//
//    // Method for creating a pdf file from text, saving it then opening it for display
//    fun createandDisplayPdf(text: String?) {
//        val doc = Document()
//        try {
//            val path = requireContext().getExternalFilesDir(null).absolutePath + "/Dir"
//            val dir = File(path)
//            if (!dir.exists()) dir.mkdirs()
//            val file = File(dir, "newFile.pdf")
//            val fOut = FileOutputStream(file)
//            PdfWriter.getInstance(doc, fOut)
//
//            //open the document
//            doc.open()
//            val p1 = Paragraph(text)
//            val paraFont = Font(Font.COURIER)
//            p1.setAlignment(Paragraph.ALIGN_CENTER)
//            p1.setFont(paraFont)
//
//            //add paragraph to document
//            doc.add(p1)
//        } catch (de: DocumentException) {
//            Log.e("PDFCreator", "DocumentException:$de")
//        } catch (e: IOException) {
//            Log.e("PDFCreator", "ioException:$e")
//        } finally {
//            doc.close()
//        }
//        viewPdf("newFile.pdf", "Dir")
//    }
//
//    // Method for opening a pdf file
//    private fun viewPdf(file: String, directory: String) {
//        val pdfFile = File(requireContext().getExternalFilesDir(null)
//            .toString() + "/" + directory + "/" + file)
//        val path: Uri = Uri.fromFile(pdfFile)
//
//        // Setting the intent for pdf reader
//        val pdfIntent = Intent(Intent.ACTION_VIEW)
//        pdfIntent.setDataAndType(path, "application/pdf")
//        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        try {
//            startActivity(pdfIntent)
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(requireContext(), "Can't read pdf file", Toast.LENGTH_SHORT).show()
//        }
//    }
}