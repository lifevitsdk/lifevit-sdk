package es.lifevit.pillreminder.utils


import android.util.Log
import es.lifevit.pillreminder.BuildConfig


object LogUtil {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG_MESSAGES) {
            Log.d(tag, message)
        }
    }


}