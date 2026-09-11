package com.alarmclock.app.common.util

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alarmclock.app.AlarmClockApp

@Composable
fun currentApp(): AlarmClockApp {
    val context = LocalContext.current
    return context.applicationContext as AlarmClockApp
}

class SimpleViewModelFactory(private val create: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
}

@Composable
inline fun <reified VM : ViewModel> rememberAppViewModel(noinline create: (AlarmClockApp) -> VM): VM {
    val app = currentApp()
    return viewModel(factory = SimpleViewModelFactory { create(app) })
}
