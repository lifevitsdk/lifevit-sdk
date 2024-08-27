package es.lifevit.sdk.newconnection.nordic

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.le.ScanSettings.PHY_LE_ALL_SUPPORTED
import android.content.Context
import android.os.StrictMode
import android.text.TextUtils
import es.lifevit.sdk.BuildConfig
import es.lifevit.sdk.LifevitSDKConstants
import es.lifevit.sdk.LifevitSDKManager
import es.lifevit.sdk.LifevitSDKManager.Aoj20fConnectionListener
import es.lifevit.sdk.utils.HexUtils
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings


open class Aoj20fBleManager(context: Context) : BleManager(context) {

    companion object {
        private var mGatt: BluetoothGatt? = null
        var mMacToConnect = ""
        private lateinit var mListener: NordicListener
        private var scanResults: MutableList<ScanResult> = mutableListOf()

        var DEVICE_NAME = "AOJ-20F"
        object Attributes {
            var MEASUREMENT_DATA = "0000ffe1"
        }
    }

    protected var gattCharacteristics: MutableMap<String, BluetoothGattCharacteristic> = hashMapOf()
    private var mDeviceConnected = false
    private var mScanner = BluetoothLeScannerCompat.getScanner()
    private var mScannerCallback: ScanCallback = scannerCallback(null, "")

    interface ReadCharacteristicListener {
        fun onReaded(data: String)
        fun onFailed()
    }

    interface NotifiedCharacteristicListener {
        fun onNotified(data: String)
    }

    interface WriteCharacteristicListener {
        fun onWrited()
        fun onFailed()
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    interface NordicListener {
        fun bleConnectionEstablished()
        fun isConnected(bledevice: BluetoothDevice?, isConnected: Boolean)
        fun onNotified(data: Data)
    }

    interface ScanListener {
        fun newDevice(device: ScanResult?)
    }

    override fun initialize() {
        val measurementData = gattCharacteristics[Attributes.MEASUREMENT_DATA.lowercase()]
        setNotificationCallback(measurementData)
            .with { device, data ->
                mListener.onNotified(data)
            }
        enableNotifications(measurementData).enqueue();
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {
        @SuppressLint("MissingPermission", "NewApi")
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gattCharacteristics.clear()
            val gattServices = gatt.services
            gattServices.forEach { gattService ->
                gattService.characteristics.forEach {
                    gattCharacteristics[getGattKey(it).lowercase()] = it
                }
            }
            mGatt = gatt
            return true
        }

        override fun onServicesInvalidated() { }

        override fun onDeviceDisconnected() {
            mListener.isConnected(null, false)
        }
    }

    fun getGattKey(gattCharacteristic: BluetoothGattCharacteristic): String {
        return gattCharacteristic.uuid.toString().substring(0, 8)
    }

    @SuppressLint("MissingPermission")
    fun closeConnection() {
        mGatt?.disconnect()
    }

    private fun turnOnStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath().build())
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath().build())
        }
    }

    private fun permitDiskReads(func: () -> Any) : Any {
        return if (BuildConfig.DEBUG) {
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
                    .permitDiskReads().build())
            val anyValue = func()
            StrictMode.setThreadPolicy(oldThreadPolicy)

            anyValue
        } else {
            func()
        }
    }

    fun scan(deviceName: String, listener: ScanListener) {
        turnOnStrictMode()
        permitDiskReads {
            val settings: ScanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(100L)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setUseHardwareBatchingIfSupported(true)
                .setPhy(PHY_LE_ALL_SUPPORTED)
                .build()

            mScanner.startScan(arrayListOf(), settings, scannerCallback(listener, deviceName))
        }
    }

    fun stopScan() {
        mScanner.stopScan(mScannerCallback)
    }

    fun connectTo(device: BluetoothDevice, listener: NordicListener) {
        mMacToConnect = device.address
        mListener = listener
        NordicConnectionObserver(this@Aoj20fBleManager, mListener).connect(device)
    }

    @SuppressLint("MissingPermission")
    private fun scannerCallback(listener: ScanListener? = null, deviceName: String): ScanCallback {
        mScannerCallback = object : ScanCallback() {

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                super.onBatchScanResults(results)
                for(result in results) {
                    if(result.scanRecord?.deviceName == deviceName) {
                        addDeviceToResultsList(result, listener)
                    }
                }
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                if(result.device.name == deviceName) {
                    addDeviceToResultsList(result, listener)
                }
            }
        }
        return mScannerCallback
    }

    private fun addDeviceToResultsList(result: ScanResult, listener: ScanListener? = null) {
        result.device.address?.let { newMac ->
            val existingItem = scanResults.find { newMac == it.device.address }
            if (existingItem == null) {
                scanResults.remove(scanResults.find { newMac == it.device.address })
                scanResults.add(result)
                listener?.newDevice(result)
            }
        }
        if(TextUtils.isEmpty(mMacToConnect)) {
            //-- Not connecting to any mac, just scanning
            listener?.newDevice(result)
        } else if (scanResults.find { mMacToConnect == it.device.address } != null) {
            val device = scanResults.find { mMacToConnect == it.device.address }
            //-- connecting to a mac and is this device
            mMacToConnect = ""
            if(device != null) {
                stopScan()
                listener?.newDevice(device)
            }
        }
    }

    @Suppress("unused")
    protected fun readChar(char: String, listener: ReadCharacteristicListener) {
        readCharacteristic(gattCharacteristics[char.lowercase()])
            .fail { _, _ ->
                listener.onFailed()
            }.with { _, data ->
                run { data.value?.let { listener.onReaded(it.decodeToString()) }
                }
            }.enqueue()
    }

    @Suppress("unused")
    protected fun writeChar(char: String, data: ByteArray, listener: WriteCharacteristicListener) {
        val gattCharacteristic = gattCharacteristics[char.lowercase()]
        gattCharacteristic?.value = data
        writeCharacteristic(gattCharacteristic, gattCharacteristic?.value, WRITE_TYPE_DEFAULT)
            .fail { _, _ ->
                listener.onFailed()
                closeConnection()
            }
            .with { _, it ->
                it.value?.let {
                    listener.onWrited()
                }
            }.enqueue()
    }

    fun isDeviceConnected(): Boolean {
        return mDeviceConnected
    }

    fun disconnectAllDevices() {
        closeConnection()
    }

    fun connectToThermomether(listener: Aoj20fConnectionListener) {
        listener.statusChanged(LifevitSDKConstants.STATUS_CONNECTING)
        scan(DEVICE_NAME, object : ScanListener {
            override fun newDevice(device: ScanResult?) {
                stopScan()
                device?.let { dev ->
                    connectTo(dev.device, object : NordicListener {
                        override fun bleConnectionEstablished() {}

                        override fun isConnected(bledevice: BluetoothDevice?, isConnected: Boolean) {
                            if(isConnected) {
                                listener.statusChanged(LifevitSDKConstants.STATUS_CONNECTED)
                            } else {
                                listener.statusChanged(LifevitSDKConstants.STATUS_DISCONNECTED)
                            }
                        }

                        override fun onNotified(data: Data) {
                            val result = HexUtils.getStringToPrint(data.value).replace(":","")

                            if(result.length == 16) {
                                val temperature = result.substring(8, 12).toLong(radix = 16)/100.0
                                val mode = result.substring(12, 14).toInt()
                                listener.onMeasurementTaken(temperature, LifevitSDKManager.ThermometerModes.entries.find { it.value == mode })
                            }
                        }
                    })
                }
            }
        })
    }
}