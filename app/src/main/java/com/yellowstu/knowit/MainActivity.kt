package com.yellowstu.knowit

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class MainActivity : Activity() {

    private lateinit var speedTextView: TextView
    private lateinit var testButton: Button
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating a simple layout programmatically to completely bypass R.layout dependency errors
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = android.view.Gravity.CENTER
        }

        speedTextView = TextView(this).apply {
            text = "0.00 Mbps"
            textSize = 32f
            setPadding(0, 0, 0, 50)
        }

        testButton = Button(this).apply {
            text = "Test Internet Speed"
        }

        linearLayout.addView(speedTextView)
        linearLayout.addView(testButton)
        setContentView(linearLayout)

        testButton.setOnClickListener {
            testButton.isEnabled = false
            speedTextView.text = "Testing Network Speed..."
            
            mainScope.launch {
                val testFileUrl = "https://speed.cloudflare.com/__down?bytes=5000000" 
                
                val finalSpeed = runSpeedTest(testFileUrl) { currentSpeed ->
                    speedTextView.text = String.format("%.2f Mbps", currentSpeed)
                }
                
                speedTextView.text = String.format("Final Speed: %.2f Mbps", finalSpeed)
                testButton.isEnabled = true
            }
        }
    }

    private suspend fun runSpeedTest(downloadUrl: String, onProgress: (Double) -> Unit): Double {
        return withContext(Dispatchers.IO) {
            try {
                val uniqueUrl = URL("$downloadUrl&nocache=${System.currentTimeMillis()}")
                val connection: URLConnection = uniqueUrl.openConnection()
                connection.connect()

                val startTime = System.currentTimeMillis()
                val inputStream: InputStream = connection.getInputStream()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    val timePassed = (System.currentTimeMillis() - startTime) / 1000.0
                    if (timePassed > 0) {
                        val currentSpeedMbps = (totalBytesRead * 8) / 1000000.0 / timePassed
                        withContext(Dispatchers.Main) {
                            onProgress(currentSpeedMbps)
                        }
                    }
                }
                inputStream.close()
                val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
                return@withContext (totalBytesRead * 8) / 1000000.0 / totalTime
            } catch (e: Exception) {
                return@withContext 0.0 
            }
        }
    }
}
