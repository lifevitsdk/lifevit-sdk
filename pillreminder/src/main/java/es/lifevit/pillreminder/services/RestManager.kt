package es.lifevit.pillreminder.services

import android.util.Log
import android.webkit.MimeTypeMap
import es.lifevit.pillreminder.PRApplication
import es.lifevit.pillreminder.R
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.pillreminder.model.ObjectResponse
import es.lifevit.pillreminder.utils.LogUtil
import es.lifevit.pillreminder.utils.PreferenceUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.TimeoutException


/**
 * Implements REST calls.
 */
object RestManager {

    private val TAG = RestManager::class.java.simpleName

    private val crlf = "\r\n"
    private val twoHyphens = "--"
    private val boundary = "*****"


    // region --- HTTP Request methods ---


    /**
     * Standard GET Request
     *
     * @param method     REST Method
     * @param parameters Arraylist with key/value objects.
     * @return String with Rest Result
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getRequest(method: String, parameters: HashMap<String, String>): ObjectResponse? {
        try {
            var url = String.format("%s/%s", PRApplication.instance!!.applicationContext.getString(R.string.URL_REST_SERVER), method)
            if (parameters != null) {
                var first = true
                for (param in parameters) {
                    if (first) {
                        url = String.format("%s/?%s", url, param.key + "=" + param.value)
                        first = false
                    } else {
                        url = String.format("%s&%s", url, param.key + "=" + param.value)
                    }
                }
            }

            return executeGetRequest(url)
        } catch (out: OutOfMemoryError) {
            Log.e(TAG + RestManager::class.java.toString(), "OutOfMemoryError: " + out.message)
            out.printStackTrace()
        }

        return null
    }


    /**
     * Standard GET Request
     *
     * @return String with Rest Result
     * @throws Exception ,TimeoutException
     */
    @Throws(Exception::class)
    private fun executeGetRequest(path: String): ObjectResponse? {
        try {
            LogUtil.d(TAG, "[getRequest] Request: $path")

            val url = URL(path.replace(" ", "%20"))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = AppConstants.REST_TIMEOUT
            conn.readTimeout = AppConstants.REST_TIMEOUT

            val token = getAccesToken()
            if (token.isNotEmpty()) {
                conn.setRequestProperty(AppConstants.REST_HEADER_TOKEN, token)
                LogUtil.d(TAG, "[getRequest] GET Header: ${AppConstants.REST_HEADER_TOKEN} = $token")
            }

            conn.connect()

            try {
                LogUtil.d(TAG, "[getRequest] GET Request: $path")
                LogUtil.d(TAG, "[getRequest] Response Code: " + conn.responseCode)

                val inputStream = if (conn.responseCode / 100 == 2) conn.inputStream else conn.errorStream

                val `in` = BufferedInputStream(inputStream)
                val respStr = readInputStream(`in`)

                LogUtil.d(TAG, "[executeGetRequest] Response from get: $respStr")

                return ObjectResponse(conn.responseCode, respStr)

            } catch (ex: Exception) {
                Log.e(TAG + RestManager::class.java.toString(), "Error: " + ex.message)
                ex.printStackTrace()
                throw ex
            }

        } catch (out: OutOfMemoryError) {
            Log.e(TAG + RestManager::class.java.toString(), "consultaString OutOfMemoryError: " + out.message)
            out.printStackTrace()
        }

        return null
    }

    /**
     * Standard POST Request
     *
     * @param method REST Method
     * @return String response
     */
    @Throws(IOException::class, JSONException::class)
    private fun postRequest(method: String, parameters: HashMap<*, *>): ObjectResponse? {
        try {

            val url = URL(String.format("%s/%s", PRApplication.instance!!.applicationContext.getString(R.string.URL_REST_SERVER), method))

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            val postParams: String
            //conn.setRequestProperty("content-mType", "application/json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = AppConstants.REST_TIMEOUT
            conn.readTimeout = AppConstants.REST_TIMEOUT

            val token = getAccesToken()
            if (token.isNotEmpty()) {
                conn.setRequestProperty(AppConstants.REST_HEADER_TOKEN, token)
                LogUtil.d(TAG, "[postRequest] POST Header: ${AppConstants.REST_HEADER_TOKEN} = $token")
            }

            val dato = JSONObject()
            for (key in parameters.keys) {
                val obj = parameters[key]
                dato.put(key as String, obj)
            }

            LogUtil.d(TAG, "[postRequest] POST Request: $method")
            LogUtil.d(TAG, "[postRequest] POST Params: $dato")

            postParams = dato.toString()

            // For POST only - BEGIN
            conn.doOutput = true
            val os = conn.outputStream
            os.write(postParams.toByteArray())
            os.flush()
            os.close()
            // For POST only - END

            try {
                LogUtil.d(TAG, "[postRequest] Response Code: " + conn.responseCode)

                var `in`: InputStream
                if (conn.responseCode / 100 == 2) {
                    `in` = conn.inputStream
                } else {
                    `in` = conn.errorStream
                }

                `in` = BufferedInputStream(`in`)
                val respStr = readInputStream(`in`)

                LogUtil.d(TAG, "[postRequest] Response from post: $respStr")

                return ObjectResponse(conn.responseCode, respStr)

            } catch (ex: Exception) {
                Log.e(TAG + RestManager::class.java.toString(), "Error: " + ex.message)
                ex.printStackTrace()
                throw ex
            }

        } catch (e: Exception) {
            Log.e(TAG + RestManager::class.java.toString(), "postRequest JSONException: " + e.message)
            e.printStackTrace()
            throw e
        }
    }


