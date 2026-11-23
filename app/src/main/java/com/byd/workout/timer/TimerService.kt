package com.byd.workout.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.byd.workout.BydApp
import com.byd.workout.MainActivity
import com.byd.workout.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var wakeLock: PowerManager.WakeLock? = null

    private val controller by lazy {
        (application as BydApp).container.timerController
    }

    override fun onCreate() {
        super.onCreate()
        createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                controller.pause()
                stopSelf()
            }
            ACTION_RESET -> {
                controller.reset()
                stopSelf()
            }
            else -> {
                startTimerForeground()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimerForeground() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BYD:TimerWakeLock").apply {
            acquire(10 * 60 * 1000L /*10 minutes*/)
        }

        scope.launch {
            val state = controller.state.first()
            if (!state.running) {
                stopSelf()
                return@launch
            }

            val remainingMs = (state.remainingSec * 1000).toLong()
            val notification = createOngoingNotification(remainingMs)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(this@TimerService, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            val diffMs = state.endAtElapsed - android.os.SystemClock.elapsedRealtime()
            if (diffMs > 0) {
                delay(diffMs)
            }

            // Time is up!
            onTimeUp()
        }
    }

    private suspend fun onTimeUp() {
        TimerAlarm.play(this)
        postDoneNotification()
        controller.onTimeUp()
        stopSelf()
    }

    private fun createOngoingNotification(remainingMs: Long): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
        val pausePending = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val resetIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESET }
        val resetPending = PendingIntent.getService(this, 2, resetIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(R.mipmap.ic_launcher) // Use launcher icon as there are no specifics
            .setContentTitle(getString(R.string.timer_running))
            .setContentIntent(openPending)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(System.currentTimeMillis() + remainingMs)
            .addAction(0, getString(R.string.timer_pause), pausePending)
            .addAction(0, getString(R.string.timer_reset), resetPending)
            .build()
    }

    private fun postDoneNotification() {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_DONE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.timer_done))
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_DONE_ID, notification)
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ongoingChannel = NotificationChannel(
                CHANNEL_TIMER,
                getString(R.string.channel_timer),
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ongoingChannel)

            val doneChannel = NotificationChannel(
                CHANNEL_DONE,
                getString(R.string.channel_timer_done),
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(doneChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PAUSE = "com.byd.workout.timer.PAUSE"
        const val ACTION_RESET = "com.byd.workout.timer.RESET"

        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_DONE_ID = 1002

        const val CHANNEL_TIMER = "timer"
        const val CHANNEL_DONE = "timer_done"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            context.stopService(intent)
        }
    }
}
