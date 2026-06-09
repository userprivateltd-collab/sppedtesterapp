package com.yellowstu.knowit

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class MainActivity : ComponentActivity() {

    private lateinit var speedValueText: TextView
    private lateinit var speedGauge: SpeedGaugeView
    private lateinit var speedGraph: LineGraphView
    private lateinit var runTestButton: Button
    private lateinit var settingsBtn: ImageView
    private lateinit var adView: AdView
    private lateinit var visibilitySwitch: Switch
    
    // Bottom Nav
    private lateinit var navGrid: ImageView
    private lateinit var navCompass: ImageView
    private lateinit var navShare: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize AdMob SDK
        MobileAds.initialize(this) {}

        // Find and load the banner ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // UI Binding
        speedValueText = findViewById(R.id.speedValueText)
        speedGauge = findViewById(R.id.speedGauge)
        speedGraph = findViewById(R.id.speedGraph)
        runTestButton = findViewById(R.id.runTestButton)
        settingsBtn = findViewById(R.id.settingsBtn)
        visibilitySwitch = findViewById(R.id.visibilitySwitch)
        
        navGrid = findViewById(R.id.navGrid)
        navCompass = findViewById(R.id.navCompass)
        navShare = findViewById(R.id.navShare)

        // Run Speed Test Button Click Listener
        runTestButton.setOnClickListener {
            startSpeedTest()
        }

        // Settings/About Button Click Listener
        settingsBtn.setOnClickListener {
            showAboutDialog()
        }

        // Visibility Switch Listener
        visibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            adView.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Bottom Nav Listeners
        navGrid.setOnClickListener {
            Toast.makeText(this, "Grid View", Toast.LENGTH_SHORT).show()
        }
        navCompass.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }
        navShare.setOnClickListener {
            shareResults()
        }
    }

    private fun startSpeedTest() {
        runTestButton.isEnabled = false
        runTestButton.text = "ANALYZING..."
        runTestButton.alpha = 0.5f 
        speedValueText.text = "0.0"
        speedGauge.setSpeed(0f)
        speedGraph.clear()

        lifecycleScope.launch {
            try {
                // Stable endpoint with cache-busting
                val testFileUrl = "https://speed.cloudflare.com/__down?bytes=10000000"
                val dynamicUrl = "$testFileUrl&nocache=${System.currentTimeMillis()}"
                
                val finalSpeed = runDownloadTest(dynamicUrl) { currentMbps ->
                    speedValueText.text = String.format("%.1f", currentMbps)
                    speedGauge.setSpeed(currentMbps.toFloat())
                    speedGraph.addDataPoint(currentMbps.toFloat())
                }

                speedValueText.text = String.format("%.1f", finalSpeed)
                runTestButton.text = "START TEST"
                runTestButton.isEnabled = true
                runTestButton.alpha = 1.0f
                
            } catch (e: Exception) {
                speedValueText.text = "0.0"
                speedGauge.setSpeed(0f)
                runTestButton.text = "START TEST"
                runTestButton.isEnabled = true
                runTestButton.alpha = 1.0f
            }
        }
    }

    private suspend fun runDownloadTest(urlString: String, onProgress: (Double) -> Unit): Double {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection: URLConnection = url.openConnection()
                connection.connect()

                val startTime = System.currentTimeMillis()
                val inputStream: InputStream = connection.getInputStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    val currentTime = System.currentTimeMillis()
                    val timePassedInSeconds = (currentTime - startTime) / 1000.0
                    
                    if (timePassedInSeconds > 0) {
                        val currentMbps = (totalBytesRead * 8.0) / 1000000.0 / timePassedInSeconds
                        withContext(Dispatchers.Main) {
                            onProgress(currentMbps)
                        }
                    }
                }
                inputStream.close()

                val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
                return@withContext (totalBytesRead * 8.0) / 1000000.0 / totalTime
            } catch (e: Exception) {
                return@withContext 0.0
            }
        }
    }

    private fun shareResults() {
        val speed = speedValueText.text.toString()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Speed Test Result")
            putExtra(Intent.EXTRA_TEXT, "I just tested my internet speed with KnowIt! My speed is $speed Mbps. #SpeedTest #KnowIt")
        }
        startActivity(Intent.createChooser(shareIntent, "Share your speed"))
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle("Project Intelligence")
        
        val message = "Developed by Yellow sTudios\n" +
                      "Founder: notrazx\n" +
                      "Instagram: yellowstudios.f\n\n" +
                      "Contact: userprivateltd@gmail.com\n" +
                      "Licensed under MIT"
                      
        builder.setMessage(message)
        builder.setPositiveButton("CLOSE") { dialog, _ -> dialog.dismiss() }
        
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#00E5FF"))
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}
