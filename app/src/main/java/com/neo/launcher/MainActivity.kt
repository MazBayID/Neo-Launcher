package com.neo.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.content.Context
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.ImageView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.ui.res.painterResource
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
private val Violet = Color(0xFFB69CFF)
private val Lime = Color(0xFFB8F36B)
private val Amber = Color(0xFFFFC46B)
private val TextMain = Color(0xFFF2F7FF)
private val TextMuted = Color(0xFF91A2B9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoLauncherApp(packageManager, { packageName -> launchPackage(packageName) }, { openAndroidSettings() }, { openCalendar() }, { openClock() }) }
    }
    private fun openClock() { runCatching { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CLOCK)) } }
    private fun openAndroidSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }
    private fun openCalendar() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
        runCatching { startActivity(intent) }.onFailure {
            runCatching { startActivity(Intent(Intent.ACTION_VIEW).setType("vnd.android.cursor.item/event")) }
        }
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
private fun NeoLauncherApp(packageManager: PackageManager, onLaunch: (String) -> Unit, onSettings: () -> Unit, onCalendar: () -> Unit, onClock: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showApps by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val appContext = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { appContext.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE) }
    var accentIndex by remember { mutableIntStateOf(prefs.getInt("accent_index", 0).coerceIn(0, 3)) }
    var iconScale by remember { mutableFloatStateOf(prefs.getFloat("icon_scale", 1f).coerceIn(0.8f, 1.5f)) }
    LaunchedEffect(accentIndex, iconScale) { prefs.edit().putInt("accent_index", accentIndex).putFloat("icon_scale", iconScale).apply() }
    val accents = listOf(Cyan, Violet, Lime, Amber)
    val accent = accents[accentIndex]
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
    val filtered = remember(apps, search, selectedLetter) { apps.filter { (selectedLetter == null || it.label.trim().firstOrNull()?.uppercaseChar() == selectedLetter) && it.label.contains(search.trim(), true) } }
    MaterialTheme(colorScheme = darkColorScheme(primary = accent, background = Night, surface = Panel, onSurface = TextMain)) {
        Surface(Modifier.fillMaxSize(), color = Night) {
            when {
                showSettings -> LauncherSettings(accentIndex, { accentIndex = it }, iconScale, { iconScale = it }, { showSettings = false }, onSettings)
                showApps -> AppLibrary(filtered, search, { search = it }, { showApps = false; search = "" }, onLaunch, accent, iconScale)
                else -> HomeScreen(time, seconds, date, apps.size, accent, batteryPercent(appContext), { showSettings = true }, onCalendar, onClock) { showApps = true }
            }
        }
    }
}

@Composable
private fun HomeScreen(time: String, seconds: String, date: String, appCount: Int, accent: Color, battery: Int, onSettings: () -> Unit, onCalendar: () -> Unit, onClock: () -> Unit, onOpenApps: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Night).statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NEO", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("LAUNCHER SYSTEM", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Icon(Icons.Default.Settings, contentDescription = "Buka Pengaturan Android", tint = TextMuted, modifier = Modifier.size(22.dp).clickable(onClick = onSettings).padding(1.dp))
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
                    Text(time, color = TextMain, fontSize = 58.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp, modifier = Modifier.clickable(onClick = onClock))
                    Text(":$seconds", color = accent, fontSize = 20.sp, modifier = Modifier.padding(bottom = 9.dp))
                }
                Text(date, color = TextMuted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("BATTERY", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    Text(if (battery >= 0) "$battery%" else "Tidak tersedia", color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(progress = { if (battery >= 0) battery / 100f else 0f }, modifier = Modifier.fillMaxWidth().height(3.dp), color = accent, trackColor = PanelAlt)
                HorizontalDivider(color = Color(0xFF263348))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnalogClock(Modifier.size(100.dp).clickable(onClick = onClock))
                        Text("ANALOG", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = com.neo.launcher.R.drawable.calendar_grid),
                            contentDescription = "Ilustrasi kalender",
                            modifier = Modifier.size(width = 72.dp, height = 54.dp).clickable(onClick = onCalendar)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(date.substringAfter(", "), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onCalendar))
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
            Text("$appCount APPS", color = accent, fontSize = 10.sp, letterSpacing = 1.sp)
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

    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = com.neo.launcher.R.drawable.clock_face_05),
            contentDescription = "Wajah jam analog Neo",
            modifier = Modifier.fillMaxSize()
        )
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.42f
            fun hand(degrees: Double, length: Float, width: Float, color: Color) {
                val a = Math.toRadians(degrees - 90.0)
                drawLine(
                    color = color,
                    start = center,
                    end = Offset(center.x + cos(a).toFloat() * length, center.y + sin(a).toFloat() * length),
                    strokeWidth = width,
                    cap = StrokeCap.Round
                )
            }
            hand(hour * 30.0, r * 0.48f, 3.dp.toPx(), TextMain)
            hand(min * 6.0, r * 0.67f, 2.dp.toPx(), Cyan)
            hand(sec * 6.0, r * 0.77f, 1.dp.toPx(), Color(0xFFFF8D79))
            drawCircle(Cyan, 3.dp.toPx(), center)
        }
    }
}