    @Throws(TimeoutException::class)
    private fun postMultipartRequest(method: String, parameters: HashMap<*, *>): ObjectResponse? {
        try {

            val url = URL(String.format("%s/%s", PRApplication.instance!!.applicationContext.getString(R.string.URL_REST_SERVER), method))

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = AppConstants.REST_TIMEOUT
            conn.readTimeout = AppConstants.REST_TIMEOUT

            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Cache-Control", "no-cache")
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            val token = getAccesToken()
            if (token.isNotEmpty()) {
                conn.setRequestProperty(AppConstants.REST_HEADER_TOKEN, token)
                LogUtil.d(TAG, "[postMultipartRequest] POST MULTIPART Header: ${AppConstants.REST_HEADER_TOKEN} = $token")
            }

            LogUtil.d(TAG, "[postMultipartRequest] POST MULTIPART Request: $method")

            conn.doOutput = true
            val request = DataOutputStream(conn.outputStream)
            request.writeBytes(twoHyphens + boundary + crlf)

            for ((i, key) in parameters.keys.withIndex()) {
                val obj = parameters[key]

                if (obj is String) {

                    LogUtil.d(TAG, "[postMultipartRequest] POST MULTIPART String Param: [$key] Value: $obj")

                    request.writeBytes("Content-Disposition: form-data; name=\"$key\"$crlf$crlf")

                    if (i != parameters.size - 1) {
                        request.writeBytes(obj + crlf)
                        request.writeBytes(twoHyphens + boundary + crlf)
                    } else {
                        request.writeBytes(obj.toString())
                    }

                } else if (obj is File) {

                    LogUtil.d(TAG, "[postMultipartRequest] POST MULTIPART File Param: [$key]")

                    var contentType = "application/octet-stream"
                    val extension = MimeTypeMap.getFileExtensionFromUrl(obj.name)
                    if (!extension.isNullOrEmpty()) {
                        try {
                            val fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                            if (!fileMimeType.isNullOrEmpty()) {
                                contentType = fileMimeType
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    request.writeBytes("Content-Type: $contentType$crlf")
                    request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + obj.name + "\"" + crlf + crlf)

                    val inputStream = FileInputStream(obj)
                    val buffer = ByteArray(65665)

                    var length: Int = inputStream.read(buffer)

                    while (length != -1) {
                        request.write(buffer, 0, length)
                        length = inputStream.read(buffer)
                    }

                    inputStream.close()
                    if (i != parameters.size - 1) {
                        request.writeBytes(crlf)
                        request.writeBytes(twoHyphens + boundary + crlf)
                    }
                }
            }

            request.writeBytes(crlf)
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf)

            request.flush()
            request.close()

            try {
                LogUtil.d(TAG, "[postMultipartRequest] Response Code: " + conn.responseCode)

                var `in`: InputStream = if (conn.responseCode / 100 == 2) {
                    conn.inputStream
                } else {
                    conn.errorStream
                }

                `in` = BufferedInputStream(`in`)
                val respStr = readInputStream(`in`)

                LogUtil.d(TAG, "[postMultipartRequest] Response from post: $respStr")

                return ObjectResponse(conn.responseCode, respStr)

            } catch (sOut: SocketTimeoutException) {
                throw TimeoutException("Timeout")
            } catch (ex: Exception) {
                Log.e(TAG + RestManager::class.java.toString(), "Error: " + ex.message)
                ex.printStackTrace()
            }

        } catch (out: OutOfMemoryError) {
            Log.e(TAG + RestManager::class.java.toString(), "postRequest OutOfMemoryError: " + out.message)
            out.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG + RestManager::class.java.toString(),
                    "postRequest UnsupportedEncodingException: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG + RestManager::class.java.toString(), "postRequest JSONException: " + e.message)
            e.printStackTrace()
        }

        return null
    }


    // endregion --- HTTP Request methods ---


    // region --- Private helper methods ---


    private fun readInputStream(input: InputStream): String {
        var respStr: String
        try {
            respStr = input.bufferedReader().use { it.readText() }  // defaults to UTF-8
        } catch (e: Exception) {
            respStr = ""
            LogUtil.d(TAG, "[postRequest] Error during conversion to String")
            e.printStackTrace()
        }

        return respStr
    }


    private fun getAccesToken(): String {
        return PreferenceUtil.getStringPreference(AppConstants.PREF_OAUTH_ACCESS_TOKEN, "")!!
    }


    // endregion --- Private helper methods ---


    // region User


    fun getToken(): ObjectResponse {

        var result: ObjectResponse

        try {
            val al = HashMap<Any, Any>()

            result = postRequest(AppConstants.REST_URL_TOKEN, al)
                    ?: ObjectResponse(AppConstants.ERROR_UNKNOWN, ObjectResponse.getSimpleErrorResult())

            if (result.isResponseOk) {
                val resultStr = result.`object` as String
                val token = MapperManager.getToken(JSONObject(resultStr))
                result.`object` = token
                PreferenceUtil.setPreference(AppConstants.PREF_OAUTH_ACCESS_TOKEN, token)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error WS getToken: " + e.message)
            e.printStackTrace()
            result = ObjectResponse(AppConstants.ERROR_EXCEPTION, e)
        }

        return result
    }


    fun login(username: String, password: String): ObjectResponse {

        var result: ObjectResponse

        try {
            val al = HashMap<Any, Any>()
            al[AppConstants.REST_PARAM_USERNAME] = username
            al[AppConstants.REST_PARAM_PASSWORD] = password

            result = postRequest(AppConstants.REST_URL_LOGIN, al)
                    ?: ObjectResponse(AppConstants.ERROR_UNKNOWN, ObjectResponse.getSimpleErrorResult())

            if (result.isResponseOk) {
                val resultStr = result.`object` as String
                val userId = MapperManager.getUserId(JSONObject(resultStr))
                val token = MapperManager.getToken(JSONObject(resultStr))
                result.`object` = userId
                PreferenceUtil.setPreference(AppConstants.PREF_USER_LOGGED_ID, userId)
                PreferenceUtil.setPreference(AppConstants.PREF_OAUTH_ACCESS_TOKEN, token)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error WS login: " + e.message)
            e.printStackTrace()
            result = ObjectResponse(AppConstants.ERROR_EXCEPTION, e)
        }

        return result
    }


    fun logout(): ObjectResponse {

        var result: ObjectResponse

        try {
            val al = HashMap<Any, Any>()

            result = postRequest(AppConstants.REST_URL_LOGOUT, al)
                    ?: ObjectResponse(AppConstants.ERROR_UNKNOWN, ObjectResponse.getSimpleErrorResult())

//            if (result.isResponseOk) {

            // TODO: Pasar del resultado?

            // Reset things
            PreferenceUtil.setPreference(AppConstants.PREF_OAUTH_ACCESS_TOKEN, "")
            PreferenceUtil.setPreference(AppConstants.PREF_USER_LOGGED_ID, -1)
//            }

        } catch (e: Exception) {
            Log.e(TAG, "Error WS logout: " + e.message)
            e.printStackTrace()
            result = ObjectResponse(AppConstants.ERROR_EXCEPTION, e)
        }

        return result
    }


    // endregion User


    // region Prescriptions

    fun getPrescriptions(): ObjectResponse {

        var result: ObjectResponse

        try {
            result = getRequest(AppConstants.REST_URL_GET_PRESCRIPTIONS, HashMap())
                    ?: ObjectResponse(AppConstants.ERROR_UNKNOWN, ObjectResponse.getSimpleErrorResult())

            if (result.isResponseOk) {
                val resultStr = result.`object` as String
                result.`object` = MapperManager.getPrescriptions(JSONArray(resultStr))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error WS getPrescriptions: " + e.message)
            e.printStackTrace()
            result = ObjectResponse(AppConstants.ERROR_EXCEPTION, e)
        }

        return result
    }

    fun setPrescription(medicationId: Int, quantity: Int, tookIt: Boolean): ObjectResponse {

        var result: ObjectResponse

        try {

            val submissionJson = JSONObject()
            val dataJson = JSONObject()

            val data1 = JSONObject()
            val data1array = JSONArray()
            data1array.put(medicationId.toString())
            data1.put(AppConstants.JSON_VALUES, data1array)
            dataJson.put("1", data1)

            val data2 = JSONObject()
            val data2array = JSONArray()
            data2array.put(quantity.toString())
            data2.put(AppConstants.JSON_VALUES, data2array)
            dataJson.put("2", data2)

            val data3 = JSONObject()
            val data3array = JSONArray()
            data3array.put(if (tookIt) {
                "1"
            } else {
                "0"
            })
            data3.put(AppConstants.JSON_VALUES, data3array)
            dataJson.put("3", data3)

            submissionJson.put(AppConstants.JSON_DATA, dataJson)

            val al = HashMap<Any, Any>()
            al[AppConstants.REST_PARAM_WEBFORM] = "0b7293d8-6cdd-4f2f-a2ba-7c8f202dc2cf"
            al[AppConstants.REST_PARAM_SUBMISSION] = submissionJson

            result = postRequest(AppConstants.REST_URL_SET_PRESCRIPTIONS, al)
                    ?: ObjectResponse(AppConstants.ERROR_UNKNOWN, ObjectResponse.getSimpleErrorResult())

//            if (result.isResponseOk) {
//                result.`object` = MapperManager.getPrescriptions(result.`object` as JSONArray)
//            }

        } catch (e: Exception) {
            Log.e(TAG, "Error WS getPrescriptions: " + e.message)
            e.printStackTrace()
            result = ObjectResponse(AppConstants.ERROR_EXCEPTION, e)
        }

        return result
    }


    // endregion Prescriptions


}
