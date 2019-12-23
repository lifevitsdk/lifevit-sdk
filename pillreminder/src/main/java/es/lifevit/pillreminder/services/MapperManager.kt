package es.lifevit.pillreminder.services

import android.util.Log
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.pillreminder.model.PRMedication
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat


/**
 * Provides mapping between JSON REST responses and model classes.
 */
object MapperManager {

    private val TAG = MapperManager::class.java.simpleName


    fun getToken(json: JSONObject): String {
        var result = ""
        try {
            result = json.get(AppConstants.JSON_TOKEN) as String
        } catch (e: Exception) {
            Log.e(TAG, "Error getToken: " + e.message)
            e.printStackTrace()
        }

        return result
    }


    fun getUserId(json: JSONObject): Int {
        var result = -1
        try {
            val userJson = json.get(AppConstants.JSON_USER) as JSONObject
            val uidString = userJson.get(AppConstants.JSON_UID) as String
            result = uidString.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error getUserId: " + e.message)
            e.printStackTrace()
        }

        return result
    }


    fun getPrescriptions(jArray: JSONArray): ArrayList<PRMedication> {
        val users: ArrayList<PRMedication> = ArrayList()

        try {

            for (i in 0 until jArray.length()) {
                val user = getPrescription(jArray.getJSONObject(i))
                users.add(user)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getPrescriptions: " + e.message)
            e.printStackTrace()
        }

        return users
    }


    private fun getPrescription(json: JSONObject): PRMedication {
        val user = PRMedication()
        val dataJson = json.get(AppConstants.JSON_DATA) as JSONObject
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd")

        try {
            for (key in dataJson.keys()) {
                try {
                    val oneDataJson = dataJson.get(key) as JSONObject
                    when (oneDataJson.getString(AppConstants.JSON_FORM_KEY)) {
                        AppConstants.JSON_PATIENT ->
                            user.patientId = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getInt(0)
                        AppConstants.JSON_MEDICATION ->
                            user.medicationId = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getInt(0)
                        AppConstants.JSON_COLOR ->
                            user.color = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getInt(0)
                        AppConstants.JSON_QUANTITY ->
                            user.quantity = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getInt(0)
                        AppConstants.JSON_INDICATIONS ->
                            user.indications = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getString(0)
                        AppConstants.JSON_TAKE_PILL_TIME -> {
                            // 08:00:00
                            val valueString = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getString(0)
                            val tokens = valueString.split(":")
                            if (tokens.size >= 2) {
                                user.startTimeInMinutes = tokens[0].toInt() * 60 + tokens[1].toInt()
                            }
                        }
                        AppConstants.JSON_START_DATE -> {
                            // 2019-11-20
                            val valueString = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getString(0)
                            user.startDate = dateFormatter.parse(valueString).time
                        }
                        AppConstants.JSON_END_DATE -> {
                            val valueString = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getString(0)
                            user.endDate = dateFormatter.parse(valueString).time
                        }
                        AppConstants.JSON_REPETITION_PATTERN ->
                            user.repeatPattern = oneDataJson.getJSONArray(AppConstants.JSON_VALUES).getInt(0)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getPrescription IN ONE DATA: " + e.message)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getPrescription: " + e.message)
            e.printStackTrace()
        }

        return user
    }


}
