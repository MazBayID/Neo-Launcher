package com.neo.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.widget.ImageView
import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
    private fun openClock() {
        // OEM clock apps do not always register CATEGORY_APP_CLOCK.
        val clockApp = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { info ->
                val label = runCatching { packageManager.getApplicationLabel(info).toString() }.getOrDefault("")
                if (label.contains("clock", true) || label.contains("jam", true) || label.contains("alarm", true)) {
                    packageManager.getLaunchIntentForPackage(info.packageName)
                } else null
            }.firstOrNull()
        val intents = listOfNotNull(
            clockApp,
            Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS),
            Intent(android.provider.AlarmClock.ACTION_SET_ALARM),
            Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK")
        )
        for (intent in intents) {
            try {
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: Exception) {
                // Continue to the next supported clock entry point.
            }
        }
        Toast.makeText(this, "Aplikasi jam tidak ditemukan. Periksa aplikasi Jam di perangkat.", Toast.LENGTH_LONG).show()
    }
    private fun openAndroidSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }.onFailure {
            Toast.makeText(this, "Pengaturan Android tidak dapat dibuka", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openCalendar() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
        runCatching { startActivity(intent) }.recoverCatching {
            startActivity(Intent(Intent.ACTION_VIEW).setType("vnd.android.cursor.item/event"))
        }.onFailure {
            Toast.makeText(this, "Aplikasi kalender tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }
    private fun launchPackage(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            Toast.makeText(this, "Aplikasi tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(launchIntent) }.onFailure {
            Toast.makeText(this, "Aplikasi gagal dibuka", Toast.LENGTH_SHORT).show()
        }
    }
}

private data class InstalledApp(val label: String, val packageName: String, val isSystemApp: Boolean)

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
    var clockFaceIndex by remember { mutableIntStateOf(prefs.getInt("clock_face", 4).coerceIn(0, 4)) }
    var showSeconds by remember { mutableStateOf(prefs.getBoolean("show_seconds", true)) }
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24_hour", true)) }
    var dateFormatIndex by remember { mutableIntStateOf(prefs.getInt("date_format", 0).coerceIn(0, 2)) }
    var showBatteryWidget by remember { mutableStateOf(prefs.getBoolean("show_battery_widget", true)) }
    var showAnalogClockWidget by remember { mutableStateOf(prefs.getBoolean("show_analog_clock_widget", true)) }
    var showHomeDate by remember { mutableStateOf(prefs.getBoolean("show_home_date", true)) }
    var homeClockScale by remember { mutableFloatStateOf(prefs.getFloat("home_clock_scale", 1f).coerceIn(0.8f, 1.4f)) }
    var homeCardOpacity by remember { mutableFloatStateOf(prefs.getFloat("home_card_opacity", 1f).coerceIn(0.35f, 1f)) }
    var analogClockScale by remember { mutableFloatStateOf(prefs.getFloat("analog_clock_scale", 1f).coerceIn(0.7f, 1.5f)) }
    var digitalClockWeight by remember { mutableIntStateOf(prefs.getInt("digital_clock_weight", 0).coerceIn(0, 2)) }
    var handStyleIndex by remember { mutableIntStateOf(prefs.getInt("clock_hand_style", 0).coerceIn(0, 3)) }
    var favoritePackages by remember { mutableStateOf(prefs.getStringSet("favorite_packages", emptySet()).orEmpty().toSet()) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var hideSystemApps by remember { mutableStateOf(prefs.getBoolean("hide_system_apps", false)) }
    var showAppLabels by remember { mutableStateOf(prefs.getBoolean("show_app_labels", true)) }
    var accentAppLabels by remember { mutableStateOf(prefs.getBoolean("accent_app_labels", false)) }
    var appLabelScale by remember { mutableFloatStateOf(prefs.getFloat("app_label_scale", 1f).coerceIn(0.8f, 1.4f)) }
    var showGreeting by remember { mutableStateOf(prefs.getBoolean("show_greeting", true)) }
    var accentDigitalClock by remember { mutableStateOf(prefs.getBoolean("accent_digital_clock", false)) }
    var homeBackgroundIndex by remember { mutableIntStateOf(prefs.getInt("home_background", 0).coerceIn(0, 3)) }
    var sortDescending by remember { mutableStateOf(prefs.getBoolean("sort_descending", false)) }
    var fuzzySearch by remember { mutableStateOf(prefs.getBoolean("fuzzy_search", false)) }
    LaunchedEffect(sortDescending) { prefs.edit().putBoolean("sort_descending", sortDescending).apply() }
    LaunchedEffect(fuzzySearch) { prefs.edit().putBoolean("fuzzy_search", fuzzySearch).apply() }
    BackHandler(enabled = showApps || showSettings) {
        if (showSettings) showSettings = false else { showApps = false; search = ""; selectedLetter = null; favoritesOnly = false }
    }
    LaunchedEffect(favoritePackages) { prefs.edit().putStringSet("favorite_packages", favoritePackages.toSet()).apply() }
    LaunchedEffect(hideSystemApps, showAppLabels, homeBackgroundIndex, accentAppLabels, appLabelScale, showGreeting, accentDigitalClock) {
        prefs.edit().putBoolean("hide_system_apps", hideSystemApps).putBoolean("show_app_labels", showAppLabels)
            .putInt("home_background", homeBackgroundIndex).putBoolean("accent_app_labels", accentAppLabels)
            .putFloat("app_label_scale", appLabelScale).putBoolean("show_greeting", showGreeting)
            .putBoolean("accent_digital_clock", accentDigitalClock).apply()
    }
    LaunchedEffect(accentIndex, iconScale, clockFaceIndex, showSeconds, use24Hour, dateFormatIndex, showBatteryWidget, showAnalogClockWidget, handStyleIndex, showHomeDate, homeClockScale, homeCardOpacity, analogClockScale, digitalClockWeight) {
        prefs.edit().putInt("accent_index", accentIndex).putFloat("icon_scale", iconScale)
            .putInt("clock_face", clockFaceIndex).putBoolean("show_seconds", showSeconds)
            .putBoolean("use_24_hour", use24Hour).putInt("date_format", dateFormatIndex)
            .putBoolean("show_battery_widget", showBatteryWidget)
            .putBoolean("show_analog_clock_widget", showAnalogClockWidget)
            .putInt("clock_hand_style", handStyleIndex).putBoolean("show_home_date", showHomeDate).putFloat("home_clock_scale", homeClockScale).putFloat("home_card_opacity", homeCardOpacity).putFloat("analog_clock_scale", analogClockScale).putInt("digital_clock_weight", digitalClockWeight).apply()
    }
    val accents = listOf(Cyan, Violet, Lime, Amber)
    val accent = accents[accentIndex]
    LaunchedEffect(Unit) {
        while (true) { now = System.currentTimeMillis(); delay(1000) }
    }
    val time = remember(now, use24Hour) { SimpleDateFormat(if (use24Hour) "HH:mm" else "h:mm", Locale.getDefault()).format(Date(now)) }
    val period = remember(now, use24Hour) { if (use24Hour) "" else SimpleDateFormat("a", Locale.getDefault()).format(Date(now)) }
    val seconds = remember(now, showSeconds) { if (showSeconds) SimpleDateFormat("ss", Locale.getDefault()).format(Date(now)) else "" }
    val date = remember(now, dateFormatIndex) {
        val pattern = when (dateFormatIndex) {
            1 -> "EEE, d MMM yyyy"
            2 -> "dd/MM/yyyy"
            else -> "EEEE, d MMMM yyyy"
        }
        SimpleDateFormat(pattern, Locale("id", "ID")).format(Date(now))
    }
    val apps = remember(packageManager) {
        packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .mapNotNull { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val pkg = info.activityInfo?.packageName.orEmpty()
                if (label.isBlank() || pkg == "com.neo.launcher") null else InstalledApp(label, pkg, (info.activityInfo?.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0)
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
    val filtered = remember(apps, search, selectedLetter) {
        apps.filter { app ->
            val first = app.label.trim().firstOrNull()?.uppercaseChar()
            val matchesLetter = when (selectedLetter) {
                null -> true
                '#' -> first == null || !first.isLetter()
                else -> first == selectedLetter
            }
            matchesLetter && (app.label.contains(search.trim(), true) || (fuzzySearch && fuzzyMatches(app.label, search.trim())))
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = accent, background = Night, surface = Panel, onSurface = TextMain)) {
        Surface(Modifier.fillMaxSize(), color = Night) {
            when {
                showSettings -> LauncherSettings(accentIndex, { accentIndex = it }, iconScale, { iconScale = it }, clockFaceIndex, { clockFaceIndex = it }, showSeconds, { showSeconds = it }, use24Hour, { use24Hour = it }, dateFormatIndex, { dateFormatIndex = it }, showBatteryWidget, { showBatteryWidget = it }, showAnalogClockWidget, { showAnalogClockWidget = it }, showHomeDate, { showHomeDate = it }, homeClockScale, { homeClockScale = it }, homeCardOpacity, { homeCardOpacity = it }, analogClockScale, { analogClockScale = it }, digitalClockWeight, { digitalClockWeight = it }, handStyleIndex, { handStyleIndex = it }, showAppLabels, { showAppLabels = it }, hideSystemApps, { hideSystemApps = it }, homeBackgroundIndex, { homeBackgroundIndex = it }, accentAppLabels, { accentAppLabels = it }, appLabelScale, { appLabelScale = it }, showGreeting, { showGreeting = it }, accentDigitalClock, { accentDigitalClock = it }, { showSettings = false }, onSettings)
                showApps -> AppLibrary(
                    filtered.filter { !favoritesOnly || it.packageName in favoritePackages }
                        .let { list -> if (sortDescending) list.sortedByDescending { it.label.lowercase(Locale.getDefault()) } else list },
                    search, { search = it }, fuzzySearch, { fuzzySearch = it },
                    { showApps = false; search = ""; selectedLetter = null; favoritesOnly = false },
                    onLaunch, accent, iconScale, selectedLetter, { selectedLetter = it },
                    favoritePackages, { pkg -> favoritePackages = if (pkg in favoritePackages) favoritePackages - pkg else favoritePackages + pkg },
                    favoritesOnly, { favoritesOnly = it }, sortDescending, { sortDescending = it },
                    hideSystemApps, { hideSystemApps = it }, showAppLabels, accentAppLabels, appLabelScale
                )
                else -> HomeScreen(time, seconds, period, date, apps.size, accent, batteryPercent(appContext), isBatteryCharging(appContext), clockFaceIndex, showBatteryWidget, showAnalogClockWidget, showHomeDate, homeClockScale, homeCardOpacity, analogClockScale, digitalClockWeight, handStyleIndex, homeBackgroundIndex, showGreeting, accentDigitalClock, { showSettings = true }, onCalendar, onClock) { showApps = true }
            }
        }
    }
}

@Composable
private fun HomeScreen(time: String, seconds: String, period: String, date: String, appCount: Int, accent: Color, battery: Int, batteryCharging: Boolean, clockFaceIndex: Int, showBatteryWidget: Boolean, showAnalogClockWidget: Boolean, showHomeDate: Boolean, homeClockScale: Float, homeCardOpacity: Float, analogClockScale: Float, digitalClockWeight: Int, handStyleIndex: Int, homeBackgroundIndex: Int, showGreeting: Boolean, accentDigitalClock: Boolean, onSettings: () -> Unit, onCalendar: () -> Unit, onClock: () -> Unit, onOpenApps: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(brush = when (homeBackgroundIndex) {
            1 -> Brush.verticalGradient(listOf(Color(0xFF10152F), Color(0xFF211638), Color(0xFF070B14)))
            2 -> Brush.verticalGradient(listOf(Color(0xFF082A2A), Color(0xFF101827), Color(0xFF070B14)))
            3 -> Brush.verticalGradient(listOf(Color(0xFF302014), Color(0xFF171A26), Color(0xFF070B14)))
            else -> Brush.verticalGradient(listOf(Night, Night))
        }).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NEO", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("LAUNCHER SYSTEM", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Icon(Icons.Default.Settings, contentDescription = "Buka Pengaturan Neo", tint = TextMuted, modifier = Modifier.size(22.dp).clickable(onClick = onSettings).padding(1.dp))
        }
        if (showGreeting) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Selamat datang kembali.", color = TextMuted, fontSize = 13.sp)
            Text("Waktu berjalan\nbersamamu.", color = TextMain, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(color = Panel.copy(alpha = homeCardOpacity), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Timelapse, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Text("LOCAL TIME", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(time, color = if (accentDigitalClock) accent else TextMain, fontSize = 58.sp * homeClockScale, fontWeight = when (digitalClockWeight) { 1 -> FontWeight.Normal; 2 -> FontWeight.SemiBold; else -> FontWeight.Light }, letterSpacing = (-2).sp, modifier = Modifier.clickable(onClick = onClock))
                    if (seconds.isNotEmpty()) Text(":$seconds", color = accent, fontSize = 20.sp, modifier = Modifier.padding(bottom = 9.dp))
                    if (period.isNotEmpty()) Text(period, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 5.dp, bottom = 13.dp))
                }
                if (showHomeDate) Text(date, color = TextMuted, fontSize = 12.sp)
                if (showBatteryWidget) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (batteryCharging) "BATTERY • MENGISI" else "BATTERY", color = TextMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                        Text(if (battery >= 0) "$battery%" else "Tidak tersedia", color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(progress = { if (battery >= 0) battery / 100f else 0f }, modifier = Modifier.fillMaxWidth().height(3.dp), color = accent, trackColor = PanelAlt)
                    HorizontalDivider(color = Color(0xFF263348))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    if (showAnalogClockWidget) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnalogClock(Modifier.size(100.dp * analogClockScale).clickable(onClick = onClock), clockFaceIndex, handStyleIndex)
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
        Spacer(Modifier.height(4.dp))
        Text("NEO LAUNCHER • BUILD 0.10.4", color = Color(0xFF526176), fontSize = 9.sp, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun AnalogClock(modifier: Modifier = Modifier, clockFaceIndex: Int = 4, handStyleIndex: Int = 0) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val c = remember(now) { java.util.Calendar.getInstance().apply { timeInMillis = now } }
    val sec = c.get(java.util.Calendar.SECOND)
    val min = c.get(java.util.Calendar.MINUTE) + sec / 60f
    val hour = c.get(java.util.Calendar.HOUR) + min / 60f

    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = when (clockFaceIndex) {
                0 -> com.neo.launcher.R.drawable.clock_face_01
                1 -> com.neo.launcher.R.drawable.clock_face_02
                2 -> com.neo.launcher.R.drawable.clock_face_03
                3 -> com.neo.launcher.R.drawable.clock_face_04
                else -> com.neo.launcher.R.drawable.clock_face_05
            }),
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
            val handColors = when (handStyleIndex) {
                1 -> Triple(Color(0xFFFFC46B), Color(0xFFFFC46B), Color(0xFFFFC46B))
                2 -> Triple(Color(0xFFB69CFF), Color(0xFF54E6E0), Color(0xFFFF8D79))
                3 -> Triple(Color(0xFF070B14), Color(0xFF070B14), Color(0xFF070B14))
                else -> Triple(TextMain, Cyan, Color(0xFFFF8D79))
            }
            val handWidths = when (handStyleIndex) {
                1 -> Triple(4.dp.toPx(), 3.dp.toPx(), 1.5.dp.toPx())
                2 -> Triple(2.dp.toPx(), 2.dp.toPx(), 1.dp.toPx())
                3 -> Triple(4.dp.toPx(), 2.5.dp.toPx(), 1.dp.toPx())
                else -> Triple(3.dp.toPx(), 2.dp.toPx(), 1.dp.toPx())
            }
            hand(hour * 30.0, r * if (handStyleIndex == 2) 0.43f else 0.48f, handWidths.first, handColors.first)
            hand(min * 6.0, r * if (handStyleIndex == 2) 0.73f else 0.67f, handWidths.second, handColors.second)
            hand(sec * 6.0, r * 0.77f, handWidths.third, handColors.third)
            drawCircle(handColors.second, 3.dp.toPx(), center)
        }
    }
}

@Composable
private fun AppLibrary(apps: List<InstalledApp>, search: String, onSearch: (String) -> Unit, fuzzySearch: Boolean, onFuzzySearch: (Boolean) -> Unit, onBack: () -> Unit, onLaunch: (String) -> Unit, accent: Color, iconScale: Float, selectedLetter: Char?, onLetter: (Char?) -> Unit, favoritePackages: Set<String>, onToggleFavorite: (String) -> Unit, favoritesOnly: Boolean, onFavoritesOnly: (Boolean) -> Unit, sortDescending: Boolean, onSortDescending: (Boolean) -> Unit, hideSystemApps: Boolean, onHideSystemApps: (Boolean) -> Unit, showAppLabels: Boolean, accentAppLabels: Boolean, appLabelScale: Float) {
    var selectedAppForActions by remember { mutableStateOf<InstalledApp?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawerPrefs = remember { context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE) }
    var recentPackages by remember { mutableStateOf(drawerPrefs.getString("recent_packages", "").orEmpty().split("|").filter { it.isNotBlank() }) }
    var recentOnly by remember { mutableStateOf(false) }
    fun launchAndRecord(packageName: String) {
        recentPackages = (listOf(packageName) + recentPackages.filterNot { it == packageName }).take(12)
        drawerPrefs.edit().putString("recent_packages", recentPackages.joinToString("|")).apply()
        onLaunch(packageName)
    }
    if (selectedAppForActions != null) {
        val selectedApp = selectedAppForActions!!
        AlertDialog(
            onDismissRequest = { selectedAppForActions = null },
            title = { Text(selectedApp.label) },
            text = { Text("Pilih tindakan untuk aplikasi ini.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedAppForActions = null
                    runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + selectedApp.packageName))) }
                }) { Text("INFO APLIKASI") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onToggleFavorite(selectedApp.packageName)
                        selectedAppForActions = null
                    }) { Text(if (selectedApp.packageName in favoritePackages) "HAPUS FAVORIT" else "JADIKAN FAVORIT") }
                    TextButton(onClick = {
                        val packageUri = Uri.parse("package:" + selectedApp.packageName)
                        val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                            data = packageUri
                        }
                        val alternateIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                            data = packageUri
                        }

                        // Jangan tutup dialog sebelum kita memastikan intent dapat dipanggil.
                        val result = runCatching {
                            context.startActivity(uninstallIntent)
                        }.recoverCatching {
                            context.startActivity(alternateIntent)
                        }

                        if (result.isSuccess) {
                            selectedAppForActions = null
                        } else {
                            Toast.makeText(
                                context,
                                "Android tidak membuka konfirmasi copot. Membuka Info Aplikasi sebagai alternatif.",
                                Toast.LENGTH_LONG
                            ).show()
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        packageUri
                                    )
                                )
                                selectedAppForActions = null
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    "Gagal membuka pencopotan maupun Info Aplikasi.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }) { Text("COPOT") }
                }
            },
            containerColor = Panel,
            titleContentColor = TextMain,
            textContentColor = TextMuted
        )
    }
    var gridMode by remember { mutableStateOf(context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).getBoolean("app_drawer_grid", false)) }
    var gridColumns by remember { mutableIntStateOf(context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).getInt("app_drawer_columns", 2).coerceIn(2, 4)) }
    var showAlphabetIndex by remember { mutableStateOf(context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).getBoolean("show_alphabet_index", true)) }
    val visibleApps = (if (recentOnly) apps.sortedBy { app -> recentPackages.indexOf(app.packageName).let { if (it < 0) Int.MAX_VALUE else it } } else apps).filter { (!hideSystemApps || !it.isSystemApp) && (!recentOnly || it.packageName in recentPackages) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("APP LIBRARY", color = accent, fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text("${visibleApps.size} aplikasi • ${favoritePackages.size} favorit${selectedLetter?.let { " • Huruf $it" } ?: ""}", color = TextMuted, fontSize = 12.sp)
            }
            TextButton(onClick = onBack) { Text("BERANDA", color = accent, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("Cari aplikasi…", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari aplikasi", tint = accent) },
            trailingIcon = {
                if (search.isNotEmpty()) TextButton(onClick = { onSearch("") }) {
                    Text("HAPUS", color = accent, fontSize = 10.sp)
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = Color(0xFF263348),
                focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = accent)
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = favoritesOnly, onClick = { onFavoritesOnly(!favoritesOnly) }, label = { Text("★ Favorit") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            FilterChip(selected = fuzzySearch, onClick = { onFuzzySearch(!fuzzySearch) }, label = { Text(if (fuzzySearch) "Pencarian fuzzy" else "Pencarian normal") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            FilterChip(selected = sortDescending, onClick = { onSortDescending(!sortDescending) }, label = { Text(if (sortDescending) "Z → A" else "A → Z") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            FilterChip(selected = hideSystemApps, onClick = { onHideSystemApps(!hideSystemApps) }, label = { Text(if (hideSystemApps) "Sistem: disembunyikan" else "Aplikasi sistem") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            FilterChip(selected = recentOnly, onClick = { recentOnly = !recentOnly }, label = { Text("Baru dipakai") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            if (recentPackages.isNotEmpty()) TextButton(onClick = { recentPackages = emptyList(); drawerPrefs.edit().remove("recent_packages").apply(); recentOnly = false }) { Text("HAPUS RIWAYAT", color = accent, fontSize = 10.sp) }
            FilterChip(selected = gridMode, onClick = { gridMode = !gridMode; context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).edit().putBoolean("app_drawer_grid", gridMode).apply() }, label = { Text(if (gridMode) "Tampilan: grid" else "Tampilan: daftar") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            if (gridMode) FilterChip(selected = gridColumns > 2, onClick = { gridColumns = if (gridColumns >= 4) 2 else gridColumns + 1; context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).edit().putInt("app_drawer_columns", gridColumns).apply() }, label = { Text("Kolom: $gridColumns") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
            TextButton(onClick = { onSearch(""); onLetter(null); onFavoritesOnly(false); recentOnly = false }) { Text("RESET", color = accent, fontSize = 10.sp) }
            FilterChip(selected = showAlphabetIndex, onClick = { showAlphabetIndex = !showAlphabetIndex; context.getSharedPreferences("neo_launcher_preferences", Context.MODE_PRIVATE).edit().putBoolean("show_alphabet_index", showAlphabetIndex).apply() }, label = { Text(if (showAlphabetIndex) "A–Z: tampil" else "A–Z: sembunyi") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.2f), selectedLabelColor = accent))
        }
        val listState = rememberLazyListState()
        LaunchedEffect(apps, gridMode) { listState.scrollToItem(0) }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!gridMode) {
                LazyColumn(state = listState, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(visibleApps, key = { it.packageName }) { app ->
                        Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { launchAndRecord(app.packageName) },
                            onLongClick = { selectedAppForActions = app }
                        )) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AndroidView(factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(runCatching { ctx.packageManager.getApplicationIcon(app.packageName) }.getOrNull()) } },
                                    modifier = Modifier.size((28 * iconScale).dp))
                                if (showAppLabels) Text(app.label, color = if (accentAppLabels) accent else TextMain, fontSize = 14.sp * appLabelScale, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 12.dp).weight(1f)) else Spacer(Modifier.weight(1f))
                                Text(if (app.packageName in favoritePackages) "★" else "☆", color = if (app.packageName in favoritePackages) accent else TextMuted, fontSize = 21.sp,
                                    modifier = Modifier.clickable { onToggleFavorite(app.packageName) }.padding(horizontal = 5.dp))
                                Text("BUKA", color = accent, fontSize = 9.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                    if (visibleApps.isEmpty()) item {
                        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (recentOnly) "Belum ada aplikasi yang baru dipakai." else if (favoritesOnly) "Belum ada aplikasi favorit yang cocok." else "Tidak ada aplikasi yang cocok.", color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            TextButton(onClick = { onSearch(""); onLetter(null); onFavoritesOnly(false) }) {
                                Text("HAPUS FILTER", color = accent)
                            }
                        }
                    }
                }
    
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(gridColumns), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gridItems(visibleApps, key = { it.packageName }) { app ->
                        Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { launchAndRecord(app.packageName) },
                            onLongClick = { selectedAppForActions = app }
                        )) {
                            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                AndroidView(factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(runCatching { ctx.packageManager.getApplicationIcon(app.packageName) }.getOrNull()) } }, modifier = Modifier.size((38 * iconScale).dp))
                                if (showAppLabels) Text(app.label, color = if (accentAppLabels) accent else TextMain, fontSize = 12.sp * appLabelScale, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text(if (app.packageName in favoritePackages) "★ Favorit" else "☆ Favorit", color = if (app.packageName in favoritePackages) accent else TextMuted, fontSize = 10.sp, modifier = Modifier.clickable { onToggleFavorite(app.packageName) }.padding(3.dp))
                            }
                        }
                    }
                    if (visibleApps.isEmpty()) item {
                        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (favoritesOnly) "Belum ada aplikasi favorit yang cocok." else "Tidak ada aplikasi yang cocok.", color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            TextButton(onClick = { onSearch(""); onLetter(null); onFavoritesOnly(false) }) { Text("HAPUS FILTER", color = accent) }
                        }
                    }
                }
            }
            if (showAlphabetIndex) {
            Column(Modifier.width(24.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                    ("ABCDEFGHIJKLMNOPQRSTUVWXYZ#").forEach { letter ->
                        Text(letter.toString(), color = if (selectedLetter == letter) Night else accent, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(CircleShape).background(if (selectedLetter == letter) accent else Color.Transparent).clickable { onLetter(if (selectedLetter == letter) null else letter) }.padding(horizontal = 3.dp, vertical = 1.dp))
                    }
                }
            }
        }
    }
}

private fun fuzzyMatches(label: String, query: String): Boolean {
    if (query.isBlank()) return true
    val target = label.lowercase(Locale.getDefault())
    var index = 0
    for (character in query.lowercase(Locale.getDefault())) {
        index = target.indexOf(character, index)
        if (index < 0) return false
        index++
    }
    return true
}

private fun isBatteryCharging(context: Context): Boolean {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
    return when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
        BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
        else -> false
    }
}

