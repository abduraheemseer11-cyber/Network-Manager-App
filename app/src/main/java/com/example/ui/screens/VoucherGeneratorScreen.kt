package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GeneratedVoucher
import com.example.ui.theme.BronzeLight
import com.example.ui.theme.BronzePrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.StatusOnline
import com.example.ui.viewmodel.NetworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherGeneratorScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val generatorState by viewModel.generatorState.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val allVouchers by viewModel.allVouchers.collectAsState()
    val activeRouter by viewModel.activeRouter.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var voucherCardStyle by remember { mutableStateOf("normal") } // "normal" or "thermal"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("voucher_generator_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("توليد الكروت (Generator)") },
                icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("معاينة وطباعة (${if (generatorState.lastGeneratedVouchers.isNotEmpty()) generatorState.lastGeneratedVouchers.size else allVouchers.size})") },
                icon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 0) {
            // Form Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "مولّد كروت الهوتسبوت بالجملة (Mikhmon v3)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "قم بتوليد كروت اشتراك دفعة واحدة وتحديد الباقة ونوع كود الدخول.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    // Mode Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("نمط كود تسجيل الدخول", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = generatorState.userMode == "same",
                                    onClick = { viewModel.updateGeneratorUserMode("same") },
                                    label = { Text("اسم المستخدم = كلمة المرور") },
                                    leadingIcon = if (generatorState.userMode == "same") {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                FilterChip(
                                    selected = generatorState.userMode == "different",
                                    onClick = { viewModel.updateGeneratorUserMode("different") },
                                    label = { Text("مستخدم + كلمة سر") },
                                    leadingIcon = if (generatorState.userMode == "different") {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                item {
                    // Quantity Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("عدد الكروت المطلوبة: ${generatorState.quantity}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 20, 50, 100).forEach { qty ->
                                    FilterChip(
                                        selected = generatorState.quantity == qty,
                                        onClick = { viewModel.updateGeneratorQuantity(qty) },
                                        label = { Text("$qty") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Prefix & Length
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = generatorState.prefix,
                            onValueChange = { viewModel.updateGeneratorPrefix(it) },
                            label = { Text("بادئة الكود (Prefix)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = generatorState.length.toString(),
                            onValueChange = {
                                val l = it.toIntOrNull() ?: 4
                                viewModel.updateGeneratorLength(l.coerceIn(3, 10))
                            },
                            label = { Text("طول الكود") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    // Profile Selection Dropdown
                    var isProfExpanded by remember { mutableStateOf(false) }
                    val selectedProfName = profiles.getOrNull(generatorState.selectedProfileIndex)?.name ?: "اختر باقة"

                    ExposedDropdownMenuBox(
                        expanded = isProfExpanded,
                        onExpandedChange = { isProfExpanded = !isProfExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedProfName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("باقة الاشتراك (User Profile)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProfExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isProfExpanded,
                            onDismissRequest = { isProfExpanded = false }
                        ) {
                            profiles.forEachIndexed { index, profile ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(profile.name, fontWeight = FontWeight.Bold)
                                            Text("السرعة: ${profile.rateLimit} | السعر: ${profile.price} ${activeRouter?.currency ?: "ر.ي"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateGeneratorProfileIndex(index)
                                        viewModel.updateGeneratorTimeLimit(profile.validity)
                                        isProfExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Time limit & Comment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = generatorState.timeLimit,
                            onValueChange = { viewModel.updateGeneratorTimeLimit(it) },
                            label = { Text("صلاحية الكارت (مثال 1d)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = generatorState.comment,
                            onValueChange = { viewModel.updateGeneratorComment(it) },
                            label = { Text("ملاحظة الدفعة") },
                            placeholder = { Text("دفعة كروت كافيه") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.generateVouchers()
                            selectedTab = 1
                        },
                        enabled = !generatorState.isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_vouchers_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BronzePrimary)
                    ) {
                        if (generatorState.isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري توليد الكروت وحفظها في الراوتر...")
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("توليد ${generatorState.quantity} كارت الآن", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Preview & Print Tab
            val vouchersToShow = if (generatorState.lastGeneratedVouchers.isNotEmpty()) {
                generatorState.lastGeneratedVouchers
            } else {
                allVouchers
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Style selector & Share actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = voucherCardStyle == "normal",
                            onClick = { voucherCardStyle = "normal" },
                            label = { Text("كارت ملون") }
                        )
                        FilterChip(
                            selected = voucherCardStyle == "thermal",
                            onClick = { voucherCardStyle = "thermal" },
                            label = { Text("طابعة حرارية") }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val text = buildString {
                                appendLine("=== كروت شبكة ${activeRouter?.hotspotName ?: "ميكروتك"} ===")
                                vouchersToShow.forEach { v ->
                                    appendLine("المستخدم: ${v.username} | كلمة السر: ${v.password} | الباقة: ${v.profileName} | السعر: ${v.price}")
                                }
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "كروت شبكة ميكروتك")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الكروت"))
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة الكروت", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (vouchersToShow.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد كروت مولّدة حالياً، قم بتوليد دفعة جديدة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (voucherCardStyle == "normal") 150.dp else 130.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(vouchersToShow) { voucher ->
                            if (voucherCardStyle == "normal") {
                                NormalVoucherCard(
                                    voucher = voucher,
                                    hotspotName = activeRouter?.hotspotName ?: "شبكة ميكروتك",
                                    currency = activeRouter?.currency ?: "ر.ي"
                                )
                            } else {
                                ThermalVoucherCard(
                                    voucher = voucher,
                                    hotspotName = activeRouter?.hotspotName ?: "شبكة ميكروتك"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NormalVoucherCard(
    voucher: GeneratedVoucher,
    hotspotName: String,
    currency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, BronzeLight.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hotspotName.take(12),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BronzePrimary
                )
                Text(
                    text = "${voucher.price} $currency",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StatusOnline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = voucher.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (voucher.password != voucher.username && voucher.password.isNotEmpty()) {
                        Text(
                            text = "كلمة السر: ${voucher.password}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = voucher.profileName.take(10),
                    fontSize = 10.sp,
                    color = CyanSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ThermalVoucherCard(
    voucher: GeneratedVoucher,
    hotspotName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "=== $hotspotName ===",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "الباقة: ${voucher.profileName}",
                fontSize = 9.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = voucher.username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
            }
            if (voucher.password != voucher.username && voucher.password.isNotEmpty()) {
                Text(
                    text = "كلمة المرور: ${voucher.password}",
                    fontSize = 9.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "السعر: ${voucher.price} | الصلاحية: ${voucher.timeLimit}",
                fontSize = 8.sp,
                color = Color.Black
            )
        }
    }
}
