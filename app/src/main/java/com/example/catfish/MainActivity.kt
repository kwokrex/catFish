package com.example.catfish

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.Serializable
import org.json.JSONObject
import okhttp3.*
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import kotlin.math.pow

@Serializable
data class FishState(val x: Int, val y: Int, val visible: Boolean)

class MainActivity : AppCompatActivity() {

    private lateinit var webSocket: WebSocket // Declare the WebSocket variable
    private lateinit var fadeOutAnimation: Animation
    private lateinit var fishImageView: ImageView // Reference to the fish ImageView

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    // Handler to schedule periodic updates
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view
        val hostnameVerifier = HostnameVerifier { hostname, session ->
            true
        }
        // Initialize the fade-out animation
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

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
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d("WebSocket", "Received message: $text")
                startFishSwimming()
                // Handle incoming messages from the server (e.g., fish state updates)
                // Update the fish position on the screen based on the received data
                runOnUiThread {
                    val fishState = JSONObject(text)
                    val x = fishState.getInt("x")
                    val y = fishState.getInt("y")

                    // Update the fish image view's position using findViewById
                    val fishImageView = findViewById<ImageView>(R.id.fishImageView)
                    fishImageView.visibility = View.VISIBLE
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

        // Schedule periodic updates every 0.1 second
        handler.postDelayed(updateFishLocationRunnable, 100)
        client.dispatcher.executorService.shutdown()
        // Find the fish ImageView
        val fishImageView = findViewById<ImageView>(R.id.fishImageView)

        // Set a click listener on the fish ImageView
        fishImageView.setOnClickListener {
            // Apply the fade-out animation when the fish is clicked
            fishImageView.startAnimation(fadeOutAnimation)

            // Send touch event to server to respawn fish
            val touchEvent = JSONObject()
            touchEvent.put("touchEvent", true)
            webSocket.send(touchEvent.toString())
            println("touched sent")
            stopFishSwimming()
            // Hide the fish ImageView when the animation finishes
            fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    //fishImageView.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
    }

    // Runnable to periodically update fish location
    private val updateFishLocationRunnable = object : Runnable {
        private var moveX = 5f // Initial horizontal movement (constant rate)
        private var moveY = 5f // Initial vertical movement (constant rate)
        private var distanceMoved = 0f // Distance moved in the current direction

        override fun run() {
            // Get the current position of the fish
            val fishImageView = findViewById<ImageView>(R.id.fishImageView)
            val currentX = fishImageView.x
            val currentY = fishImageView.y
            val fishWidth = fishImageView.width.toFloat()
            val fishHeight = fishImageView.height.toFloat()

            // Get the screen dimensions
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val screenWidth = rootView.width.toFloat()
            val screenHeight = rootView.height.toFloat()

            // Calculate the new position by adding the movement
            val newX = currentX + moveX
            val newY = currentY + moveY

            // Check if the fish has hit the left or right screen edge
            if (newX < 0) {
                // Reverse horizontal movement to bounce off the left edge
                moveX = -moveX
            } else if (newX + fishWidth > screenWidth) {
                // Reverse horizontal movement to bounce off the right edge
                moveX = -moveX
            }

            // Check if the fish has hit the top or bottom screen edge
            if (newY < 0) {
                // Reverse vertical movement to bounce off the top edge
                moveY = -moveY
            } else if (newY + fishHeight > screenHeight) {
                // Reverse vertical movement to bounce off the bottom edge
                moveY = -moveY
            }

            // Update the position
            fishImageView.x = newX
            fishImageView.y = newY

            // Increment the distance moved
            distanceMoved += Math.sqrt((newX - currentX).toDouble().pow(2) + (newY - currentY).toDouble().pow(2)).toFloat()

            // Check if the fish has moved at least 300 pixels in the same direction
            if (distanceMoved >= 300) {
                // Randomly change the direction
                moveX = (Math.random() * 10 - 5).toFloat() // Random horizontal movement (-2 to 2)
                moveY = (Math.random() * 10 - 5).toFloat() // Random vertical movement (-2 to 2)

                // Reset the distance moved
                distanceMoved = 0f
            }

            // Schedule the next update after 0.1 second
            handler.postDelayed(this, 10)
        }
    }

    private fun startFishSwimming() {
        handler.removeCallbacks(updateFishLocationRunnable)
        handler.post(updateFishLocationRunnable)
    }

    private fun stopFishSwimming() {
        handler.removeCallbacks(updateFishLocationRunnable)
    }


/*    override fun onTouchEvent(event: MotionEvent): Boolean {
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
    }*/
}
