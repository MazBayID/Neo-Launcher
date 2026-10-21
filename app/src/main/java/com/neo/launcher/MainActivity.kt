package com.neo.launcher

import android.Manifest
import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.TextClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class AppInfo(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    private val requestCalendarPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
        ) requestCalendarPermission.launch(Manifest.permission.READ_CALENDAR)
        setContent { NeoLauncherApp() }
    }
}

@Composable
fun NeoLauncherApp() {
    val context = LocalContext.current
    var time by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var laciTerbuka by remember { mutableStateOf(false) }
    var agenda by remember { mutableStateOf<String?>(null) }
    val notificationVersion = NeoNotificationListener.version

    LaunchedEffect(Unit) {
        while (true) {
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            agenda = getUpcomingEvent(context)
            delay(30_000)
        }
    }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.activityInfo.loadIcon(pm))
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    // Read this state so the home screen recomposes when a notification arrives.
    @Suppress("UNUSED_VARIABLE") val refreshNotifications = notificationVersion

    MaterialTheme {
        if (laciTerbuka) {
            LaciAplikasi(apps = apps, onClose = { laciTerbuka = false })
        } else {
            Surface(Modifier.fillMaxSize(), color = Color(0xFF080A0F)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("neo@android:~$", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("────────────────────────", color = Color(0xFF39414D), fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(12.dp))
                    AndroidView(
                        factory = { ctx -> TextClock(ctx).apply {
                            format12Hour = "HH:mm"
                            format24Hour = "HH:mm"
                            textSize = 38f
                            setTextColor(android.graphics.Color.WHITE)
                            typeface = android.graphics.Typeface.MONOSPACE
                        } },
                        modifier = Modifier.height(52.dp)
                    )
                    val header = when {
                        agenda != null -> "NEXT: $agenda"
                        NeoNotificationListener.mediaTitle.value.isNotBlank() -> "♪ ${NeoNotificationListener.mediaTitle.value}"
                        else -> "SYSTEM ONLINE"
                    }
                    Text(header, color = if (agenda != null) Color(0xFFE5C07B) else Color(0xFF63E6BE),
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { laciTerbuka = true }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("> ", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                        Text("apps", color = Color(0xFFE5C07B), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(apps.take(15), key = { it.packageName }) { app ->
                            AppRow(app = app, compact = true, onLaunch = {
                                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                            })
                        }
                    }
                    Text("────────────────────────", color = Color(0xFF39414D), fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("● SYSTEM", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Text("Neo Launcher v0.2", color = Color(0xFF7F8793), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Row(Modifier.fillMaxWidth().clickable {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("> ", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace)
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFFABB2BF), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("settings", color = Color(0xFFABB2BF), fontFamily = FontFamily.Monospace)
                    }
                    Text("Notifications: tap to grant access", color = Color(0xFF7F8793),
                        fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                        modifier = Modifier.clickable { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) })
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, compact: Boolean, onLaunch: () -> Unit) {
    val context = LocalContext.current
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    var showActions by remember { mutableStateOf(false) }
    val springOffset by animateFloatAsState(
        targetValue = swipeOffset, animationSpec = spring(dampingRatio = 0.48f, stiffness = 350f),
        label = "app-row-spring"
    )
    val preview = NeoNotificationListener.previews[app.packageName].orEmpty()
    Column(
        Modifier.fillMaxWidth()
            .pointerInput(app.packageName) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        swipeOffset = (swipeOffset + amount).coerceIn(0f, 115f)
                    },
                    onDragEnd = {
                        showActions = swipeOffset > 55f
                        swipeOffset = if (showActions) 70f else 0f
                    }
                )
            }
            .clickable { if (showActions) showActions = false else onLaunch() }
            .padding(vertical = if (compact) 8.dp else 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.offset(x = springOffset.dp)) {
            Text("›", color = Color(0xFFC678DD), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            if (!compact) {
                Image(app.icon.toBitmap(width = 96, height = 96).asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(app.label, color = Color(0xFFE6E6E6), fontFamily = FontFamily.Monospace,
                    fontSize = if (compact) 13.sp else 15.sp, maxLines = 1)
                if (preview.isNotBlank()) Text(preview, color = Color(0xFF7F8793),
                    fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1)
            }
        }
        if (showActions) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, top = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("[ OPEN ]", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    modifier = Modifier.clickable { showActions = false; swipeOffset = 0f; onLaunch() })
                Text("[ INFO ]", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    modifier = Modifier.clickable {
                        showActions = false; swipeOffset = 0f
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${app.packageName}")
                        })
                    })
                Text("[ CLOSE ]", color = Color(0xFF7F8793), fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    modifier = Modifier.clickable { showActions = false; swipeOffset = 0f })
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun LaciAplikasi(apps: List<AppInfo>, onClose: () -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val abjad = ('A'..'Z').toList()
    var indexSentuh by remember { mutableIntStateOf(-1) }
    var lastIndex by remember { mutableIntStateOf(-1) }
    val vibrator = remember(context) {
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else @Suppress("DEPRECATION") (context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator)
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080A0F))) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 42.dp), contentPadding = PaddingValues(16.dp)) {
            item {
                Text("← Kembali", color = Color(0xFFE5C07B), fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onClose() }.padding(bottom = 16.dp))
            }
            items(apps, key = { it.packageName }) { app ->
                AppRow(app, compact = false, onLaunch = {
                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                })
            }
        }
        BoxWithConstraints(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(42.dp)
                .pointerInput(apps) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        fun update(y: Float) {
                            val idx = ((y / size.height.coerceAtLeast(1)) * abjad.size).toInt().coerceIn(0, 25)
                            indexSentuh = idx
                            if (idx != lastIndex) {
                                lastIndex = idx
                                if (android.os.Build.VERSION.SDK_INT >= 29) vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                                else vibrator.vibrate(8L)
                            }
                            val letter = abjad[idx]
                            val target = apps.indexOfFirst { it.label.firstOrNull()?.uppercaseChar() == letter }
                            if (target >= 0) scope.launch { listState.scrollToItem(target + 1) }
                        }
                        update(down.position.y)
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) update(change.position.y)
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                        indexSentuh = -1
                        lastIndex = -1
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val step = size.height / abjad.size
                abjad.forEachIndexed { i, char ->
                    val distance = if (indexSentuh >= 0) abs(indexSentuh - i) else 99
                    val scale = if (indexSentuh >= 0) (1f + (1f - distance / 5f).coerceIn(0f, 1f) * 0.8f) else 1f
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11f * density * scale
                        typeface = android.graphics.Typeface.create("monospace", if (distance == 0) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                        color = if (distance == 0) android.graphics.Color.rgb(99, 230, 190) else android.graphics.Color.rgb(127, 135, 147)
                    }
                    drawContext.canvas.nativeCanvas.drawText(char.toString(), size.width / 2f, step * (i + 0.68f), paint)
                }
            }
        }
    }
}

private fun getUpcomingEvent(context: android.content.Context): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return null
    return try {
        val now = System.currentTimeMillis()
        val end = now + 60 * 60 * 1000
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, now)
        android.content.ContentUris.appendId(builder, end)
        context.contentResolver.query(
            builder.build(),
            arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN),
            null, null, "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val title = cursor.getString(0) ?: return@use null
                val start = cursor.getLong(1)
                val minutes = ((start - now).coerceAtLeast(0) / 60000).toInt()
                "$title (${minutes}m)"
            } else null
        }
    } catch (_: Exception) { null }
}
