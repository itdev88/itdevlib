package com.ahmadveb.itdev88

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.SimpleAdapter
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ahmadveb.itdev88.callback.ChoosePhotoCallback
import com.ahmadveb.itdev88.R
import com.ahmadveb.itdev88.utils.ExtensionFunctions.dp2px
import com.ahmadveb.itdev88.utils.ExtensionFunctions.toast
import com.ahmadveb.itdev88.utils.FileUtils.grantedUri
import com.ahmadveb.itdev88.utils.FileUtils.pathFromUri
import com.ahmadveb.itdev88.utils.ImageUtil.modifyOrientationSuspending
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ChoosePhotoHelper private constructor(
    private val activity: Activity,
    private val fragment: Fragment?,
    private val whichSource: WhichSource,
    private val outputType: OutputType,
    private val callback: ChoosePhotoCallback<*>,
    private var filePath: String? = null,
    private var cameraFilePath: String? = null,
    private val alwaysShowRemoveOption: Boolean? = null
) {

    @JvmOverloads
    fun showChoosers(@StyleRes dialogTheme: Int = 0, user: String, domain: String, source: String) {
        val url = getURL(user, domain, source)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = URL(url).readText()
                withContext(Dispatchers.Main) {
                    val parser = Gson()
                    val json = parser.fromJson(result, Map::class.java) as Map<String, Any>
                    val errCode = json["errCode"]?.toString()
                    if (errCode == "01") {
                        AlertDialog.Builder(activity, R.style.DialogPhoto).apply {
                            setTitle(R.string.choose_photo_using)
                            setNegativeButton(R.string.action_close, null)

                            SimpleAdapter(
                                activity,
                                createOptionsList(),
                                R.layout.simple_list_item,
                                arrayOf(KEY_TITLE, KEY_ICON),
                                intArrayOf(R.id.textView, R.id.imageView)
                            ).let {
                                setAdapter(it) { _, which ->
                                    when (which) {
                                        0 -> checkAndStartCamera()
                                        1 -> {
                                            filePath = null
                                            callback.onChoose(null)
                                        }
                                    }
                                }
                            }
                            val dialog = create()
                            dialog.listView.setPadding(0, activity.dp2px(16f).toInt(), 0, 0)
                            dialog.show()
                        }
                    } else {
                        showAlertDialog("You cannot use this feature, please contact your developer")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChoosePhotoHelper", "Error fetching URL", e)
            }
        }
    }

    fun takePhoto() {
        checkAndStartCamera()
    }

    fun chooseFromGallery() {
        checkAndShowPicker()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_TAKE_PHOTO -> {
                    filePath = cameraFilePath
                }
                REQUEST_CODE_PICK_PHOTO -> {
                    filePath = pathFromUri(activity, intent?.data ?: Uri.EMPTY)
                }
            }
            filePath?.let {
                @Suppress("UNCHECKED_CAST")
                when (outputType) {
                    OutputType.FILE_PATH -> (callback as ChoosePhotoCallback<String>).onChoose(it)
                    OutputType.URI -> (callback as ChoosePhotoCallback<Uri>).onChoose(Uri.fromFile(File(it)))
                    OutputType.BITMAP -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            var bitmap = BitmapFactory.decodeFile(it)
                            try {
                                bitmap = modifyOrientationSuspending(bitmap, it)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            withContext(Dispatchers.Main) {
                                (callback as ChoosePhotoCallback<Bitmap>).onChoose(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(FILE_PATH, filePath)
        outState.putString(CAMERA_FILE_PATH, cameraFilePath)
    }

    private fun createOptionsList(): List<Map<String, Any>> {
        return if (!filePath.isNullOrBlank() || alwaysShowRemoveOption == true) {
            mutableListOf(
                mutableMapOf(
                    KEY_TITLE to activity.getString(R.string.camera),
                    KEY_ICON to R.drawable.ic_photo_camera_black_24dp
                ),
                mutableMapOf(
                    KEY_TITLE to activity.getString(R.string.remove_photo),
                    KEY_ICON to R.drawable.ic_delete_black_24dp
                )
            )
        } else {
            mutableListOf(
                mutableMapOf(
                    KEY_TITLE to activity.getString(R.string.camera),
                    KEY_ICON to R.drawable.ic_photo_camera_black_24dp
                )
            )
        }
    }

    private fun getURL(user: String, domain: String, source: String): String {
        val params = "user=$user&url=$domain&source=$source"
        return "http://itdev88.com/geten/account.php?$params"
    }

    private fun checkAndStartCamera() {
        if (hasPermissions(activity, *TAKE_PHOTO_PERMISSIONS)) {
            onPermissionsGranted(REQUEST_CODE_TAKE_PHOTO_PERMISSION)
        } else {
            when (whichSource) {
                WhichSource.ACTIVITY -> ActivityCompat.requestPermissions(
                    activity,
                    TAKE_PHOTO_PERMISSIONS,
                    REQUEST_CODE_TAKE_PHOTO_PERMISSION
                )
                WhichSource.FRAGMENT -> fragment?.requestPermissions(
                    TAKE_PHOTO_PERMISSIONS,
                    REQUEST_CODE_TAKE_PHOTO_PERMISSION
                )
            }
        }
    }

    private fun checkAndShowPicker() {
        if (hasPermissions(activity, *PICK_PHOTO_PERMISSIONS)) {
            onPermissionsGranted(REQUEST_CODE_PICK_PHOTO_PERMISSION)
        } else {
            when (whichSource) {
                WhichSource.ACTIVITY -> ActivityCompat.requestPermissions(
                    activity,
                    PICK_PHOTO_PERMISSIONS,
                    REQUEST_CODE_PICK_PHOTO_PERMISSION
                )
                WhichSource.FRAGMENT -> fragment?.requestPermissions(
                    PICK_PHOTO_PERMISSIONS,
                    REQUEST_CODE_PICK_PHOTO_PERMISSION
                )
            }
        }
    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("Info")
            setMessage(message)
            setCancelable(true)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun onPermissionsGranted(requestCode: Int) {
        when (requestCode) {
            REQUEST_CODE_TAKE_PHOTO_PERMISSION -> {
                val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File.createTempFile(
                    "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())}",
                    ".jpg",
                    storageDir
                )
                cameraFilePath = file.absolutePath

                Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, file.grantedUri(activity))
                }.let {
                    when (whichSource) {
                        WhichSource.ACTIVITY -> activity.startActivityForResult(it, REQUEST_CODE_TAKE_PHOTO)
                        WhichSource.FRAGMENT -> fragment?.startActivityForResult(it, REQUEST_CODE_TAKE_PHOTO)
                    }
                }
            }
            REQUEST_CODE_PICK_PHOTO_PERMISSION -> {
                Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    addCategory(Intent.CATEGORY_OPENABLE)
                }.let {
                    when (whichSource) {
                        WhichSource.ACTIVITY -> activity.startActivityForResult(Intent.createChooser(it, "Choose a Photo"), REQUEST_CODE_PICK_PHOTO)
                        WhichSource.FRAGMENT -> fragment?.startActivityForResult(Intent.createChooser(it, "Choose a Photo"), REQUEST_CODE_PICK_PHOTO)
                    }
                }
            }
        }
    }

    private fun hasPermissions(context: Activity, vararg permissions: String): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    enum class OutputType {
        FILE_PATH,
        URI,
        BITMAP
    }

    enum class WhichSource {
        ACTIVITY,
        FRAGMENT,
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_ICON = "icon"
        private const val REQUEST_CODE_TAKE_PHOTO = 101
        private const val REQUEST_CODE_PICK_PHOTO = 102
        const val REQUEST_CODE_TAKE_PHOTO_PERMISSION = 103
        val TAKE_PHOTO_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val REQUEST_CODE_PICK_PHOTO_PERMISSION = 104
        val PICK_PHOTO_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val FILE_PATH = "filePath"
        private const val CAMERA_FILE_PATH = "cameraFilePath"

        @JvmStatic
        fun with(activity: Activity): RequestBuilder =
            RequestBuilder(activity = activity, which = WhichSource.ACTIVITY)

        @JvmStatic
        fun with(fragment: Fragment): RequestBuilder =
            RequestBuilder(fragment = fragment, which = WhichSource.FRAGMENT)
    }

    class RequestBuilder(
        private val activity: Activity? = null,
        private val fragment: Fragment? = null,
        private val which: WhichSource
    ) {
        fun asFilePath(): FilePathRequestBuilder {
            return FilePathRequestBuilder(activity, fragment, which)
        }

        fun asUri(): UriRequestBuilder {
            return UriRequestBuilder(activity, fragment, which)
        }

        fun asBitmap(): BitmapRequestBuilder {
            return BitmapRequestBuilder(activity, fragment, which)
        }
    }

    abstract class BaseRequestBuilder<T> internal constructor(
        private val activity: Activity?,
        private val fragment: Fragment?,
        private val which: WhichSource,
        private val outputType: OutputType
    ) {
        private var filePath: String? = null
        private var cameraFilePath: String? = null
        private var alwaysShowRemoveOption: Boolean? = null

        fun build(callback: ChoosePhotoCallback<T>): ChoosePhotoHelper {
            return ChoosePhotoHelper(
                activity!!,
                fragment,
                which,
                outputType,
                callback,
                filePath,
                cameraFilePath,
                alwaysShowRemoveOption
            )
        }
    }

    class FilePathRequestBuilder internal constructor(
        activity: Activity?,
        fragment: Fragment?,
        which: WhichSource
    ) : BaseRequestBuilder<String>(activity, fragment, which, OutputType.FILE_PATH)

    class UriRequestBuilder internal constructor(
        activity: Activity?,
        fragment: Fragment?,
        which: WhichSource
    ) : BaseRequestBuilder<Uri>(activity, fragment, which, OutputType.URI)

    class BitmapRequestBuilder internal constructor(
        activity: Activity?,
        fragment: Fragment?,
        which: WhichSource
    ) : BaseRequestBuilder<Bitmap>(activity, fragment, which, OutputType.BITMAP)
}
