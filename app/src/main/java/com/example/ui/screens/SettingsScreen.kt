package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RouterConfig
import com.example.ui.theme.BronzePrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.StatusOnline
import com.example.ui.viewmodel.NetworkViewModel

@Composable
fun SettingsScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val routers by viewModel.allRouters.collectAsState()
    val activeRouter by viewModel.activeRouter.collectAsState()

    var showAddRouterDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إدارة أجهزة الراوتر (Router Sessions)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "إضافة وتبديل الاتصال بين أجهزة راوتر ميكروتك المختلفة",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddRouterDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BronzePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة راوتر", fontSize = 12.sp)
                }
            }
        }

        items(routers, key = { it.id }) { router ->
            val isActive = activeRouter?.id == router.id
            RouterConfigItemCard(
                router = router,
                isActive = isActive,
                onSelect = { viewModel.selectRouter(router) },
                onDelete = { viewModel.deleteRouter(router) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            // About App Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حول تطبيق إدارة الشبكة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تم تحويل هذا التطبيق من مشروع Mikhmon v3 الشهير إلى تطبيق أندرويد نيتف أصيل (Native Android) باستخدام Kotlin و Jetpack Compose مع دعم كامل للغة العربية ومظهر حديث متجاوب لمديري شبكات ميكروتك والمشتركين.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "الإصدار: 3.2.0-Android | RouterOS API v6/v7",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BronzePrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddRouterDialog) {
        AddRouterDialog(
            onDismiss = { showAddRouterDialog = false },
            onConfirm = { router ->
                viewModel.saveRouter(router)
                showAddRouterDialog = false
            }
        )
    }
}

@Composable
fun RouterConfigItemCard(
    router: RouterConfig,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isActive) StatusOnline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = if (isActive) StatusOnline else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = router.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "النشط حالياً",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusOnline,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusOnline.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${router.ipAddress}:${router.apiPort} | المستخدم: ${router.username}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "اسم الهوتسبوت: ${router.hotspotName} (${router.currency})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                if (!isActive) {
                    IconButton(onClick = onSelect) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "تحديد", tint = CyanSecondary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddRouterDialog(
    onDismiss: () -> Unit,
    onConfirm: (RouterConfig) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("192.168.88.1") }
    var port by remember { mutableStateOf("8728") }
    var user by remember { mutableStateOf("admin") }
    var pass by remember { mutableStateOf("") }
    var hotspotName by remember { mutableStateOf("شبكة ميكروتك") }
    var currency by remember { mutableStateOf("ر.ي") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة راوتر ميكروتك جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم جلسة الراوتر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("عنوان IP / المضيف") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("المنفذ") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("اسم المستخدم") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hotspotName,
                        onValueChange = { hotspotName = it },
                        label = { Text("اسم الهوتسبوت") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("العملة") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && ip.isNotBlank()) {
                        onConfirm(
                            RouterConfig(
                                name = name,
                                ipAddress = ip,
                                apiPort = port.toIntOrNull() ?: 8728,
                                username = user,
                                password = pass,
                                hotspotName = hotspotName,
                                currency = currency,
                                isActive = false
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && ip.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BronzePrimary)
            ) {
                Text("حفظ والاتصال")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
