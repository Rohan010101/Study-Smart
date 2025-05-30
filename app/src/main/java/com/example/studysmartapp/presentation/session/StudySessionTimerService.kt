package com.example.studysmartapp.presentation.session

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.studysmartapp.util.Constants.ACTION_SERVICE_CANCEL
import com.example.studysmartapp.util.Constants.ACTION_SERVICE_START
import com.example.studysmartapp.util.Constants.ACTION_SERVICE_STOP
import com.example.studysmartapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.studysmartapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.studysmartapp.util.Constants.NOTIFICATION_ID
import com.example.studysmartapp.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class StudySessionTimerService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager


    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder


    private var binder = StudySessionTimerBinder()

    private lateinit var timer: Timer
    var duration: Duration = Duration.ZERO
        private set
    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentTimerState = mutableStateOf(TimerState.IDLE)
        private set
    val subjectId = mutableStateOf<Int?>(null)

    override fun onBind(p0: Intent?) = binder

//    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StudySessionTimerService", "onStartCommand started")
        intent?.action.let {
            when(it) {
                ACTION_SERVICE_START -> {
                    startForegroundService()
                    startTimer { hours, minutes, seconds ->
                        updateNotification(hours, minutes, seconds)
                    }
                }

                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }

                ACTION_SERVICE_CANCEL -> {
                    stopTimer()
                    cancelTimer()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startForegroundService() {
        Log.d("StudySessionTimerService", "startForegroundService started")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }



    private fun stopForegroundService() {
        Log.d("StudySessionTimerService", "stopForegroundService started")

        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    
    private fun createNotificationChannel() {
        Log.d("StudySessionTimerService", "createNotificationChannel started")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("StudySessionTimerService", "createNotificationChannel: if clause")
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            Log.d("StudySessionTimerService", "createNotificationChannel: outside if")
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        Log.d("StudySessionTimerService", "updateNotification started")

        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentText("$hours:$minutes:$seconds")
                .build()
        )
    }


    private fun startTimer (
        onTick: (h: String, m: String, s: String) -> Unit
    ) {
        Log.d("StudySessionTimerService", "startTimer started")
        currentTimerState.value = TimerState.STARTED
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopTimer() {
        Log.d("StudySessionTimerService", "stopTimer started")

        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentTimerState.value = TimerState.STOPPED
    }


    private fun cancelTimer() {
        Log.d("StudySessionTimerService", "cancelTimer started")

        duration = ZERO
        updateTimeUnits()
        currentTimerState.value = TimerState.IDLE
    }



    private fun updateTimeUnits() {
        Log.d("StudySessionTimerService", "updateTimeUnits started")

        duration.toComponents { hours, minutes, seconds, _ ->
            this@StudySessionTimerService.hours.value = hours.toInt().pad()
            this@StudySessionTimerService.minutes.value = minutes.pad()
            this@StudySessionTimerService.seconds.value = seconds.pad()
        }
    }


    inner class StudySessionTimerBinder : Binder() {
        fun getService(): StudySessionTimerService = this@StudySessionTimerService
    }
}



enum class TimerState {
    IDLE,
    STARTED,
    STOPPED
}
