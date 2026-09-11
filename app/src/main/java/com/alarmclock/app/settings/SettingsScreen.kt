package com.alarmclock.app.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alarmclock.app.R
import com.alarmclock.app.common.util.rememberAppViewModel
import com.alarmclock.app.theme.AppThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangeLanguage: () -> Unit,
    onPrivacySettings: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = rememberAppViewModel { app -> SettingsViewModel(app.settingsDataStore, com.alarmclock.app.alarm.AlarmScheduler(app)) }
    val settings by viewModel.settings.collectAsState()
    var showRateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd)) }
                }
            )
        }
    ) { padding ->
        val bugReportSubject = stringResource(R.string.bug_report_subject)
        val feedbackSubject = stringResource(R.string.feedback_label)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            SectionHeader(stringResource(R.string.section_general))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.language_label),
                    value = settings.languageName,
                    onClick = onChangeLanguage
                )
                SettingsRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.upcoming_alarm_notification_label),
                    value = if (settings.upcomingAlarmNotification) stringResource(R.string.on_text) else stringResource(R.string.off_text),
                    onClick = { viewModel.setUpcomingNotification(!settings.upcomingAlarmNotification) }
                )
                SettingsRow(
                    icon = Icons.Filled.BrightnessMedium,
                    title = stringResource(R.string.app_theme_label),
                    value = when (settings.themeMode) {
                        AppThemeMode.AUTO -> stringResource(R.string.auto_mode)
                        AppThemeMode.LIGHT -> stringResource(R.string.light_mode)
                        AppThemeMode.DARK -> stringResource(R.string.dark_mode)
                    },
                    onClick = { viewModel.cycleTheme() }
                )
            }

            SectionHeader(stringResource(R.string.section_manage_alarm))
            SettingsGroup {
                SettingsRowSwitch(
                    icon = Icons.Filled.Vibration,
                    title = stringResource(R.string.vibration_label),
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { viewModel.setVibration(it) }
                )
            }

            SectionHeader(stringResource(R.string.section_communicate))
            SettingsGroup {
                SettingsRow(icon = Icons.Filled.Shield, title = stringResource(R.string.privacy_settings_label), value = null, onClick = onPrivacySettings)
                SettingsRow(
                    icon = Icons.Filled.Email,
                    title = stringResource(R.string.feedback_label),
                    value = null,
                    onClick = { sendFeedbackEmail(context, feedbackSubject) }
                )
                SettingsRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = stringResource(R.string.privacy_policy_label),
                    value = null,
                    onClick = { openPrivacyPolicy(context) }
                )
                SettingsRow(
                    icon = Icons.Filled.BugReport,
                    title = stringResource(R.string.report_bug_label),
                    value = null,
                    onClick = { sendFeedbackEmail(context, bugReportSubject) }
                )
            }

            SectionHeader(stringResource(R.string.section_others))
            SettingsGroup {
                val shareLabel = stringResource(R.string.share_app_label)
                val shareMessageTemplate = stringResource(R.string.share_app_message)
                SettingsRow(
                    icon = Icons.Filled.Share,
                    title = shareLabel,
                    value = null,
                    onClick = { shareApp(context, shareLabel, shareMessageTemplate) }
                )
                SettingsRow(
                    icon = Icons.Filled.RateReview,
                    title = stringResource(R.string.rate_us_label),
                    value = null,
                    onClick = { showRateDialog = true }
                )
            }
        }

        if (showRateDialog) {
            RateUsDialog(
                onDismiss = { showRateDialog = false },
                onRate = {
                    showRateDialog = false
                    rateApp(context)
                }
            )
        }
    }
}

@Composable
private fun RateUsDialog(onDismiss: () -> Unit, onRate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rate_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.rate_dialog_message))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFA726))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRate) { Text(stringResource(R.string.rate_us_label)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)
        }
        if (value != null) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsRowSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun sendFeedbackEmail(context: android.content.Context, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@alarmclockapp.example"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

private const val PRIVACY_POLICY_URL = "https://api.buildprivacypolicy.com/policy/c3e35708-3302-4283-9e19-7231a9f1fc89"

private fun openPrivacyPolicy(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

private fun shareApp(context: android.content.Context, chooserTitle: String, messageTemplate: String) {
    val storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, String.format(messageTemplate, storeUrl))
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun rateApp(context: android.content.Context) {
    val uri = Uri.parse("market://details?id=${context.packageName}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        )
        context.startActivity(webIntent)
    }
}
