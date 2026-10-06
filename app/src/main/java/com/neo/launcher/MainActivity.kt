package com.neo.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class AppInfo(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoLauncherApp() }
    }
}

@Composable
fun NeoLauncherApp() {
    val context = LocalContext.current
    var time by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = pm.queryIntentActivities(intent, 0)
            .map { AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF080A0F)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("neo@android:~$", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("────────────────────────", color = Color(0xFF39414D), fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(18.dp))
                Text(time, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 42.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("> ", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                    Text("apps", color = Color(0xFFE5C07B), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                }
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(2.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(apps) { app ->
                        Row(Modifier.fillMaxWidth().clickable {
                            context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                        }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("›", color = Color(0xFFC678DD), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(app.label, color = Color(0xFFE6E6E6), fontFamily = FontFamily.Monospace, fontSize = 15.sp, maxLines = 1)
                        }
                    }
                }
                Text("────────────────────────", color = Color(0xFF39414D), fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("WiFi ●", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Neo Launcher v0.1", color = Color(0xFF7F8793), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth().clickable { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("> ", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace)
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFFABB2BF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("settings", color = Color(0xFFABB2BF), fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
