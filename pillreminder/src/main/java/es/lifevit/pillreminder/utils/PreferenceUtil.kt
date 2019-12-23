package es.lifevit.pillreminder.utils

import android.preference.PreferenceManager
import es.lifevit.pillreminder.PRApplication

/**
 * Provides utilities to access and manage UserPreferences.
 */
object PreferenceUtil {

    fun setPreference(key: String, value: Any?) {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        val editor = mySharedPrefs.edit()
        if (value is String) {
            editor.putString(key, value)
        } else if (value is Int) {
            editor.putInt(key, value)
        } else if (value is Boolean) {
            editor.putBoolean(key, value)
        } else if (value is Long) {
            editor.putLong(key, value)
        }
        editor.commit()
    }

    @JvmOverloads
    fun getStringPreference(key: String, defaultValue: String? = null): String? {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        return mySharedPrefs.getString(key, defaultValue)
    }

    @JvmOverloads
    fun getIntPreference(key: String, defaultValue: Int = -1): Int {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        return mySharedPrefs.getInt(key, defaultValue)
    }

    @JvmOverloads
    fun getBooleanPreference(key: String, defaultValue: Boolean = false): Boolean {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        return mySharedPrefs.getBoolean(key, defaultValue)
    }

    @JvmOverloads
    fun getLongPreference(key: String, defaultValue: Long = 0): Long {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        return mySharedPrefs.getLong(key, defaultValue)
    }

    @JvmOverloads
    fun containsPreference(key: String): Boolean {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        return mySharedPrefs.contains(key)
    }

    @JvmOverloads
    fun removePreference(key: String): Boolean {
        val mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(PRApplication.instance!!.applicationContext)
        val editor = mySharedPrefs.edit()
        editor.remove(key)
        return editor.commit()
    }

}