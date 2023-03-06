package com.android.app.itemscanner

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class UsbSerialController(context: Context) {

    companion object {
        private const val WRITE_WAIT_MILLIS = 2000
    }

    private val context: Context
    private var port: UsbSerialPort? = null

    init {
        this.context = context
    }

    fun write(msg: String) {
//        Toast.makeText(context, if (port != null) port.toString() else "No port", Toast.LENGTH_LONG).show()
        val port = port ?: return
        if (port.isOpen) {
            port.write(msg.toByteArray(), WRITE_WAIT_MILLIS)
        }
    }

    fun openDevice() {
        // Find all available drivers from attached devices.
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager? ?: return
        Log.i(null, "gregoriou: devices ${manager.deviceList}")
        if (manager.deviceList.isEmpty()) return
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) return

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        Log.i(null, "gregoriou: driver $driver")
        if (driver.ports.isEmpty()) return
        port = driver.ports[0] // Most devices have just one port (port 0)

        val connection = manager.openDevice(driver.device)
        Log.i(null, "gregoriou: connection $connection")
        if (connection == null) {
            Log.e(null, "Error getting serial connection")
            return
        }
        port?.open(connection)
        port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
    }

    fun closeDevice() {
        port?.close()
    }
}