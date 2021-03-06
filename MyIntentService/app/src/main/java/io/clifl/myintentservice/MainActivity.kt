package io.clifl.myintentservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //Notification stuff
    val CHANNEL_ID = "channelId"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 0

    //Binding stuff
    private var mService: ServiceWithBind? = null
    private var mBound = false
    private lateinit var intentToStartService: Intent




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Binding stuff
        intentToStartService = Intent(this, ServiceWithBind::class.java)
        startService(intentToStartService)

        //Notification
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Awesome notification")
            .setContentText("This is the content text")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        buttonNotification.setOnClickListener {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        buttonNotificationService.setOnClickListener {
            Intent(this, NotificationService::class.java).also {
                startService(it)
            }
        }


        // IntentService
        buttonStart.setOnClickListener {
            Intent(this, ABCIntentService::class.java).also {
                startService(it)
                textViewService.text = "Service running"
            }
        }


        buttonStop.setOnClickListener {
            ABCIntentService.stopService()
            textViewService.text = "Service stopped"
        }

        // Normal Service
        buttonStartService.setOnClickListener {
            Log.d("MyService", Thread.currentThread().name)
            Intent(this, ABCService::class.java).also {
                startService(it)
                textViewService.text = "A normal service running"
            }
        }

        buttonStopService.setOnClickListener {
            Intent(this, ABCService::class.java).also {
                // Instead of using companion object (singleton), this is also way to stop a service
                stopService(it)
                Log.d("MyService", "A normal service is stopped")
                textViewService.text = "A normal service stopped"
            }
        }

        buttonSendService.setOnClickListener {
            Intent(this, ABCService::class.java).also {
                // Instead of using companion object (singleton), this is also way to stop a service
                val dataString = editTextDataString.text.toString()
                it.putExtra("EXTRA_DATA", dataString)
                // Even though our service is still running, it wont restart it, it will just calls onStartCommand and pass the intent over to the service.
                startService(it)
            }
        }
    }

    // Notification
    fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

        }
    }

    override fun onStart() {
        super.onStart()
        bindService(intentToStartService, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if(mBound) {
            unbindService(connection)
            mBound = false
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as ServiceWithBind.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(intentToStartService)
    }
}