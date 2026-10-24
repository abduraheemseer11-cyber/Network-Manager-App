package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BronzeLight
import com.example.ui.theme.BronzePrimary
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val activeRouter by viewModel.activeRouter.collectAsState()
    val isLiveConnected by viewModel.isLiveConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val statusMessage by viewModel.connectionStatusMessage.collectAsState()
    val systemResource by viewModel.systemResource.collectAsState()
    val trafficStats by viewModel.trafficStats.collectAsState()
    val activeUsers by viewModel.activeUsers.collectAsState()
    val allUsers by viewModel.filteredUsers.collectAsState()

    var showRebootDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Router Connection Status Banner
            RouterConnectionBanner(
                routerName = activeRouter?.name ?: "راوتر ميكروتك الرئيسي",
                ip = activeRouter?.ipAddress ?: "192.168.88.1",
                port = activeRouter?.apiPort ?: 8728,
                isLive = isLiveConnected,
                isConnecting = isConnecting,
                statusMessage = statusMessage,
                onReconnect = { viewModel.reconnect() }
            )
        }

        item {
            // Live Traffic Monitor Card (Mikhmon Traffic Graph)
            LiveTrafficCard(
                rxFormatted = trafficStats.formattedRx(),
                txFormatted = trafficStats.formattedTx(),
                interfaceName = trafficStats.interfaceName
            )
        }

        item {
            // Hotspot Key Numbers Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "المتصلين الآن",
                    value = "${activeUsers.size}",
                    subValue = "جلسة نشطة",
                    icon = Icons.Default.Wifi,
                    color = CyanSecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(ScreenDestination.ACTIVE_USERS)
                }

                StatCard(
                    title = "إجمالي الكروت",
                    value = "${allUsers.size}",
                    subValue = "مستخدم في الهوتسبوت",
                    icon = Icons.Default.People,
                    color = BronzePrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(ScreenDestination.USERS)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "مبيعات اليوم",
                    value = "12,500",
                    subValue = activeRouter?.currency ?: "ر.ي",
                    icon = Icons.Default.AttachMoney,
                    color = StatusOnline,
                    modifier = Modifier.weight(1f)
                ) {}

                StatCard(
                    title = "وقت التشغيل",
                    value = systemResource.uptime.take(8),
                    subValue = systemResource.version,
                    icon = Icons.Default.Speed,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                ) {}
            }
        }

        item {
            // System Hardware Resource Gauges
            SystemResourcesCard(systemResource = systemResource)
        }

        item {
            // Quick Shortcuts Grid
            QuickActionsCard(
                onGenerateVouchers = { viewModel.navigateTo(ScreenDestination.VOUCHERS) },
                onAddUser = { viewModel.navigateTo(ScreenDestination.USERS) },
                onViewProfiles = { viewModel.navigateTo(ScreenDestination.PROFILES) },
                onReboot = { showRebootDialog = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text("إعادة تشغيل الراوتر", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في إعادة تشغيل راوتر ميكروتك؟ سيتم قطع اتصال المشتركين مؤقتاً.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRebootDialog = false
                        viewModel.rebootRouter()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("إعادة التشغيل الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun RouterConnectionBanner(
    routerName: String,
    ip: String,
    port: Int,
    isLive: Boolean,
    isConnecting: Boolean,
    statusMessage: String,
    onReconnect: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                (if (isLive) StatusOnline else StatusWarning).copy(
                                    alpha = if (isConnecting) alphaAnim else 1f
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = routerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onReconnect,
                    modifier = Modifier.testTag("reconnect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة الاتصال",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "المضيف: $ip | منفذ API: $port",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLive) StatusOnline else StatusWarning
            )
        }
    }
}

@Composable
fun LiveTrafficCard(
    rxFormatted: String,
    txFormatted: String,
    interfaceName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(CyanLight, BronzeLight)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مراقبة سرعة البيانات المباشرة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = interfaceName,
                    fontSize = 12.sp,
                    color = CyanSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Download Rx
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "تنزيل",
                            tint = CyanSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تنزيل (Rx)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rxFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanSecondary
                    )
                }

                // Upload Tx
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "رفع",
                            tint = BronzePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "رفع (Tx)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = txFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BronzePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("stat_${title}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subValue,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SystemResourcesCard(systemResource: com.example.data.model.SystemResource) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "موارد وعتاد الراوتر (System)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CPU Load
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("جهد المعالج (CPU)", fontSize = 13.sp)
                Text("${systemResource.cpuLoad}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { systemResource.cpuLoad / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (systemResource.cpuLoad > 75) MaterialTheme.colorScheme.error else CyanSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Memory RAM
            val usedMem = (systemResource.totalMemoryMb - systemResource.freeMemoryMb).coerceAtLeast(0)
            val memProgress = (usedMem.toFloat() / systemResource.totalMemoryMb.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الذاكرة (RAM)", fontSize = 13.sp)
                Text(
                    "المتاح: ${systemResource.freeMemoryMb} MB / ${systemResource.totalMemoryMb} MB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { memProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BronzePrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Storage HDD
            val usedHdd = (systemResource.totalHddMb - systemResource.freeHddMb).coerceAtLeast(0)
            val hddProgress = (usedHdd.toFloat() / systemResource.totalHddMb.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("التخزين (HDD)", fontSize = 13.sp)
                Text(
                    "المتاح: ${systemResource.freeHddMb} MB / ${systemResource.totalHddMb} MB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { hddProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusOnline
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("طراز اللوحة: ${systemResource.boardName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("نظام: ${systemResource.version}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onGenerateVouchers: () -> Unit,
    onAddUser: () -> Unit,
    onViewProfiles: () -> Unit,
    onReboot: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "الإجراءات السريعة",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGenerateVouchers,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_generate_vouchers"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BronzePrimary)
                ) {
                    Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توليد كروت", fontSize = 13.sp)
                }

                Button(
                    onClick = onAddUser,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مستخدم جديد", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfiles,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("باقات الاشتراك", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onReboot,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إعادة تشغيل", fontSize = 13.sp)
                }
            }
        }
    }
}
