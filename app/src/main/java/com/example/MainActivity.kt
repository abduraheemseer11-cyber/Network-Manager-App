package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.mikrotik.NetworkRepository
import com.example.ui.screens.ActiveUsersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NetworkMapScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UsersScreen
import com.example.ui.screens.VoucherGeneratorScreen
import com.example.ui.theme.BronzeLight
import com.example.ui.theme.BronzePrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.NetworkViewModelFactory
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: NetworkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        repository = NetworkRepository(
            routerDao = database.routerDao(),
            voucherDao = database.voucherDao(),
            networkDeviceDao = database.networkDeviceDao(),
            coroutineScope = lifecycleScope
        )

        setContent {
            val viewModel: NetworkViewModel = viewModel(
                factory = NetworkViewModelFactory(repository)
            )

            MyApplicationTheme {
                // Ensure RTL layout for authentic Arabic experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavItem(
    val destination: ScreenDestination,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: NetworkViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeRouter by viewModel.activeRouter.collectAsState()
    val isLiveConnected by viewModel.isLiveConnected.collectAsState()
    val activeUsers by viewModel.activeUsers.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    BackHandler(enabled = currentScreen != ScreenDestination.DASHBOARD) {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val navItems = listOf(
        NavItem(ScreenDestination.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
        NavItem(ScreenDestination.USERS, "المستخدمين", Icons.Default.People),
        NavItem(ScreenDestination.VOUCHERS, "الكروت", Icons.Default.Bolt),
        NavItem(ScreenDestination.ACTIVE_USERS, "النشطين", Icons.Default.Wifi, activeUsers.size),
        NavItem(ScreenDestination.NETWORK_MAP, "الخريطة", Icons.Default.CellTower),
        NavItem(ScreenDestination.SETTINGS, "الإعدادات", Icons.Default.Settings)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "شعار التطبيق",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إدارة الشبكة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = activeRouter?.hotspotName ?: "MikroTik Hotspot",
                                    fontSize = 11.sp,
                                    color = BronzeLight
                                )
                            }
                        }
                    },
                    actions = {
                        // Router Status Pill in TopBar
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveConnected) StatusOnline else StatusWarning)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeRouter?.name?.take(10) ?: "الراوتر",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentScreen == item.destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    if (item.badgeCount != null && item.badgeCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(
                                                containerColor = CyanSecondary,
                                                contentColor = Color.Black
                                            ) {
                                                Text("${item.badgeCount}")
                                            }
                                        }) {
                                            Icon(item.icon, contentDescription = item.title)
                                        }
                                    } else {
                                        Icon(item.icon, contentDescription = item.title)
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = BronzePrimary,
                                    selectedTextColor = BronzePrimary,
                                    indicatorColor = BronzePrimary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Navigation Rail for Tablets / Wide screens
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = currentScreen == item.destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    if (item.badgeCount != null && item.badgeCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(containerColor = CyanSecondary, contentColor = Color.Black) {
                                                Text("${item.badgeCount}")
                                            }
                                        }) {
                                            Icon(item.icon, contentDescription = item.title)
                                        }
                                    } else {
                                        Icon(item.icon, contentDescription = item.title)
                                    }
                                },
                                label = { Text(item.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Main Screen Content with smooth fade transitions
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            ScreenDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            ScreenDestination.USERS -> UsersScreen(viewModel = viewModel)
                            ScreenDestination.VOUCHERS -> VoucherGeneratorScreen(viewModel = viewModel)
                            ScreenDestination.ACTIVE_USERS -> ActiveUsersScreen(viewModel = viewModel)
                            ScreenDestination.PROFILES -> ProfilesScreen(viewModel = viewModel)
                            ScreenDestination.NETWORK_MAP -> NetworkMapScreen(viewModel = viewModel)
                            ScreenDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
