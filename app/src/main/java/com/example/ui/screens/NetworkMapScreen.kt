package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NetworkDevice
import com.example.ui.theme.BronzePrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.NavyTertiary
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.NetworkViewModel

@Composable
fun NetworkMapScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.allNetworkDevices.collectAsState()
    var isSatelliteView by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<NetworkDevice?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("network_map_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BronzePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة جهاز للخريطة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Map Header & View Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "خريطة الأجهزة وتوزيع الأبراج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مواقع الراوترات والأبراج والمشتركين المتصلين",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isSatelliteView,
                        onClick = { isSatelliteView = true },
                        label = { Text("قمر صناعي") },
                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = !isSatelliteView,
                        onClick = { isSatelliteView = false },
                        label = { Text("تخطيطي") },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map Visual Canvas Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSatelliteView) {
                            Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        }
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSatelliteView) "عرض الخريطة الفضائية (Satellite GPS View)" else "خريطة التوزيع الطوبولوجي (Network Topology)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${devices.size} جهاز مسجل",
                            color = CyanSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Simulated node points on canvas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        devices.take(4).forEach { dev ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedDevice = dev }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (dev.status) {
                                                "ONLINE" -> StatusOnline.copy(alpha = 0.85f)
                                                "WARNING" -> StatusWarning.copy(alpha = 0.85f)
                                                else -> StatusOffline.copy(alpha = 0.85f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (dev.type) {
                                            "SECTOR_ANTENNA" -> Icons.Default.CellTower
                                            "ACCESS_POINT" -> Icons.Default.Wifi
                                            else -> Icons.Default.Router
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dev.name.take(12),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${dev.connectedClients} عميل",
                                    color = CyanSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "قائمة الأجهزة والقطاعات على الخريطة",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(devices, key = { it.id }) { device ->
                    NetworkDeviceCard(
                        device = device,
                        onClick = { selectedDevice = device }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { dev ->
                viewModel.saveNetworkDevice(dev)
                showAddDialog = false
            }
        )
    }

    selectedDevice?.let { dev ->
        AlertDialog(
            onDismissRequest = { selectedDevice = null },
            title = { Text(dev.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("النوع: ${dev.type}")
                    Text("الحالة: ${dev.status}")
                    Text("عنوان IP: ${dev.ipAddress}")
                    Text("المشتركين المتصلين: ${dev.connectedClients} عميل")
                    Text("الإحداثيات: ${dev.latitude}, ${dev.longitude}")
                    if (dev.notes.isNotEmpty()) {
                        Text("ملاحظات: ${dev.notes}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDevice = null }) {
                    Text("إغلاق")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNetworkDevice(dev)
                        selectedDevice = null
                    }
                ) {
                    Text("حذف الجهاز", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun NetworkDeviceCard(
    device: NetworkDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (device.status) {
                            "ONLINE" -> StatusOnline.copy(alpha = 0.15f)
                            "WARNING" -> StatusWarning.copy(alpha = 0.15f)
                            else -> StatusOffline.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (device.type) {
                        "SECTOR_ANTENNA" -> Icons.Default.CellTower
                        "ACCESS_POINT" -> Icons.Default.Wifi
                        else -> Icons.Default.Router
                    },
                    contentDescription = null,
                    tint = when (device.status) {
                        "ONLINE" -> StatusOnline
                        "WARNING" -> StatusWarning
                        else -> StatusOffline
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "IP: ${device.ipAddress} | إحداثيات: ${device.latitude}, ${device.longitude}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${device.connectedClients} عميل",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyanSecondary
                )
                Text(
                    text = if (device.status == "ONLINE") "متصل" else "تنبيه",
                    fontSize = 10.sp,
                    color = if (device.status == "ONLINE") StatusOnline else StatusWarning
                )
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (NetworkDevice) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ACCESS_POINT") }
    var ipAddress by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("24.7136") }
    var lng by remember { mutableStateOf("46.6753") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة نقطة / برج شبكة جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم النقطة أو البرج") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("عنوان IP للجهاز") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        label = { Text("خط العرض Lat") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lng,
                        onValueChange = { lng = it },
                        label = { Text("خط الطول Lng") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            NetworkDevice(
                                name = name,
                                type = type,
                                latitude = lat.toDoubleOrNull() ?: 24.7136,
                                longitude = lng.toDoubleOrNull() ?: 46.6753,
                                ipAddress = ipAddress,
                                notes = notes
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BronzePrimary)
            ) {
                Text("حفظ الجهاز")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
