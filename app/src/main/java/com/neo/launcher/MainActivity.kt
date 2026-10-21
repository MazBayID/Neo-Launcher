package com.neo.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val Night = Color(0xFF070B14)
private val Panel = Color(0xFF101827)
private val PanelAlt = Color(0xFF151F31)
private val Cyan = Color(0xFF54E6E0)
private val TextMain = Color(0xFFF2F7FF)
private val TextMuted = Color(0xFF91A2B9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoLauncherApp(packageManager, { packageName -> launchPackage(packageName) }) }
    }
    private fun launchPackage(packageName: String) {
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@Composable
private fun NeoLauncherApp(packageManager: PackageManager, onLaunch: (String) -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showApps by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) { now = System.currentTimeMillis(); delay(1000) }
    }
    val time = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)) }
    val seconds = remember(now) { SimpleDateFormat("ss", Locale.getDefault()).format(Date(now)) }
    val date = remember(now) { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date(now)) }
    val apps = remember(packageManager) {
        packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .mapNotNull { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val pkg = info.activityInfo?.packageName.orEmpty()
                if (label.isBlank() || pkg == "com.neo.launcher") null else InstalledApp(label, pkg)
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
    val filtered = remember(apps, search) { apps.filter { it.label.contains(search.trim(), true) } }
    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Night, surface = Panel, onSurface = TextMain)) {
        Surface(Modifier.fillMaxSize(), color = Night) {
            if (showApps) AppLibrary(filtered, search, { search = it }, { showApps = false }, onLaunch)
            else HomeScreen(time, seconds, date, apps.size) { showApps = true }
        }
    }
}

@Composable
private fun HomeScreen(time: String, seconds: String, date: String, appCount: Int, onOpenApps: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Night).statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NEO", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("LAUNCHER SYSTEM", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = TextMuted, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Selamat datang kembali.", color = TextMuted, fontSize = 13.sp)
            Text("Waktu berjalan\nbersamamu.", color = TextMain, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(color = Panel, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Timelapse, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                    Text("LOCAL TIME", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(time, color = TextMain, fontSize = 58.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                    Text(":$seconds", color = Cyan, fontSize = 20.sp, modifier = Modifier.padding(bottom = 9.dp))
                }
                Text(date, color = TextMuted, fontSize = 12.sp)
                HorizontalDivider(color = Color(0xFF263348))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnalogClock(Modifier.size(100.dp))
                        Text("ANALOG", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Cyan, modifier = Modifier.size(29.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(date.substringAfter(", "), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("KALENDER", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("QUICK ACCESS", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
                Text("Ruang aplikasimu", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("$appCount APPS", color = Cyan, fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Surface(color = PanelAlt, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenApps)) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apps, contentDescription = null, tint = Cyan, modifier = Modifier.size(28.dp))
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("App Library", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Cari dan buka aplikasi", color = TextMuted, fontSize = 11.sp)
                }
                Text("↗", color = Cyan, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("NEO LAUNCHER • BUILD 0.2.0", color = Color(0xFF526176), fontSize = 9.sp, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun AnalogClock(modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val c = remember(now) { java.util.Calendar.getInstance().apply { timeInMillis = now } }
    val sec = c.get(java.util.Calendar.SECOND)
    val min = c.get(java.util.Calendar.MINUTE) + sec / 60f
    val hour = c.get(java.util.Calendar.HOUR) + min / 60f
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.44f
        drawCircle(Color(0xFF1B2A3D), r, center)
        drawCircle(Cyan.copy(alpha = .75f), r, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90.0)
            val outer = r * .91f
            val inner = r * if (i % 5 == 0) .75f else .84f
            drawLine(if (i % 5 == 0) Cyan else TextMuted.copy(alpha = .6f),
                Offset(center.x + cos(a).toFloat() * inner, center.y + sin(a).toFloat() * inner),
                Offset(center.x + cos(a).toFloat() * outer, center.y + sin(a).toFloat() * outer),
                strokeWidth = if (i % 5 == 0) 1.8.dp.toPx() else .7.dp.toPx(), cap = StrokeCap.Round)
        }
        fun hand(degrees: Double, length: Float, width: Float, color: Color) {
            val a = Math.toRadians(degrees - 90.0)
            drawLine(color, center, Offset(center.x + cos(a).toFloat() * length, center.y + sin(a).toFloat() * length), width, cap = StrokeCap.Round)
        }
        hand(hour * 30.0, r * .48f, 3.dp.toPx(), TextMain)
        hand(min * 6.0, r * .68f, 2.dp.toPx(), Cyan)
        hand(sec * 6.0, r * .78f, 1.dp.toPx(), Color(0xFFFF8D79))
        drawCircle(TextMain, 3.dp.toPx(), center)
    }
}

@Composable
private fun AppLibrary(apps: List<InstalledApp>, search: String, onSearch: (String) -> Unit, onBack: () -> Unit, onLaunch: (String) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("APP LIBRARY", color = Cyan, fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text("${apps.size} aplikasi ditemukan", color = TextMuted, fontSize = 12.sp)
            }
            TextButton(onClick = onBack) { Text("BERANDA", color = Cyan, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("Cari aplikasi…", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan, unfocusedBorderColor = Color(0xFF263348),
                focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Cyan)
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(apps, key = { it.packageName }) { app ->
                Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onLaunch(app.packageName) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = Cyan, modifier = Modifier.size(25.dp))
                        Text(app.label, color = TextMain, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp).weight(1f))
                        Text("OPEN", color = Cyan, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}
