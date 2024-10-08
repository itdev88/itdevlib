package com.ahmadveb.itdev88.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object Grather {


    @SuppressLint("SimpleDateFormat")
    fun getAbbreviatedFromDateTime(dateTime: String, dateFormat: String, field: String): String? {
        val input = SimpleDateFormat(dateFormat)
        val output = SimpleDateFormat(field, Locale("en", "EN"))
        try {
            val getAbbreviate = input.parse(dateTime)    // parse input
            return output.format(getAbbreviate)    // format output
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    @SuppressLint("DefaultLocale")
    fun getInisialName(name: String?): String {
        var fullName: String? = name ?: return ""
        fullName = fullName!!.trim { it <= ' ' }
        val separateName = fullName.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (separateName.isEmpty()) {
            return ""
        }
        return if (separateName.size > 1) {
            separateName[0][0] + "" + separateName[1][0]
        } else {
            separateName[0][0].toString().toUpperCase()
        }
    }

    @SuppressLint("DefaultLocale")
    fun getInisialNameDot(name: String?): String {
        var fullName: String? = name ?: return ""
        fullName = fullName!!.trim { it <= '.' }
        val separateName = fullName.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (separateName.isEmpty()) {
            return ""
        }
        return if (separateName.size > 1) {
            separateName[0][0] + "" + separateName[1][0]
        } else {
            separateName[0][0].toString().toUpperCase()
        }
    }

    class DecimalDigitsInputFilter(
        maxDigitsIncludingPoint: Int, maxDecimalPlaces: Int
    ) : InputFilter {
        private val pattern: Pattern = Pattern.compile(
            "[0-9]{0," + (maxDigitsIncludingPoint - 1) + "}+((\\.[0-9]{0,"
                    + (maxDecimalPlaces - 1) + "})?)||(\\.)?"
        )

        override fun filter(

            p0: CharSequence?, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int

        ): CharSequence? {

            p3?.apply {

                val matcher: Matcher = pattern.matcher(p3)

                return if (!matcher.matches()) "" else null

            }

            return null

        }

    }

    fun convertToCurrency(value: String): String {
        val currentValue: Double
        currentValue = try {
            java.lang.Double.parseDouble(value)
        } catch (nfe: NumberFormatException) {
            0.0
        }

        return convertToCurrency(currentValue)
    }

    fun convertToCurrency(value: Int): String {
        val currentValue: Double
        currentValue = try {
            value.toDouble()
        } catch (nfe: NumberFormatException) {
            0.0
        }

        return convertToCurrency(currentValue)
    }

    fun convertToCurrency(amount: Double): String {
        val formatter = DecimalFormat("#,###,###")
        return formatter.format(amount).replace(",", ".")
    }

    fun openAppSettings(activity: Activity) {
        val packageUri = Uri.fromParts("package", activity.applicationContext.packageName, null)
        val applicationDetailsSettingsIntent = Intent()
        applicationDetailsSettingsIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        applicationDetailsSettingsIntent.data = packageUri
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.applicationContext.startActivity(applicationDetailsSettingsIntent)
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneValid(phone: String): Boolean {
        if (phone.length < 8) {
            return false
        }
        val prefix = phone.substring(0, 0)
        if ("" == prefix) {
            return true
        }
        return false
    }
    fun getJsonStringFromAssets(context: Context, fileName: String): String? {
        val json: String?
        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun createPartFromString(text: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), text)
    }

    fun createPartFromFile(path: String?, key: String): MultipartBody.Part? {
        if (path == null) {
            return null
        }
        val file = File(path)
        val request = RequestBody.create(MediaType.parse("image/*"), file)
        return MultipartBody.Part.createFormData(key, file.name, request)
    }

    fun shareBitmapToApps(context: Context, pathUri: Uri) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "image/*"
        i.putExtra(Intent.EXTRA_STREAM, pathUri)
        context.startActivity(Intent.createChooser(i, "Share to ..."))
    }

}