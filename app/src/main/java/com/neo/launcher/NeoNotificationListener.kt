package com.neo.launcher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

class NeoNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isNotBlank() || text.isNotBlank()) {
            val preview = listOf(title, text).filter { it.isNotBlank() }.joinToString(": ")
                .replace(Regex("\\s+"), " ").take(110)
            previews[sbn.packageName] = preview
            version.value++
        }
        val media = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        if (sbn.notification.category == Notification.CATEGORY_TRANSPORT && media.isNotBlank()) {
            mediaTitle.value = media
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Keep the last preview; users can still see the latest message while the app is quiet.
    }

    companion object {
        val previews = mutableStateMapOf<String, String>()
        val version = mutableStateOf(0)
        val mediaTitle = mutableStateOf("")
    }
}
