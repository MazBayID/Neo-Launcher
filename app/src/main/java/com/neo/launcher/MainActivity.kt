package com.neo.launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Void = Color(0xFF070B12)
private val Panel = Color(0xFF101824)
private val Mint = Color(0xFF71F6C5)
private val Cyan = Color(0xFF67DDF5)
private val Muted = Color(0xFF8293A8)
private val White = Color(0xFFF2F7FC)

private data class LauncherApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 11, 18)
        window.navigationBarColor = android.graphics.Color.rgb(7, 11, 18)
        setContent { NeoLauncher() }
    }
}

@Composable
private fun NeoLauncher() {
    val context = LocalContext.current
    var now by remember { mutableStateOf(Date()) }
    var drawerOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var allApps by remember { mutableStateOf(emptyList<LauncherApp>()) }

    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000) }
    }
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = pm.queryIntentActivities(intent, 0)
            .map { LauncherApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.activityInfo.loadIcon(pm)) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
    val time = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val date = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now) }
    val filtered = remember(allApps, query) { allApps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) } }

    MaterialTheme(colorScheme = darkColorScheme(background = Void, surface = Panel, primary = Mint, secondary = Cyan, onBackground = White, onSurface = White)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101F2C), Void, Color(0xFF090D15))))) {
            if (drawerOpen) {
                AppDrawer(filtered, query, { query = it }, { drawerOpen = false; query = "" }) { app ->
                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                }
            } else {
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Mint))
                        Spacer(Modifier.width(9.dp))
                        Text("NEO / HOME", color = White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Wifi, null, tint = Mint, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("ONLINE", color = Muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(32.dp))
                    Text("YOUR SPACE.", color = Muted, fontSize = 11.sp, letterSpacing = 3.sp, fontFamily = FontFamily.Monospace)
                    Text(time, color = White, fontSize = 76.sp, lineHeight = 78.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = (-3).sp)
                    Text(date.uppercase(Locale.getDefault()), color = Mint, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(28.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusTile("SYSTEM", "READY", "01", Modifier.weight(1f))
                        StatusTile("APPLICATIONS", allApps.size.toString().padStart(2, '0'), "02", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("QUICK ACCESS", color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                            Text("Your apps, one tap away", color = Muted, fontSize = 11.sp)
                        }
                        Text("VIEW ALL ↗", color = Mint, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { drawerOpen = true }.padding(10.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(allApps.take(12), key = { it.packageName }) { app ->
                            AppTile(app) { context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).clickable { drawerOpen = true }.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Mint, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Search apps and tools", color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Apps, null, tint = Muted, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("N / L", color = Mint, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("NEO LAUNCHER • REDESIGN 01", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Settings, "Android settings", tint = Muted, modifier = Modifier.size(17.dp).clickable { context.startActivity(Intent(Settings.ACTION_SETTINGS)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTile(label: String, value: String, index: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Panel).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(index, color = Mint, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(7.dp))
            Text(label, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(10.dp))
        Text(value, color = White, fontSize = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

@Composable
private fun AppTile(app: LauncherApp, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color(0x66131F2C)).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(bitmap = app.icon.toBitmap(width = 144, height = 144).asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(8.dp))
        Text(app.label, color = White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun AppDrawer(apps: List<LauncherApp>, query: String, onQuery: (String) -> Unit, onBack: () -> Unit, onLaunch: (LauncherApp) -> Unit) {
    val alphabet = listOf("#") + ('A'..'Z').map { it.toString() }
    val activeLetter = query.takeIf { it.length == 1 && it[0].isLetter() }?.uppercase(Locale.ROOT)
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = Mint, fontSize = 24.sp,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 14.dp))
            Column(Modifier.weight(1f)) {
                Text("APP LIBRARY", color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("${apps.size} APPLICATIONS", color = Muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Text("NEO / 01", color = Mint, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps…", color = Muted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Mint) },
            trailingIcon = { if (query.isNotEmpty()) Text("×", color = Mint, fontSize = 20.sp,
                modifier = Modifier.clickable { onQuery("") }.padding(horizontal = 12.dp)) },
            singleLine = true, shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = White, unfocusedTextColor = White,
                focusedBorderColor = Mint, unfocusedBorderColor = Color(0xFF283747),
                cursorColor = Mint, focusedContainerColor = Panel, unfocusedContainerColor = Panel
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 82.dp), modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                items(apps, key = { it.packageName }) { app -> AppTile(app) { onLaunch(app) } }
            }
            Column(
                modifier = Modifier.width(22.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                alphabet.forEach { letter ->
                    val selected = if (letter == "#") query.isEmpty() else activeLetter == letter
                    Text(
                        text = letter,
                        color = if (selected) Mint else Muted,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.clickable {
                            onQuery(if (letter == "#") "" else letter)
                        }.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
