package es.lifevit.pillreminder.views.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import es.lifevit.pillreminder.R
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.pillreminder.services.RestManager
import es.lifevit.pillreminder.utils.PreferenceUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class LoginActivity : BaseAppCompatActivity() {

    private val TAG = LoginActivity::class.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val currentUserId = PreferenceUtil.getIntPreference(AppConstants.PREF_USER_LOGGED_ID, -1)
        if (currentUserId != -1) {

            startActivity(Intent(this@LoginActivity, MainActivity::class.java))

        } else {
            initListeners()
            initGUI()
        }
    }


    private fun initListeners() {
        login_activity_enter_button.setOnClickListener {

            // Reset errors
            login_activity_user_edittext.error = null
            login_activity_passwords_edittext.error = null

            // Get texts
            val usernameText = login_activity_user_edittext.text.toString()
            val passwordText = login_activity_passwords_edittext.text.toString()

            // Check errors
            var cancel = false
            var focusView: View? = null

            if (TextUtils.isEmpty(usernameText)) {
                login_activity_user_edittext.error = getString(R.string.error_empty_field)
                focusView = login_activity_user_edittext
                cancel = true
            }

            if (TextUtils.isEmpty(passwordText)) {
                login_activity_passwords_edittext.error = getString(R.string.error_empty_field)
                focusView = login_activity_passwords_edittext
                cancel = true
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView?.requestFocus()
            } else {

                doAsync {
                    val tokenResult = RestManager.getToken()
                    if (tokenResult.isResponseOk) {
                        val loginResult = RestManager.login(usernameText, passwordText)
                        uiThread {
                            if (loginResult.isResponseOk) {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            } else {
                                if (loginResult.error == AppConstants.RESPONSE_ERROR_UNAUTHORIZED) {
                                    showError(getString(R.string.login_error_unauthorized))
                                } else {
                                    showError(getString(R.string.login_error))
                                }
                                Log.e(TAG, "Error haciendo login")
                            }
                        }
                    } else {
                        uiThread {
                            showError(getString(R.string.login_error))
                            Log.e(TAG, "Error obteniendo token")
                        }
                    }
                }
            }
        }
    }


    private fun initGUI() {

    }

}
