/*package com.example.e4app.ui.timer

import android.annotation.SuppressLint
import android.app.Activity
import android.icu.util.DateInterval
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import com.example.e4app.R
import com.example.e4app.ui.sensor.SensorActivity
import com.example.e4app.ui.wristband.wristbandActivity
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.activity_timer.*
import org.jetbrains.anko.toast
import java.util.concurrent.CompletableFuture

class timerActivity : AppCompatActivity() {

    private var flagTimer = false
    private var flagStop = false
    private var secondsRemaining = 120

    val timer = Counter(secondsRemaining.toLong()*1000, 1000)



    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        btnPlay.setOnClickListener { v ->
            btnPlay.setVisibility(FloatingActionButton.INVISIBLE)
            btnStop.setVisibility(FloatingActionButton.VISIBLE)
            timer.start()
            progress_countdown.max = secondsRemaining
        }
    }

    override fun onResume() {
        super.onResume()
        btnStop.setOnClickListener { v ->
            timer.cancel()
            flagStop  = true
            toast("BOTÓN STOP")
        }
    }

    inner class Counter(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval){
        @SuppressLint("RestrictedApi")
        override fun onFinish() {
            toast("Tiempo finalizado")
            println("Tiempo finalizado")
            progress_countdown.progress = 0
            btnStop.setVisibility(FloatingActionButton.INVISIBLE)

            flagTimer = true


        }

        override fun onTick(millisUntilFinished: Long) {
            secondsRemaining = millisUntilFinished.toInt() / 1000
            println("Timer : " + millisUntilFinished / 1000)


            updateCountdownUI()

            progress_countdown.progress = (secondsRemaining).toInt()

        }
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinutesUnilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinutesUnilFinished.toString()
        textViewTimer.text = "$minutesUntilFinished:${
        if (secondsStr.length == 2) secondsStr
        else "0" + secondsStr}"
    }
}*/