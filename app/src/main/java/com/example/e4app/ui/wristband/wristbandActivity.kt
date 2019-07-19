package com.example.e4app.ui.wristband

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.empatica.empalink.ConnectionNotAllowedException
import com.empatica.empalink.EmpaDeviceManager
import com.empatica.empalink.EmpaticaDevice
import com.empatica.empalink.config.EmpaSensorType
import com.empatica.empalink.config.EmpaStatus
import com.empatica.empalink.delegate.EmpaDataDelegate
import com.empatica.empalink.delegate.EmpaStatusDelegate
import com.example.e4app.R
import com.example.e4app.ui.sensor.SensorActivity
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.activity_principal.*
import kotlinx.android.synthetic.main.activity_timer.*
import org.jetbrains.anko.toast
import java.util.concurrent.CompletableFuture

private const val PERMISSION_REQUEST = 10

open class wristbandActivity : AppCompatActivity(), EmpaDataDelegate, EmpaStatusDelegate {

    private var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET)

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1
    private val EMPATICA_API_KEY = "72d372af2c044db1ae355227e19acc35"
    private var deviceManager: EmpaDeviceManager? = null

    private var flagConnected = false
    private var flagInitTimer = false
    private var flagFinishTimer = false


    private var secondsRemaining = 120

    val timer = Counter(secondsRemaining.toLong()*1000, 1000)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        requestPermissions(permissions, PERMISSION_REQUEST)


    }

    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        btnScan.clicks()
            .subscribe {
                toast("Se inicia búsqueda")
                initEmpaticaDeviceManager()
            }

        btnStartCount.clicks()
             .subscribe{
                 if (flagConnected) {
                     flagInitTimer = true
                     timer.start()
                     //val intent: Intent = Intent(this, timerActivity::class.java )
                     //startActivity(intent)
                     progress_countdown.max = secondsRemaining
                     mainInvisible()

                 }else
                     toast("No está conectado a ningún dispositivo")
             }

        btnStop.clicks()
             .subscribe{
                 toast("Detenido")
                 timer.cancel()
                 timerInvisible()
                 progress_countdown.progress = 0
                 secondsRemaining = 120
                 flagInitTimer = false
             }


    }



    inner class Counter(millisInFuture: Long, countDownTimer: Long) : CountDownTimer(millisInFuture, countDownTimer){
        override fun onFinish() {
            toast("Tiempo finalizado")
            println("Tiempo finalizado")
            progress_countdown.progress = 0
            flagFinishTimer = true

        }

        override fun onTick(millisUntilFinished: Long) {
            secondsRemaining = millisUntilFinished.toInt() / 1000
            println("Timer: " + millisUntilFinished/ 1000)

            updateCountdownUI()
            progress_countdown.progress = (secondsRemaining).toInt()

        }
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinutesUntilFinifhed = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinutesUntilFinifhed.toString()
        textViewTimer.text = "$minutesUntilFinished:${
        if (secondsStr.length == 2) secondsStr
        else "0" + secondsStr}"
    }





    fun checkPermissions(context: Context, permissionsArray: Array<String>):Boolean{
        var allSuccess = true
        for (i in permissionsArray.indices){
            if(checkCallingOrSelfPermission(permissionsArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST){
            var allSuccess = true
            for (i in permissions.indices){
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    allSuccess = false
                    var requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain){
                        Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Ve a configuraciones y activa los permisos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
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
                    updateLabel(txvStatus, "CONECTÁNDOSE A: " + deviceLabel)
                }
            } catch (e: ConnectionNotAllowedException) {
                // This should happen only if you try to connect when allowed == false.
                toast("Sorry, you can't connect to this device")
            }

        }

    }

    override fun didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun didUpdateSensorStatus(status: Int, type: EmpaSensorType?) {
        didUpdateOnWristStatus(status)

    }

    override fun didUpdateStatus(status: EmpaStatus?) {

        // Update the UI
        //flagConnected = status.toString() == getString(R.string.connected)

        if(status!!.name == "DISCONNECTED"){
            updateLabel(txvStatus, "DESCONECTADO")
        }else if (status!!.name == "CONNECTED"){
            updateLabel(txvStatus, "CONECTADO")
            btnScan!!.setEnabled(false)
        }else{
            updateLabel(txvStatus, "PREPARADO PARA CONECTARSE" + '\n' + "Enciende el dispositivo y espera un momento")
            btnScan!!.setEnabled(false)
        }


        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(txvStatus, "PREPARADO PARA CONECTARSE" + '\n' + "Enciende el dispositivo y espera un momento")

            // Start scanning
            deviceManager!!.startScanning()
            // The device manager has established a connection
            // hide()

        } else if (status == EmpaStatus.CONNECTED) {
            flagConnected = status.toString() == getString(R.string.connected)
            // show()
            // The device manager disconnected from a device

        } else if (status == EmpaStatus.DISCONNECTED) {

            //  updateLabel(deviceNameLabel, "")

            // hide()
        }
    }


    private fun updateLabel(label: TextView, text: String) {
        runOnUiThread { label.text = text }
    }

    override fun didReceiveAcceleration(x: Int, y: Int, z: Int, timestamp: Double) {

    }

    override fun didReceiveBVP(bvp: Float, timestamp: Double) {
        println("INICIADO SENSOR BVP" + bvp.toString())
        // Log.i("sensorToma", bvp.toString())
        /*if(flagCount) {
            // iconList.add(bvp)
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            test += df.format(bvp).toString()
            test += "\n"
        }*/
        // sensorBvp.text = bvp.toString()
        // Log.i("bvpS", bvp.toString())

    }

    override fun didReceiveBatteryLevel(level: Float, timestamp: Double) {

    }

    override fun didReceiveGSR(gsr: Float, timestamp: Double) {

    }

    override fun didReceiveIBI(ibi: Float, timestamp: Double) {

    }

    override fun didReceiveTemperature(t: Float, timestamp: Double) {

    }

    override fun didReceiveTag(timestamp: Double) {

    }

    override fun didEstablishConnection() {

    }

    override fun didUpdateOnWristStatus(status: Int) {

    }

    @SuppressLint("RestrictedApi")
    private fun timerInvisible(){
        textView4.setVisibility(TextView.VISIBLE)
        btnScan.setVisibility(Button.VISIBLE)
        txvStatus.setVisibility(TextView.VISIBLE)
        btnStartCount.setVisibility(Button.VISIBLE)
        textViewTimer.setVisibility(TextView.INVISIBLE)
        textView7.setVisibility(TextView.INVISIBLE)
        btnStop.setVisibility(Button.INVISIBLE)
    }

    @SuppressLint("RestrictedApi")
    private fun mainInvisible(){
        textView4.setVisibility(TextView.INVISIBLE)
        btnScan.setVisibility(Button.INVISIBLE)
        txvStatus.setVisibility(TextView.INVISIBLE)
        btnStartCount.setVisibility(Button.INVISIBLE)
        textViewTimer.setVisibility(TextView.VISIBLE)
        textView7.setVisibility(TextView.VISIBLE)
        btnStop.setVisibility(Button.VISIBLE)
    }








}