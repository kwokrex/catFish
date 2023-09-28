package com.example.catfish

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.Serializable
import org.json.JSONObject
import okhttp3.*
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

@Serializable
data class FishState(val x: Int, val y: Int, val visible: Boolean)

class MainActivity : AppCompatActivity() {

    private lateinit var webSocket: WebSocket // Declare the WebSocket variable
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view
        val hostnameVerifier = HostnameVerifier { hostname, session ->
            true
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .hostnameVerifier(hostnameVerifier)
            .build()

        val request = Request.Builder()
            .url("wss://192.168.1.2:8081") // Replace with your WebSocket server URL
            .build()


        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d("WebSocket", "WebSocket connection opened.")

                // Send screen dimensions to the server when the WebSocket connection is established
                val rootView = findViewById<ViewGroup>(android.R.id.content)
                val screenWidth = rootView.width
                val screenHeight = rootView.height
                val screenDimensions = JSONObject()
                screenDimensions.put("screenWidth", screenWidth)
                screenDimensions.put("screenHeight", screenHeight)

                webSocket.send(screenDimensions.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d("WebSocket", "Received message: $text")

                // Handle incoming messages from the server (e.g., fish state updates)
                // Update the fish position on the screen based on the received data
                runOnUiThread {
                    val fishState = JSONObject(text)
                    val x = fishState.getInt("x")
                    val y = fishState.getInt("y")

                    // Update the fish image view's position using findViewById
                    val fishImageView = findViewById<ImageView>(R.id.fishImageView)
                    fishImageView.x = x.toFloat()
                    fishImageView.y = y.toFloat()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                // Handle WebSocket connection failure
                Log.e("WebSocket", "WebSocket connection failure: ${t.message}")

            }
        })

        client.dispatcher.executorService.shutdown()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch event coordinates are within the fish's bounds
                val fishImageView = findViewById<ImageView>(R.id.fishImageView)
                val fishX = fishImageView.x
                val fishY = fishImageView.y
                val fishWidth = fishImageView.width
                val fishHeight = fishImageView.height

                if (event.x >= fishX && event.x <= fishX + fishWidth &&
                    event.y >= fishY && event.y <= fishY + fishHeight
                ) {
                    // Handle the touch event (e.g., send a message to the WebSocket server)
                    // You can add your logic here

                    // Example: Sending a message indicating a touch event

                    val touchEvent = JSONObject()
                    touchEvent.put("touchEvent", true)
                    webSocket.send(touchEvent.toString())
                    println("touched sent")
                }
            }
            else -> return super.onTouchEvent(event)
        }
        return true
    }
}
