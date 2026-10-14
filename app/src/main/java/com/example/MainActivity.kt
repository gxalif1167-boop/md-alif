package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HabitFlowApp
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModel.provideFactory(application)
            )
            HabitFlowApp(viewModel = viewModel)
        }
    }
}
