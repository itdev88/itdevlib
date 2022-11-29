package com.ahmadveb.itdev88.ui.ext

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toInvisible() {
    this.visibility = View.INVISIBLE
}

fun Fragment.toast(msg: String) {
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun TextView.htmlText(text: String) {
    setText(Html.fromHtml(text))
}

fun Drawable.setColorFilter(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
    } else {
        @Suppress("DEPRECATION")
        this.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun Drawable.setColorFilter(color: Int, blendMode: BlendMode, porterMode: PorterDuff.Mode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.colorFilter = BlendModeColorFilter(color, blendMode)
    } else {
        @Suppress("DEPRECATION")
        this.setColorFilter(color, porterMode)
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
