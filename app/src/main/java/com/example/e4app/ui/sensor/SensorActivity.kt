package com.example.e4app.ui.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import com.empatica.empalink.ConnectionNotAllowedException
import com.empatica.empalink.EmpaDeviceManager
import com.empatica.empalink.EmpaticaDevice
import com.empatica.empalink.config.EmpaSensorType
import com.empatica.empalink.config.EmpaStatus
import com.empatica.empalink.delegate.EmpaDataDelegate
import com.empatica.empalink.delegate.EmpaStatusDelegate
import com.example.e4app.R
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.ArrayList

class SensorActivity : Activity(), EmpaDataDelegate, EmpaStatusDelegate{


    private val REQUEST_ENABLE_BT = 1


    private val REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1


    private val EMPATICA_API_KEY = "72d372af2c044db1ae355227e19acc35" // TODO insert your API Key here


    private var deviceManager: EmpaDeviceManager? = null

    private var flagCount = false

    private var flagCounnected = false

    internal var iconList = ArrayList<Float>()

    internal var test = ""

    private var namePerson = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        initEmpaticaDeviceManager()


    }

    inner class MyCounter(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onFinish() {
            println("Timer Completed.")
            flagCount = false
            toast("FINALIZA")
            val name = edtPersonName.text.toString() + ".csv"

            val textFile = File(Environment.getExternalStorageDirectory(), name)
            val fos = FileOutputStream(textFile)

            // Se crea el string a partir del array
            /* val y = 0
             while ( y < iconList.size) {
                 test += iconList[y]
                 test += "\n"
             }
             */
            fos.write(test.toByteArray())
            fos.close()
            // iconList.clear()
            test = ""
            toast("ARCHIVO GUARDADO")
            // tv.text = "Timer Completed."
        }

        override fun onTick(millisUntilFinished: Long) {
            // tv.textSize = 50f

            // tv.text = (millisUntilFinished / 1000).toString() + ""
            println("Timer  : " + millisUntilFinished / 1000)

            txvTimmer.text = (millisUntilFinished / 1000).toString()

        }
    }

    fun createCsv() {
        val name = edtPersonName.text.toString() + ".csv"

        val textFile = File(Environment.getExternalStorageDirectory(), name)
        val fos = FileOutputStream(textFile)

        // Se crea el string a partir del array
       /* val y = 0
        while ( y < iconList.size) {
            test += iconList[y]
            test += "\n"
        }
        */
        fos.write(test.toByteArray())
        fos.close()
       // iconList.clear()
        test = ""
        toast("ARCHIVO GUARDADO")

    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        val timer = MyCounter(120000, 1000)


        btnScanM.clicks()
                .subscribe {
                    toast("se inicia busqueda")
                    initEmpaticaDeviceManager()
                }

        btnStartCountM.clicks()
                .subscribe{
                    edtPersonName.text.isEmpty()
                    if(!edtPersonName.text.isEmpty() && !edtPersonAge.text.isEmpty() && flagCounnected) {

                        timer.start()
                        flagCount = true
                    } else {
                        when (flagCounnected) {
                            false -> toast("No está conectado a ningún dispositivo")
                            else -> toast("Los campos de nombre y edad son obligatorios")
                        }
                    }
                }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_COARSE_LOCATION ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager()
                } else {
                    // Permission denied, boo!
                    val needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry") { dialog, which ->
                                // try again
                                if (needRationale) {
                                    // the "never ask again" flash is not set, try again with permission request
                                    initEmpaticaDeviceManager()
                                } else {
                                    // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                }
                            }
                            .setNegativeButton("Exit application") { dialog, which ->
                                // without permission exit is the only way
                                finish()
                            }
                            .show()
                }
        }
    }


    private fun initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_ACCESS_COARSE_LOCATION)
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close") { dialog, which ->
                            // without permission exit is the only way
                            finish()
                        }
                        .show()
                return
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = EmpaDeviceManager(applicationContext, this, this)

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager!!.authenticateWithAPIKey(EMPATICA_API_KEY)
        }
    }

    override fun onPause() {
        super.onPause()
        if (deviceManager != null) {
            deviceManager!!.stopScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (deviceManager != null) {
            deviceManager!!.cleanUp()
        }
    }


    override fun didReceiveTemperature(t: Float, timestamp: Double) {

    }

    override fun didReceiveTag(timestamp: Double) {

    }

    override fun didReceiveGSR(gsr: Float, timestamp: Double) {

    }

    override fun didReceiveBatteryLevel(level: Float, timestamp: Double) {

    }

    override fun didReceiveAcceleration(x: Int, y: Int, z: Int, timestamp: Double) {

    }

    override fun didReceiveIBI(ibi: Float, timestamp: Double) {

    }

    override fun didReceiveBVP(bvp: Float, timestamp: Double) {
        // Log.i("sensorToma", bvp.toString())
        if(flagCount) {
            // iconList.add(bvp)
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            test += df.format(bvp).toString()
            test += "\n"
        }
       // sensorBvp.text = bvp.toString()
       // Log.i("bvpS", bvp.toString())

    }

    override fun didUpdateSensorStatus(status: Int, type: EmpaSensorType?) {
        didUpdateOnWristStatus(status)

    }

    override fun didUpdateOnWristStatus(status: Int) {

    }

    override fun didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

    }


    override fun didUpdateStatus(status: EmpaStatus?) {
        // Update the UI
        // statusLabel.text = status.toString()
        flagCounnected = status.toString() == getString(R.string.connected)

        // updateLabel(statusLabel, status.name)

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            // updateLabel(statusLabel, status.name + " - Turn on your device")
            // Start scanning
            deviceManager!!.startScanning()
            // The device manager has established a connection

            // hide()

        } else if (status == EmpaStatus.CONNECTED) {
            // show()
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {

           //  updateLabel(deviceNameLabel, "")

           // hide()
        }

    }

    override fun didEstablishConnection() {

    }

    override fun didDiscoverDevice(device: EmpaticaDevice?, deviceLabel: String?, rssi: Int, allowed: Boolean) {
        Log.i("allowed2", allowed.toString())
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager!!.stopScanning()
            try {
                // Connect to the device
                deviceManager!!.connectDevice(device)
                if (deviceLabel != null) {
                    toast(deviceLabel)
                }
                // Nombre del dispositivo
                // updateLabel(deviceNameLabel, "To: $deviceName")
            } catch (e: ConnectionNotAllowedException) {
                // This should happen only if you try to connect when allowed == false.
                toast("Sorry, you can't connect to this device")
            }

        }

    }


}