package com.yellowstu.knowit

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
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
    private lateinit var speedGauge: ProgressBar
    private lateinit var runTestButton: Button
    private lateinit var aboutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Binding
        speedValueText = findViewById(R.id.speedValueText)
        speedGauge = findViewById(R.id.speedGauge)
        runTestButton = findViewById(R.id.runTestButton)
        aboutButton = findViewById(R.id.aboutButton)

        // Run Speed Test Button Click Listener
        runTestButton.setOnClickListener {
            startSpeedTest()
        }

        // About Project Button Click Listener
        aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun startSpeedTest() {
        runTestButton.isEnabled = false
        runTestButton.text = "ANALYZING WIRES..."
        speedValueText.text = "0.00"
        speedGauge.progress = 0

        lifecycleScope.launch {
            try {
                // Stable endpoint with cache-busting
                val testFileUrl = "https://speed.cloudflare.com/__down?bytes=5000000"
                val dynamicUrl = "$testFileUrl&nocache=${System.currentTimeMillis()}"
                
                val finalSpeed = runDownloadTest(dynamicUrl) { currentMbps ->
                    speedValueText.text = String.format("%.2f", currentMbps)
                    // Scale gauge (assuming max 100 Mbps for visual representation)
                    val progress = (currentMbps.coerceIn(0.0, 100.0)).toInt()
                    speedGauge.progress = progress
                }

                speedValueText.text = String.format("%.2f", finalSpeed)
                runTestButton.text = "RUN SPEED TEST"
                runTestButton.isEnabled = true
                
            } catch (e: Exception) {
                speedValueText.text = "0.00"
                speedGauge.progress = 0
                runTestButton.text = "RUN SPEED TEST"
                runTestButton.isEnabled = true
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
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    val currentTime = System.currentTimeMillis()
                    val timePassedInSeconds = (currentTime - startTime) / 1000.0
                    
                    if (timePassedInSeconds > 0) {
                        // bits per second conversion: (bytes * 8) / 1,000,000 / seconds
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

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Project Intelligence")
        builder.setMessage(
            "Yellow sTudios\n" +
            "Founder: notrazx\n" +
            "Instagram: yellowstudios.f\n" +
            "Contact: userprivateitd@gmail.com\n\n" +
            "Licensed under MIT - A dedicated one-member performance matrix."
        )
        builder.setPositiveButton("CLOSE") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
