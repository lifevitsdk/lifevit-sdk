package es.lifevit.pillreminder.views.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import es.lifevit.pillreminder.R

/**
 * Created by inmovens on 9/5/18.
 */
abstract class BaseAppCompatActivity : AppCompatActivity() {

    private val TAG = BaseAppCompatActivity::class.java.simpleName


    interface RequestAcceptListener {
        fun onRequestAccepted(accepted: Boolean, isUserAction: Boolean)
    }

    private var requestAcceptListener: RequestAcceptListener? = null
    protected var isLocked: Boolean = false


    fun showError(message: String) {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.dialog_error_title))
            builder.setMessage(message)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            if (!this.isFinishing) {
                val dialog = builder.create()
                dialog.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not show error")
            e.printStackTrace()
        }
    }


    fun showMessage(title: String?, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        if (title != null) {
            builder.setTitle(title)
        }
        builder.setMessage(message)
        // Mostrar mensaje
        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        if (!this.isFinishing) {
            builder.show()
        }
    }


    fun showConfirmMessage(message: String, buttonOk: String, cancelButton: Boolean, okListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setMessage(message)
        builder.setPositiveButton(buttonOk, okListener)
        builder.setCancelable(false)

        if (cancelButton) {
            builder.setNegativeButton(getString(android.R.string.cancel)) { dialogInterface, _ -> dialogInterface.cancel() }
        }

        //2. now setup to change color of the button
        val dialog = builder.create()

        dialog.setOnShowListener {
            val color = ContextCompat.getColor(this@BaseAppCompatActivity, R.color.colorPrimary)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(color)
        }
        dialog.show()
    }


    @SuppressLint("NewApi")
    fun requestPermission(main: View, permission: String, requestCode: Int, errorMessage: Int, indefinite: Boolean, listener: RequestAcceptListener) {
        requestAcceptListener = listener

        if (Build.VERSION.SDK_INT < 23 || ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            requestAcceptListener!!.onRequestAccepted(true, false)
        } else {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(permission)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showPermissionDenied(main, resources.getString(errorMessage), indefinite)

                requestAcceptListener!!.onRequestAccepted(false, false)
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(arrayOf(permission), requestCode)
            }
        }
    }


    fun requestPermission(main: View, permission: String, requestCode: Int, errorMessage: Int, listener: RequestAcceptListener) {
        requestPermission(main, permission, requestCode, errorMessage, false, listener)
    }


    /**
     * Request for multiple permissions
     */
    @SuppressLint("NewApi")
    fun requestPermissions(main: View, permissions: Array<String>, requestCode: Int, message: Int, indefinite: Boolean, listener: RequestAcceptListener) {

        requestAcceptListener = listener

        var allGranted = true
        for (permission in permissions) {
            allGranted = allGranted && ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (Build.VERSION.SDK_INT < 23 || allGranted) {
            requestAcceptListener!!.onRequestAccepted(true, false)
        } else {
            var shouldRequestPermission = true
            for (permission in permissions) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(permission)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    showPermissionDenied(main, resources.getString(message), indefinite)
                    requestAcceptListener!!.onRequestAccepted(false, false)
                    shouldRequestPermission = false
                    break
                }
            }
            if (shouldRequestPermission) {
                // No explanation needed, we can request the permission.
                requestPermissions(permissions, requestCode)
            }
        }
    }


    // esta classe mostra la snack bar amb un missatge, fer que cada cop que faci falta es cridi d'aquí
    private fun showPermissionDenied(main: View, message: String, indefinite: Boolean) {
        try {
            val snackbar = Snackbar.make(main, message, if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG)
            snackbar.setAction(resources.getString(R.string.settings)) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            snackbar.show()
        } catch (e: Exception) {
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            e.printStackTrace()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestAcceptListener != null) {
                requestAcceptListener!!.onRequestAccepted(true, true)
            }
        } else {
            if (requestAcceptListener != null) {
                requestAcceptListener!!.onRequestAccepted(false, true)
            }
        }
    }


    fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

//    fun lockView(lock: Boolean) {
//        showProgress(lock)
//    }
//
//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    open fun showProgress(show: Boolean) {
//        showProgress(show, null)
//    }
//
//
//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @SuppressLint("ObsoleteSdkInt")
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    open fun showProgress(show: Boolean, message: String?) {
//        isLocked = show
//        val mProgressView = findViewById<RelativeLayout>(R.id.progressBar)
//        if (mProgressView != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//                val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
//
//                mProgressView.visibility = if (show) View.VISIBLE else View.GONE
//                mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
//                        (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        mProgressView.visibility = if (show) View.VISIBLE else View.GONE
//                        mProgressView.requestFocus()
//                    }
//                })
//            } else {
//                mProgressView.visibility = if (show) View.VISIBLE else View.GONE
//                mProgressView.requestFocus()
//            }
//
//            try {
//                // Visibility of text
//                progressBarText.visibility = if (message == null || message.isEmpty()) View.INVISIBLE else View.VISIBLE
//
//                // Text's message
//                val mProgressBarText = findViewById<TextView>(R.id.progressBarText)
//                if (message != null && mProgressBarText != null) {
//                    mProgressBarText.text = message
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }


}

