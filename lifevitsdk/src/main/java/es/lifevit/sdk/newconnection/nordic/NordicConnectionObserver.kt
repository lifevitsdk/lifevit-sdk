package es.lifevit.sdk.newconnection.nordic

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.observer.ConnectionObserver

class NordicConnectionObserver(var parentManager: Aoj20fBleManager, var mListener: Aoj20fBleManager.NordicListener) : ConnectionObserver {

    fun connect(device: BluetoothDevice) {
        parentManager.connectionObserver = this
        parentManager.connect(device)
                .timeout(10000)
                .retry(3, 100)
                .enqueue()
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d("bluetooth log", "onDeviceDisconnecting")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d("bluetooth log", "onDeviceDisconnected")
        mListener.isConnected(device, false)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.d("bluetooth log", "onDeviceReady")
        mListener.isConnected(device, true)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d("bluetooth log", "onDeviceConnected")
        mListener.bleConnectionEstablished()
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d("bluetooth log", "onDeviceFailedToConnect")
        onDeviceDisconnected(device, reason)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d("bluetooth log", "onDeviceConnecting")
    }
}