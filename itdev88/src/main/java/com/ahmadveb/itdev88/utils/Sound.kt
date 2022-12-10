package com.ahmadveb.itdev88.utils

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.ahmadveb.itdev88.R
import java.io.IOException

object Sound {

    var mMediaPlayer: MediaPlayer? = null

    fun playSound(context: Context) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.sound)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    fun playSoundChat(context: Context) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.ding)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }

    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    // 4. Destroys the MediaPlayer instance when the app is closed
    fun onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

}