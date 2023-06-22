package com.android.app.itemscanner

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber


class UsbSerialController(activity: Activity) {

    companion object {
        private const val TAG = "UsbSerialController"
        private const val WAIT_MILLIS = 2000
    }

    private val activity: Activity
    private var port: UsbSerialPort? = null

    init {
        this.activity = activity
    }

    /**
     * Write to the USB Serial device and waits for expectedResponse before running onResponse.
     * If port is missing or closed, run onResponse immediately.
     */
    fun writeWithResponse(msg: String, expectedResponse: String, onResponse: Runnable) {
        if (port == null || !port!!.isOpen) {
            onResponse.run()
            Log.i(TAG, "port is closed, running onResponse")
            return
        }
        val port = port!!
        port.write((msg + '\n').toByteArray(), WAIT_MILLIS)
        Log.i(TAG, "sent $msg")

        val response = ByteArray(expectedResponse.length)
        port.read(response, WAIT_MILLIS)
        if (String(response) == expectedResponse) {
            onResponse.run()
            Log.i(TAG, "received ${response.size} bytes")
        }
    }

    fun openDevice() {
        // Find all available drivers from attached devices.
        val manager = activity.getSystemService(Context.USB_SERVICE) as UsbManager? ?: return
        if (manager.deviceList.isEmpty()) return
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) return

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        if (driver.ports.isEmpty()) return
        port = driver.ports[0] // Most devices have just one port (port 0)

        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            Log.e(TAG, "Error getting serial connection")
            return
        }
        port?.open(connection)
        port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        port?.dtr = true
    }

    fun closeDevice() {
        port?.close()
    }
}