private fun batteryPercent(context: Context): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return -1
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) (level * 100) / scale else -1
}

@Composable
private fun LauncherSettings(accentIndex: Int, onAccent: (Int) -> Unit, iconScale: Float, onIconScale: (Float) -> Unit, clockFaceIndex: Int, onClockFace: (Int) -> Unit, showSeconds: Boolean, onShowSeconds: (Boolean) -> Unit, use24Hour: Boolean, onUse24Hour: (Boolean) -> Unit, dateFormatIndex: Int, onDateFormat: (Int) -> Unit, showBatteryWidget: Boolean, onShowBatteryWidget: (Boolean) -> Unit, showAnalogClockWidget: Boolean, onShowAnalogClockWidget: (Boolean) -> Unit, showHomeDate: Boolean, onShowHomeDate: (Boolean) -> Unit, homeClockScale: Float, onHomeClockScale: (Float) -> Unit, homeCardOpacity: Float, onHomeCardOpacity: (Float) -> Unit, analogClockScale: Float, onAnalogClockScale: (Float) -> Unit, digitalClockWeight: Int, onDigitalClockWeight: (Int) -> Unit, handStyleIndex: Int, onHandStyle: (Int) -> Unit, showAppLabels: Boolean, onShowAppLabels: (Boolean) -> Unit, hideSystemApps: Boolean, onHideSystemApps: (Boolean) -> Unit, homeBackgroundIndex: Int, onHomeBackground: (Int) -> Unit, accentAppLabels: Boolean, onAccentAppLabels: (Boolean) -> Unit, appLabelScale: Float, onAppLabelScale: (Float) -> Unit, showGreeting: Boolean, onShowGreeting: (Boolean) -> Unit, accentDigitalClock: Boolean, onAccentDigitalClock: (Boolean) -> Unit, onBack: () -> Unit, onAndroidSettings: () -> Unit) {
    val accents = listOf(Cyan, Violet, Lime, Amber)
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        HorizontalDivider(color = Color(0xFF263348))
        Text("APP LIBRARY", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tampilkan nama aplikasi", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Sembunyikan label untuk tampilan lebih minimal", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showAppLabels, onCheckedChange = onShowAppLabels, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sembunyikan aplikasi sistem", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Filter aplikasi yang ditandai sebagai aplikasi sistem Android", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = hideSystemApps, onCheckedChange = onHideSystemApps, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tampilkan salam beranda", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Sembunyikan teks pembuka untuk tampilan lebih minimal", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showGreeting, onCheckedChange = onShowGreeting, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Warna jam digital mengikuti aksen", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Gunakan warna tema untuk angka jam utama", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = accentDigitalClock, onCheckedChange = onAccentDigitalClock, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Text("LATAR BERANDA", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Neo", "Violet", "Teal", "Amber").forEachIndexed { index, label ->
                val preview = when (index) { 1 -> Color(0xFF211638); 2 -> Color(0xFF082A2A); 3 -> Color(0xFF302014); else -> Night }
                Surface(color = preview, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).clickable { onHomeBackground(index) }, border = androidx.compose.foundation.BorderStroke(if (homeBackgroundIndex == index) 2.dp else 1.dp, if (homeBackgroundIndex == index) accents[accentIndex] else Color(0xFF263348))) {
                    Text(label, color = TextMain, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
        HorizontalDivider(color = Color(0xFF263348))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Warna label aplikasi mengikuti aksen", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Gunakan warna tema pada nama aplikasi di drawer", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = accentAppLabels, onCheckedChange = onAccentAppLabels, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Text("UKURAN TEKS LABEL APLIKASI", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("${(appLabelScale * 100).toInt()}%", color = accents[accentIndex], fontSize = 20.sp)
        Slider(value = appLabelScale, onValueChange = onAppLabelScale, valueRange = 0.8f..1.4f, colors = SliderDefaults.colors(thumbColor = accents[accentIndex], activeTrackColor = accents[accentIndex]))
        HorizontalDivider(color = Color(0xFF263348))
        Text("UKURAN JAM ANALOG", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("${(analogClockScale * 100).toInt()}%", color = accents[accentIndex], fontSize = 20.sp)
        Slider(value = analogClockScale, onValueChange = onAnalogClockScale, valueRange = 0.7f..1.5f, colors = SliderDefaults.colors(thumbColor = accents[accentIndex], activeTrackColor = accents[accentIndex]))
        HorizontalDivider(color = Color(0xFF263348))
        Text("KETEBALAN ANGKA JAM", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tipis", "Normal", "Tebal").forEachIndexed { index, label ->
                Surface(color = if (digitalClockWeight == index) accents[accentIndex].copy(alpha = 0.2f) else Panel, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).clickable { onDigitalClockWeight(index) }, border = androidx.compose.foundation.BorderStroke(if (digitalClockWeight == index) 1.dp else 0.dp, accents[accentIndex])) {
                    Text(label, color = if (digitalClockWeight == index) accents[accentIndex] else TextMuted, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
        HorizontalDivider(color = Color(0xFF263348))
        Text("TRANSPARANSI KARTU JAM", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("${(homeCardOpacity * 100).toInt()}%", color = accents[accentIndex], fontSize = 20.sp)
        Slider(value = homeCardOpacity, onValueChange = onHomeCardOpacity, valueRange = 0.35f..1f, colors = SliderDefaults.colors(thumbColor = accents[accentIndex], activeTrackColor = accents[accentIndex]))
        HorizontalDivider(color = Color(0xFF263348))
        Text("UKURAN JAM DIGITAL", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("${(homeClockScale * 100).toInt()}%", color = accents[accentIndex], fontSize = 20.sp)
        Slider(value = homeClockScale, onValueChange = onHomeClockScale, valueRange = 0.8f..1.4f, colors = SliderDefaults.colors(thumbColor = accents[accentIndex], activeTrackColor = accents[accentIndex]))
        HorizontalDivider(color = Color(0xFF263348))
        Text("FORMAT JAM", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(true to "24 JAM", false to "12 JAM").forEach { (is24, label) ->
                Surface(color = if (use24Hour == is24) accents[accentIndex].copy(alpha = 0.2f) else Panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).clickable { onUse24Hour(is24) }, border = androidx.compose.foundation.BorderStroke(if (use24Hour == is24) 1.dp else 0.dp, accents[accentIndex])) {
                    Text(label, color = if (use24Hour == is24) accents[accentIndex] else TextMuted, modifier = Modifier.padding(13.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tampilkan detik", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Tampilkan atau sembunyikan detik", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showSeconds, onCheckedChange = onShowSeconds, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Widget baterai", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Tampilkan indikator baterai di beranda", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showBatteryWidget, onCheckedChange = onShowBatteryWidget, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tanggal di widget jam", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Tampilkan atau sembunyikan tanggal di bawah jam digital", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showHomeDate, onCheckedChange = onShowHomeDate, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Jam analog di beranda", color = TextMain, fontWeight = FontWeight.Medium)
                Text("Tampilkan atau sembunyikan jam analog", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = showAnalogClockWidget, onCheckedChange = onShowAnalogClockWidget, colors = SwitchDefaults.colors(checkedTrackColor = accents[accentIndex]))
        }
        Text("FORMAT TANGGAL", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Lengkap", "Singkat", "Angka").forEachIndexed { index, label ->
                Surface(color = if (dateFormatIndex == index) accents[accentIndex].copy(alpha = 0.2f) else Panel, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).clickable { onDateFormat(index) }, border = androidx.compose.foundation.BorderStroke(if (dateFormatIndex == index) 1.dp else 0.dp, accents[accentIndex])) {
                    Text(label, color = if (dateFormatIndex == index) accents[accentIndex] else TextMuted, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
        Text("DESAIN JAM ANALOG", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            (0..4).forEach { index ->
                Surface(color = if (clockFaceIndex == index) accents[accentIndex].copy(alpha = 0.2f) else Panel, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).clickable { onClockFace(index) }, border = androidx.compose.foundation.BorderStroke(if (clockFaceIndex == index) 1.dp else 0.dp, accents[accentIndex])) {
                    Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = when (index) {
                            0 -> com.neo.launcher.R.drawable.clock_face_01
                            1 -> com.neo.launcher.R.drawable.clock_face_02
                            2 -> com.neo.launcher.R.drawable.clock_face_03
                            3 -> com.neo.launcher.R.drawable.clock_face_04
                            else -> com.neo.launcher.R.drawable.clock_face_05
                        }), contentDescription = "Desain jam ${index + 1}", modifier = Modifier.size(30.dp))
                        Text("${index + 1}", color = accents[accentIndex], fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("GAYA JARUM JAM", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("Pilih warna dan ketebalan jarum analog", color = TextMuted, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Neo", "Emas", "Neon", "Gelap").forEachIndexed { index, label ->
                Surface(
                    color = if (handStyleIndex == index) accents[accentIndex].copy(alpha = 0.2f) else Panel,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).clickable { onHandStyle(index) },
                    border = androidx.compose.foundation.BorderStroke(if (handStyleIndex == index) 1.dp else 0.dp, accents[accentIndex])
                ) {
                    Text(label, color = if (handStyleIndex == index) accents[accentIndex] else TextMuted, fontSize = 10.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
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
        Text("Preferensi tema, ukuran ikon, dan jam disimpan otomatis di perangkat ini.", color = TextMuted, fontSize = 11.sp)
    }
}
