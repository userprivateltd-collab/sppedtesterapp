import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class MainActivity : AppCompatActivity() {

    private lateinit var speedTextView: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedTextView = findViewById(R.id.speedTextView)
        testButton = findViewById(R.id.testButton)

        testButton.setOnClickListener {
            testButton.isEnabled = false
            speedTextView.text = "Testing..."
            
            // Launching the network request safely on a background thread
            lifecycleScope.launch {
                // Using a reliable, small public file link for testing
                val testFileUrl = "https://speed.cloudflare.com/__down?bytes=5000000" 
                
                val finalSpeed = runSpeedTest(testFileUrl) { currentSpeed ->
                    // Update text in real-time as bytes stream down
                    speedTextView.text = String.format("%.2f Mbps", currentSpeed)
                }
                
                speedTextView.text = String.format("Final Speed: %.2f Mbps", finalSpeed)
                testButton.isEnabled = true
            }
        }
    }

    private suspend fun runSpeedTest(downloadUrl: String, onProgress: (Double) -> Unit): Double {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // The cache-buster parameter handles the offline bug
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
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            onProgress(currentSpeedMbps)
                        }
                    }
                }
                inputStream.close()
                val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
                return@withContext (totalBytesRead * 8) / 1000000.0 / totalTime
            } catch (e: Exception) {
                return@withContext 0.0 // Forces 0 Mbps if connection fails or drops
            }
        }
    }
}
