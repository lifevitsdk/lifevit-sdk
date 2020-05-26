package es.lifevit.pillreminder.views.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import es.lifevit.pillreminder.PRApplication
import es.lifevit.pillreminder.R
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.pillreminder.model.PRMedication
import es.lifevit.pillreminder.services.RestManager
import es.lifevit.pillreminder.utils.PreferenceUtil
import es.lifevit.pillreminder.utils.Utils
import es.lifevit.pillreminder.views.adapters.MedicationsAdapter
import es.lifevit.pillreminder.views.adapters.spacing.SpacesItemDecorationVerticalLinearLayout
import es.lifevit.sdk.LifevitSDKConstants
import es.lifevit.sdk.listeners.LifevitSDKDeviceListener
import es.lifevit.sdk.listeners.LifevitSDKPillReminderListener
import es.lifevit.sdk.pillreminder.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseAppCompatActivity() {

    private val TAG = MainActivity::class.simpleName

    // Listener to manage Bracelet Device
    private var connectionListener: LifevitSDKDeviceListener? = null

    // Adapter for the medications list
    private var medicationsAdapter: MedicationsAdapter? = null
    private var itemDecoration: SpacesItemDecorationVerticalLinearLayout? = null

    // Medications shown in the list
    var medications = arrayListOf<PRMedication>()

    // Counters for alarms and pill takes
    internal var alarmsRequestCounter: Int = 0
    internal var historyRecordsRequestCounter: Int = 0

    // Send alarms to bracelet Only once per connection
    var alreadySent = false

    // Hashes to identify medication when we receive a pill take
    var hashMedicationIdsByDate: HashMap<Long, Int> = hashMapOf()
    var hashMedicationColorsByAlarmNumber: HashMap<Int, Int> = hashMapOf()

    // Refresh medication list periodically
    private var refreshTimer: Timer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initListeners()
        initData()
    }


    override fun onResume() {
        super.onResume()
        initSdk()
    }


    override fun onStop() {

        PRApplication.instance!!.lifevitSDKManager.removeDeviceListener(connectionListener)
        PRApplication.instance!!.lifevitSDKManager.pillReminderListener = null

        super.onStop()
    }


    private fun initListeners() {
        main_activity_old_activity_button.setOnClickListener {
            startActivity(Intent(this@MainActivity, OldPillReminderActivity::class.java))
        }

        main_activity_disconnect_button.setOnClickListener {
            PRApplication.instance!!.lifevitSDKManager.disconnectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER)
        }

        main_activity_logout_button.setOnClickListener {
            doAsync {
                val result = RestManager.logout()
                // TODO: Pasar del resultado?
//                if(result.error == AppConstants.RESPONSE_OK){
                uiThread {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
//                }
            }
        }

    }


    private fun initData() {

        val currentUserId = PreferenceUtil.getIntPreference(AppConstants.PREF_USER_LOGGED_ID, -1)

        // Add one fake medication
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.MINUTE, 1)

        val medication1 = PRMedication(currentUserId, AppConstants.MEDICATION_ENANTYUM, AppConstants.TICARE_COLOR_PURPLE,
                1, "",
                startDate.get(Calendar.HOUR_OF_DAY) * 60 + startDate.get(Calendar.MINUTE),
                startDate.timeInMillis, null,
                AppConstants.PERIODICITY_4H)

        medications.add(medication1)


        // And get medications from server
        doAsync {

            val queryResult = RestManager.getPrescriptions()
            if (queryResult.isResponseOk) {
                medications.addAll(queryResult.`object` as ArrayList<PRMedication>)
                medications.filter { it.patientId == currentUserId }
            }

            uiThread {
                initGUI()
            }

            sendMedications()
        }
    }


    private fun sendMedications() {
        if (PRApplication.instance!!.lifevitSDKManager.isDeviceConnected(LifevitSDKConstants.DEVICE_PILL_REMINDER) && !alreadySent) {

/*
            val alarms = ArrayList<LifevitSDKPillReminderAlarmData>()

            //FIXME: FECHA FIJADA PARA COMPARAR CON LOS VALORES DE iOS
            val now = System.currentTimeMillis()
            //long now = 1559888240000L;

            val a1 = LifevitSDKPillReminderAlarmData()
            a1.date = now + 60 * 1000
            a1.color = LifevitSDKConstants.PILLREMINDER_COLOR_RED
            alarms.add(a1)

            val a2 = LifevitSDKPillReminderAlarmData()
            a2.date = now + 2 * 60 * 1000
            a2.color = LifevitSDKConstants.PILLREMINDER_COLOR_BLUE
            alarms.add(a2)

            val a3 = LifevitSDKPillReminderAlarmData()
            a3.date = now + 4 * 60 * 1000
            a3.color = LifevitSDKConstants.PILLREMINDER_COLOR_GREEN
            alarms.add(a3)

            val a4 = LifevitSDKPillReminderAlarmData()
            a4.date = now + 10 * 60 * 1000
            a4.color = LifevitSDKConstants.PILLREMINDER_COLOR_PURPLE
            alarms.add(a4)

            val a5 = LifevitSDKPillReminderAlarmData()
            a5.date = now + 2 * 60 * 1000
            a5.color = LifevitSDKConstants.PILLREMINDER_COLOR_YELLOW
            alarms.add(a5)

            val a6 = LifevitSDKPillReminderAlarmData()
            a6.date = now + 7 * 60 * 1000
            a6.color = LifevitSDKConstants.PILLREMINDER_COLOR_RED
            alarms.add(a6)



            PRApplication.instance!!.lifevitSDKManager.prSetAlarmSchedule(alarms)
            alreadySent = true
*/


            val manager = PRApplication.instance!!.lifevitSDKManager

            // TODO: Clear was a problem?
            manager.prClearAlarmSchedule()

            // TODO: Just for testing
            manager.prGetDeviceTime()
            manager.prGetDeviceTimeZone()
            manager.prGetBatteryLevel()

            if (!medications.isNullOrEmpty()) {

                hashMedicationIdsByDate.clear()

                // FIXME: Reset fake medication time
                val startDate = Calendar.getInstance()
                startDate.add(Calendar.MINUTE, 1)
                medications[0].startTimeInMinutes = startDate.get(Calendar.HOUR_OF_DAY) * 60 + startDate.get(Calendar.MINUTE)

                setConnectedMessage(getString(R.string.synchronizing_meds))

                val alarms = arrayListOf<LifevitSDKPillReminderAlarmData>()
                for (medication in medications) {

                    // TODO: ERROR: pillReminderOnError: Too many alarms for the same day
                    val medicationTake = medication.getNextMedicationTakes()[0]

                    // TODO: Por si acaso quitamos tomas de mañana...
                    if (Utils.isSameDay(Calendar.getInstance().timeInMillis, medicationTake)) {


//                    for (medicationTake in medication.getNextMedicationTakes()) {
                        val a1 = LifevitSDKPillReminderAlarmData()
                        a1.date = medicationTake
                        a1.color = medication.getLifeVitColor()
                        alarms.add(a1)
                        Log.d(TAG, "Will add medication " + medication.medicationId
                                + " (" + Utils.getMedicationName(this@MainActivity, medication.medicationId)
                                + ") with date " + SimpleDateFormat("dd/MM/yyyy HH:mm").format(a1.date))

                        hashMedicationIdsByDate[a1.date] = medication.medicationId

//                    }
                    }
                }
                manager.prSetAlarmSchedule(alarms)
                alreadySent = true
            }
        }
    }


    private fun initGUI() {

        if (medications.isNullOrEmpty()) {

            main_activity_empty_text.visibility = View.VISIBLE
            main_activity_medication_list_container.visibility = View.GONE

        } else {

            main_activity_empty_text.visibility = View.GONE
            main_activity_medication_list_container.visibility = View.VISIBLE

            // set up the RecyclerView
            val layoutManager = LinearLayoutManager(this@MainActivity)
            layoutManager.orientation = RecyclerView.VERTICAL
            main_activity_medication_recycler.isNestedScrollingEnabled = false
            main_activity_medication_recycler.layoutManager = layoutManager

            if (itemDecoration == null) {
                val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_small)
                val initialAndFinalSpace = resources.getDimensionPixelSize(R.dimen.margin_small)

                itemDecoration = SpacesItemDecorationVerticalLinearLayout(initialAndFinalSpace, spacingInPixels, initialAndFinalSpace)
                main_activity_medication_recycler.addItemDecoration(itemDecoration!!)
            }

            medicationsAdapter = MedicationsAdapter(this@MainActivity, medications)

            main_activity_medication_recycler.adapter = medicationsAdapter

            // Refresh recycler every minute
            if (refreshTimer == null) {
                refreshTimer = Timer()
                refreshTimer!!.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            medicationsAdapter?.notifyDataSetChanged()
                        }
                    }
                }, 60 * 1000, 60 * 1000)
            }
        }
    }


    private fun setWarningMessage(message: String, listener: View.OnClickListener) {

        main_activity_warning_layout.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.warningBackground))

        main_activity_warning_title.text = getString(R.string.warning)
        main_activity_warning_text.text = message

        main_activity_warning_layout.setOnClickListener(listener)

    }


    private fun setConnectedMessage(message: String) {

        main_activity_warning_layout.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.connectedBackground))

        main_activity_warning_title.text = getString(R.string.connected)
        main_activity_warning_text.text = message

        main_activity_warning_layout.setOnClickListener(null)
    }


    private fun setConnectingMessage(message: String) {

        main_activity_warning_layout.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.connectingBackground))

        main_activity_warning_title.text = getString(R.string.connecting)
        main_activity_warning_text.text = message

        main_activity_warning_layout.setOnClickListener {
        }

    }


    private fun initSdk() {

        if (PRApplication.instance!!.lifevitSDKManager.isDeviceConnected(LifevitSDKConstants.DEVICE_PILL_REMINDER)) {
            setConnectedMessage("")
            main_activity_disconnect_button.visibility = View.VISIBLE
        } else {
            setWarningMessage(getString(R.string.not_connected), View.OnClickListener {
                PRApplication.instance!!.lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, AppConstants.SCAN_PERIOD)
            })
            main_activity_disconnect_button.visibility = View.GONE
        }


        // Create connection listener
        connectionListener = object : LifevitSDKDeviceListener {

            override fun deviceOnConnectionError(deviceType: Int, errorCode: Int) {
                if (deviceType != LifevitSDKConstants.DEVICE_PILL_REMINDER) {
                    return
                }
                runOnUiThread {
                    if (errorCode == LifevitSDKConstants.CODE_LOCATION_DISABLED) {
                        // Ask for permissions
                        requestPermission(findViewById(R.id.main_activity_parent_layout), Manifest.permission.ACCESS_FINE_LOCATION,
                                AppConstants.REQUEST_CODE_LOCATION_PERMISSIONS, R.string.enable_location_permission,
                                object : RequestAcceptListener {
                                    override fun onRequestAccepted(accepted: Boolean, isUserAction: Boolean) {
                                        PRApplication.instance!!.lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, AppConstants.SCAN_PERIOD)

                                    }
                                }
                        )
                    } else if (errorCode == LifevitSDKConstants.CODE_BLUETOOTH_DISABLED) {
                        setWarningMessage(getString(R.string.error_enable_bluetooth), View.OnClickListener {
                            // Enable BT
                            val adapter = BluetoothAdapter.getDefaultAdapter()
                            adapter.enable()

                            Handler().postDelayed({
                                PRApplication.instance!!.lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, AppConstants.SCAN_PERIOD)
                            }, 100)

                        })
                    } else if (errorCode == LifevitSDKConstants.CODE_LOCATION_TURN_OFF) {
                        setWarningMessage(getString(R.string.error_turn_on_location), View.OnClickListener {
                            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        })
                    } else {
                        setWarningMessage(getString(R.string.error_unknown), View.OnClickListener {
                            PRApplication.instance!!.lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, AppConstants.SCAN_PERIOD)

                        })
                    }
                }
            }

            override fun deviceOnConnectionChanged(deviceType: Int, status: Int) {
                if (deviceType != LifevitSDKConstants.DEVICE_PILL_REMINDER) {
                    return
                }

                runOnUiThread {
                    when (status) {
                        LifevitSDKConstants.STATUS_DISCONNECTED -> {
                            alreadySent = false
                            setWarningMessage(getString(R.string.not_connected), View.OnClickListener {
                                PRApplication.instance!!.lifevitSDKManager.connectDevice(LifevitSDKConstants.DEVICE_PILL_REMINDER, AppConstants
                                        .SCAN_PERIOD)
                            })
                            main_activity_disconnect_button.visibility = View.GONE
                        }
                        LifevitSDKConstants.STATUS_SCANNING -> {
                            setConnectingMessage(getString(R.string.scanning))
                        }
                        LifevitSDKConstants.STATUS_CONNECTING -> {
                            setConnectingMessage(getString(R.string.connecting_description))
                        }
                        LifevitSDKConstants.STATUS_CONNECTED -> {
                            setConnectedMessage(getString(R.string.synchronizing_meds))
                            main_activity_disconnect_button.visibility = View.VISIBLE
                            sendMedications()
                        }
                    }
                }
            }
        }

        val pillReminderListener = object : LifevitSDKPillReminderListener {
            override fun pillReminderOnResult(info: Any) {
                runOnUiThread {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                    if (info is LifevitSDKPillReminderMessageData) {

                        when (info.request) {
                            LifevitSDKConstants.PILLREMINDER_REQUEST_GET_BATTERYLEVEL -> {
                                Log.d(TAG, "Battery level: " + info.messageText)
                            }
                            LifevitSDKConstants.PILLREMINDER_REQUEST_SET_SUCCESSFULSYNCHRONIZATIONSTATUS -> {
                                setConnectedMessage(getString(R.string.synchronization_successful))
                                PRApplication.instance!!.lifevitSDKManager.prGetAlarmSchedule()
                            }
                            else -> {
                                Log.d(TAG, "Another message: " + info.messageText)
                            }
                        }

                    } else if (info is LifevitSDKPillReminderAlarmListData) {

                        when (info.request) {
                            LifevitSDKConstants.PILLREMINDER_REQUEST_GET_ALARMSCHEDULE -> {

                                if (info.alarmList != null) {
                                    val alarmList = info.alarmList as ArrayList<LifevitSDKPillReminderAlarmData>
                                    hashMedicationColorsByAlarmNumber.clear()
                                    var i = 1
                                    for (alarm in alarmList) {
                                        val d = Date(alarm.date)
                                        Log.d(TAG, "--- " + dateFormat.format(d) + ", color: " + alarm.color)
                                        hashMedicationColorsByAlarmNumber[i] = alarm.color
                                        i++
                                    }
                                    alarmsRequestCounter = alarmList.size
                                    setConnectedMessage(getString(R.string.synchronized_meds, alarmsRequestCounter))

                                } else {
                                    alarmsRequestCounter = 0
                                }
                                Log.d(TAG, "Alarm schedule GET request successful: " + alarmsRequestCounter)

                            }

                            LifevitSDKConstants.PILLREMINDER_REQUEST_GET_SCHEDULEPERFORMANCEHISTORY -> {

                                if (historyRecordsRequestCounter == 0) {
                                    Log.d(TAG, "Schedule performance history request successful:")
                                }

                                if (info.alarmList != null) {
                                    val records = info.alarmList as ArrayList<LifevitSDKPillReminderPerformanceData>
                                    for (record in records) {
                                        val d = Date(record.date)
                                        val dTaken = Date(record.date)
                                        Log.d(TAG, "--- " + dateFormat.format(d) + " - status: " + record.statusTaken + " (" + dateFormat.format(dTaken) + ")")
                                    }
                                } else {
                                    historyRecordsRequestCounter = 0
                                }
                            }
                        }

                    } else if (info is LifevitSDKPillReminderPerformanceData) {

                        Log.d(TAG, "Real-time performance received:")
                        val d = Date(info.date)
                        Log.d(TAG, "--- " + dateFormat.format(d) + " - status: " + info.statusTaken + " - alarm number: " + info.alarmNumber)

                        if (info.statusTaken == LifevitSDKConstants.PILLREMINDER_STATUS_TAKEN_RESPONDED_SECOND_ALARM
                                || info.statusTaken == LifevitSDKConstants.PILLREMINDER_STATUS_TAKEN_IGNORED_SECOND_ALARM) {

                            val medicationIdByCurrentDate = hashMedicationIdsByDate[info.date]
                            val colorByCurrentAlarmNumber = hashMedicationColorsByAlarmNumber[info.alarmNumber]

                            Log.d(TAG, "   --- medicationIdByCurrentDate: " + medicationIdByCurrentDate)
                            Log.d(TAG, "   --- medicationIdByCurrentAlarmNumber: " + colorByCurrentAlarmNumber)

                            var medicationFound: PRMedication? = null

                            if (medicationIdByCurrentDate != null && colorByCurrentAlarmNumber != null) {
                                for (medication in medications) {
                                    if (medication.medicationId == medicationIdByCurrentDate && medication.getLifeVitColor() == colorByCurrentAlarmNumber) {
                                        medicationFound = medication
                                        break
                                    }
                                }
                            }

                            if (medicationFound != null) {

                                val quantity = medicationFound.quantity
                                        ?: 1

                                val tookIt = info.statusTaken == LifevitSDKConstants.PILLREMINDER_STATUS_TAKEN_RESPONDED_SECOND_ALARM

                                doAsync {
                                    val sendingResult = RestManager.setPrescription(medicationFound.medicationId, quantity, tookIt)
                                    if (sendingResult.isResponseOk) {
                                        uiThread {
                                            Toast.makeText(this@MainActivity, getString(R.string.sent_correctly), Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }

                    } else if (info is LifevitSDKPillReminderData) {

                        val d = Date(info.date)
                        when (info.request) {
                            LifevitSDKConstants.PILLREMINDER_REQUEST_GET_DEVICETIME -> {
                                Log.d(TAG, "Device date: " + dateFormat.format(d))
                            }
                            LifevitSDKConstants.PILLREMINDER_REQUEST_GET_LATESTSYNCHRONIZATIONTIME -> {
                                val dl = info.date
                                if (dl != null) {
                                    Log.d(TAG, "Latest synchronization time: " + dateFormat.format(d))
                                } else {
                                    Log.d(TAG, "Device not synchronized")
                                }
                            }
                        }
                    }
                }
            }

            override fun pillReminderOnError(info: LifevitSDKPillReminderMessageData) {

                runOnUiThread {
                    Log.e(TAG, "pillReminderOnError: " + info.messageText)
                }
            }

        }

        // Create connection helper
        PRApplication.instance!!.lifevitSDKManager.addDeviceListener(connectionListener)
        PRApplication.instance!!.lifevitSDKManager.pillReminderListener = pillReminderListener
    }


}
