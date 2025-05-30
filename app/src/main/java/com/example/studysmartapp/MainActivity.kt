package com.example.studysmartapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import com.example.studysmartapp.domain.model.Session
import com.example.studysmartapp.domain.model.Subject
import com.example.studysmartapp.domain.model.Task
import com.example.studysmartapp.presentation.NavGraphs
import com.example.studysmartapp.presentation.destinations.SessionScreenRouteDestination
import com.example.studysmartapp.presentation.session.StudySessionTimerService
import com.example.studysmartapp.presentation.theme.StudySmartAppTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(true)                // checks whether the app is currently bound to the service or not
    private lateinit var timerService: StudySessionTimerService

    private val connection = object : ServiceConnection {

        // Timer Service Running
        override fun onServiceConnected(
            p0: ComponentName?,        // Represents the service name you are connecting to
            service: IBinder?           // a communication channel between the client & the service. Acts like a bridge between the two
        ) {
            try {
                Log.d("MainActivity", "onServiceConnected called, isBound = $isBound")
                val binder = service as StudySessionTimerService.StudySessionTimerBinder
                timerService = binder.getService()
                isBound = true
                Log.d("MainActivity", "onServiceConnected ended, isBound = $isBound")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in onServiceConnected: ${e.message}")
            }
        }

        // Timer Service Stopped
        override fun onServiceDisconnected(p0: ComponentName?) {
            try {
                isBound = false
                Log.d("MainActivity", "onServiceDisconnected called, isBound = $isBound")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in onServiceDisconnected: ${e.message}")
            }
        }
    }




    override fun onStart() {
        super.onStart()
        try {
            Intent(this, StudySessionTimerService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
            Log.d("MainActivity", "onStart called")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onStart: ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unbindService(connection)
            isBound = false
            Log.d("MainActivity", "onStop called")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onStop: ${e.message}")
        }
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d("MainActivity", "onCreate called, isBound = $isBound") // Log when onCreate is called
            enableEdgeToEdge()
            setContent {
                StudySmartAppTheme {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        dependenciesContainerBuilder = {
                            if (isBound) dependency(SessionScreenRouteDestination) { timerService }
                        }
                    )
                    Log.d("MainActivity", "setContent is complete")
                }
            }
            requestPermission()
            Log.d("MainActivity", "requestPermission is done")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
            Log.d("MainActivity", "requestPermission called")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in requestPermission: ${e.message}")
        }
    }
}





//// DUMMY SUBJECTS
//val subject = listOf(
//    Subject(name = "English", goalHours = 10f, colors = Subject.subjectCardColors[0].map { it.toArgb() }, subjectId = 0),
//    Subject(name = "Physics", goalHours = 10f, colors = Subject.subjectCardColors[1].map { it.toArgb() }, subjectId = 1),
//    Subject(name = "Chemistry", goalHours = 10f, colors = Subject.subjectCardColors[2].map { it.toArgb() }, subjectId = 2),
//    Subject(name = "Maths", goalHours = 10f, colors = Subject.subjectCardColors[3].map { it.toArgb() }, subjectId = 3),
//    Subject(name = "Geology", goalHours = 10f, colors = Subject.subjectCardColors[4].map { it.toArgb() }, subjectId = 4),
//)
//
//// DUMMY TASKS
//val tasks = listOf(
//    Task(
//        title = "Prepare Notes",
//        description = "",
//        dueDate = 0L,
//        priority = 1,
//        relatedToSubject = "",
//        isComplete = false,
//        taskSubjectId = 0,
//        taskId = 0,
//    ),
//    Task(
//        title = "Do Homework",
//        description = "",
//        dueDate = 0L,
//        priority = 2,
//        relatedToSubject = "",
//        isComplete = true,
//        taskSubjectId = 0,
//        taskId = 0,
//    ),
//    Task(
//        title = "Go Coaching",
//        description = "",
//        dueDate = 0L,
//        priority = 2,
//        relatedToSubject = "",
//        isComplete = false,
//        taskSubjectId = 0,
//        taskId = 0,
//    ),
//    Task(
//        title = "Assignment",
//        description = "",
//        dueDate = 0L,
//        priority = 1,
//        relatedToSubject = "",
//        isComplete = false,
//        taskSubjectId = 0,
//        taskId = 0,
//    ),
//    Task(
//        title = "Write Poem",
//        description = "",
//        dueDate = 0L,
//        priority = 0,
//        relatedToSubject = "",
//        isComplete = true,
//        taskSubjectId = 0,
//        taskId = 0,
//    ),
//)
//
//// DUMMY SESSIONS
//val sessions = listOf(
//    Session(
//        relatedToSubject = "English",
//        date = 0L,
//        duration = 2,
//        sessionSubjectId = 0,
//        sessionId = 0
//    ),
//    Session(
//        relatedToSubject = "Physics",
//        date = 0L,
//        duration = 2,
//        sessionSubjectId = 0,
//        sessionId = 0
//    ),
//    Session(
//        relatedToSubject = "Chemistry",
//        date = 0L,
//        duration = 2,
//        sessionSubjectId = 0,
//        sessionId = 0
//    ),
//    Session(
//        relatedToSubject = "Maths",
//        date = 0L,
//        duration = 2,
//        sessionSubjectId = 0,
//        sessionId = 0
//    ),
//    Session(
//        relatedToSubject = "Geology",
//        date = 0L,
//        duration = 2,
//        sessionSubjectId = 0,
//        sessionId = 0
//    ),
//)