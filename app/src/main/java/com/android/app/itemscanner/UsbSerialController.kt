package com.android.app.itemscanner

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
import android.text.SpannableStringBuilder
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.lang.Exception
import java.nio.charset.Charset


class UsbSerialController(activity: Activity) {

    companion object {
        private const val TAG = "UsbSerialController"
        private const val WAIT_MILLIS = 2000
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    private val activity: Activity
    private var port: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null

    init {
        this.activity = activity
    }

    fun writeWithResponse(msg: String, expectedResponse: String, onResponse: Runnable) {
        val port = port ?: return
        if (port.isOpen) {
            ioManager =
                SerialInputOutputManager(
                    port, SerialListener(activity, expectedResponse, onResponse))
            ioManager?.start()

            port.write((msg + '\n').toByteArray(), WAIT_MILLIS)
            Log.i("gregoriou", "sent $msg")
        }
    }

    private class SerialListener(activity: Activity, validResponse: String, onResponse: Runnable)
        : SerialInputOutputManager.Listener {
        private val activity: Activity
        private val validResponse: String
        private val onResponse: Runnable

        init {
            this.activity = activity
            this.validResponse = validResponse
            this.onResponse = onResponse
        }

        override fun onNewData(data: ByteArray?) {
            activity.runOnUiThread {
                val data = data ?: return@runOnUiThread
                Log.i("gregoriou", "response: ${String(data)}")
                if (String(data) == validResponse) {
                    onResponse.run()
                    Log.i("gregoriou", "received ${data.size} bytes")
                }
            }
        }

        override fun onRunError(e: Exception?) {
            // Error will return when the port closed. Ignore this
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

    fun closeIoManager() {
        ioManager?.stop()
        ioManager = null
    }
}