package com.android.app.itemscanner

import android.content.Context
import android.hardware.usb.UsbManager
import android.text.SpannableStringBuilder
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber


class UsbSerialController(context: Context) {

    companion object {
        private const val TAG = "UsbSerialController"
        private const val WAIT_MILLIS = 2000
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    private val context: Context
    private var port: UsbSerialPort? = null

    init {
        this.context = context
    }

    fun write(msg: String) {
        val port = port ?: return
        if (port.isOpen) {
            port.write(msg.toByteArray(), WAIT_MILLIS)
            Log.i(TAG, "sent $msg")
            val buffer = ByteArray(8192)
            while (!buffer.toHex().contains("stepped")) {
                port.read(buffer, WAIT_MILLIS)
            }
            Log.i(TAG, "received ${buffer.toHex()}")
        }
    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun openDevice() {
        // Find all available drivers from attached devices.
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager? ?: return
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
    }

    fun closeDevice() {
        port?.close()
    }
}