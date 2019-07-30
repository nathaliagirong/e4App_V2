package com.example.e4app.ui.splash

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import com.example.e4app.R
import com.example.e4app.ui.home.HomeActivity
import com.example.e4app.ui.sensor.SensorActivity
import com.example.e4app.ui.wristband.wristbandActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    private val timer = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        splashImgView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_in))

        Handler().postDelayed({
            splashImgView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_out))
            Handler().postDelayed({
                splashImgView.visibility = View.GONE
                //startActivity<wristbandActivity>()
                startActivity<HomeActivity>()
                finish()
            },500)
        }, timer.toLong())

    }
}
