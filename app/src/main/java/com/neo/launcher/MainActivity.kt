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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max

data class AppInfo(val label: String, val packageName: String, val icon: Drawable)

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
    var laciTerbuka by remember { mutableStateOf(false) } 

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
            .map {
                AppInfo(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.activityInfo.loadIcon(pm)
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    MaterialTheme {
        if (laciTerbuka) {
            LaciAplikasi(apps = apps, onClose = { laciTerbuka = false })
        } else {
            Surface(Modifier.fillMaxSize(), color = Color(0xFF080A0F)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("neo@android:~$", color = Color(0xFF63E6BE), fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("────────────────────────", color = Color(0xFF39414D), fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(18.dp))
                    Text(time, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 42.sp, fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(18.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { laciTerbuka = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("> ", color = Color(0xFF66D9EF), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                        Text("apps", color = Color(0xFFE5C07B), fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    
                    LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(2.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        items(apps.take(15)) { app ->
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
}

@Composable
fun LaciAplikasi(apps: List<AppInfo>, onClose: () -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    val abjad = ('A'..'Z').toList()
    var indexSentuh by remember { mutableStateOf<Int?>(null) }
    
    Box(Modifier.fillMaxSize().background(Color(0xFF080A0F))) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(end = 50.dp), 
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "← Kembali", 
                    color = Color(0xFFE5C07B), 
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onClose() }.padding(bottom = 16.dp)
                )
            }
            items(apps) { app ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                    }.padding(vertical = 12.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = app.icon.toBitmap(width = 120, height = 120).asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(app.label, color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        BoxWithConstraints(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(50.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        
                        fun updateHuruf(posY: Float, tinggiTotal: Float) {
                            val rasio = (posY / tinggiTotal).coerceIn(0f, 0.99f)
                            val idx = (rasio * abjad.size).toInt()
                            indexSentuh = idx
                            
                            val hurufPilihan = abjad[idx].toString()
                            val targetIndex = apps.indexOfFirst { it.label.startsWith(hurufPilihan, ignoreCase = true) }
                            if (targetIndex >= 0) {
                                scope.launch {
                                    listState.scrollToItem(targetIndex + 1)
                                }
                            }
                        }
                        
                        updateHuruf(down.position.y, size.height.toFloat())
                        
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    updateHuruf(change.position.y, size.height.toFloat())
                                }
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                        
                        indexSentuh = null 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                abjad.forEachIndexed { i, char ->
                    val jarak = if (indexSentuh != null) abs(indexSentuh!! - i) else 100
                    val geserX = max(0f, 60f - (jarak * 15f)) 
                    
                    Text(
                        text = char.toString(),
                        color = if (jarak == 0) Color(0xFF63E6BE) else Color(0xFF7F8793),
                        fontSize = if (jarak == 0) 18.sp else 12.sp,
                        fontWeight = if (jarak == 0) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(x = (-geserX).dp)
                    )
                }
            }
        }
    }
}