@Composable
private fun AppLibrary(apps: List<InstalledApp>, search: String, onSearch: (String) -> Unit, onBack: () -> Unit, onLaunch: (String) -> Unit, accent: Color, iconScale: Float, selectedLetter: Char?, onLetter: (Char?) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("APP LIBRARY", color = accent, fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text("${apps.size} aplikasi ditemukan${selectedLetter?.let { " • Huruf $it" } ?: ""}", color = TextMuted, fontSize = 12.sp)
            }
            TextButton(onClick = onBack) { Text("BERANDA", color = accent, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("Cari aplikasi…", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accent) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = Color(0xFF263348),
                focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = accent)
        )
        Spacer(Modifier.height(10.dp))
        val listState = rememberLazyListState()
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LazyColumn(state = listState, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onLaunch(app.packageName) }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AndroidView(factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(runCatching { ctx.packageManager.getApplicationIcon(app.packageName) }.getOrNull()) } },
                                modifier = Modifier.size((28 * iconScale).dp))
                            Text(app.label, color = TextMain, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 12.dp).weight(1f))
                            Text("BUKA", color = accent, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                }
                if (apps.isEmpty()) item { Text("Tidak ada aplikasi yang cocok. Coba kata kunci lain.", color = TextMuted, modifier = Modifier.padding(16.dp)) }
            }
            Column(Modifier.width(24.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                ("ABCDEFGHIJKLMNOPQRSTUVWXYZ#").forEach { letter ->
                    Text(letter.toString(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onLetter(if (letter == '#') null else letter) })
                }
            }
        }
    }
}

private fun batteryPercent(context: Context): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return -1
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) (level * 100) / scale else -1
}

@Composable
private fun LauncherSettings(accentIndex: Int, onAccent: (Int) -> Unit, iconScale: Float, onIconScale: (Float) -> Unit, onBack: () -> Unit, onAndroidSettings: () -> Unit) {
    val accents = listOf(Cyan, Violet, Lime, Amber)
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NEO CONTROL", color = accents[accentIndex], fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text("Kustomisasi launcher", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onBack) { Text("SELESAI", color = accents[accentIndex]) }
        }
        Text("WARNA AKSEN", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            accents.forEachIndexed { index, color ->
                Surface(color = color, shape = CircleShape, modifier = Modifier.size(46.dp).clickable { onAccent(index) }) {
                    Box(contentAlignment = Alignment.Center) { if (index == accentIndex) Text("✓", color = Night, fontWeight = FontWeight.Bold) }
                }
            }
        }
        HorizontalDivider(color = Color(0xFF263348))
        Text("UKURAN IKON APLIKASI", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("${(iconScale * 100).toInt()}%", color = accents[accentIndex], fontSize = 22.sp)
        Slider(value = iconScale, onValueChange = onIconScale, valueRange = 0.8f..1.5f, colors = SliderDefaults.colors(thumbColor = accents[accentIndex], activeTrackColor = accents[accentIndex]))
        HorizontalDivider(color = Color(0xFF263348))
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onAndroidSettings)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = accents[accentIndex], modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Pengaturan Android", color = TextMain, fontWeight = FontWeight.Medium)
                    Text("Wi-Fi, layar, aplikasi dan sistem", color = TextMuted, fontSize = 12.sp)
                }
                Text("↗", color = accents[accentIndex], fontSize = 22.sp)
            }
        }
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("CUACA", color = accents[accentIndex], fontWeight = FontWeight.Bold)
                Text("Integrasi sumber cuaca akan ditambahkan agar data akurat dan diperbarui.", color = TextMuted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("Preferensi saat ini aktif selama aplikasi berjalan. Penyimpanan permanen dan wallpaper akan ditambahkan pada tahap lanjutan.", color = TextMuted, fontSize = 11.sp)
    }
}
