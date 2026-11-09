package com.example

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BlockedListScreen
import com.example.ui.ContactsScreen
import com.example.ui.HomeScreen
import com.example.ui.IncomingCallSimulationDialog
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurplePrimary

class MainActivity : ComponentActivity() {

    private val viewModel: BlockedCallViewModel by viewModels()

    private var hasContactsPermission by mutableStateOf(false)
    private var isCallScreeningRoleHeld by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasContactsPermission = isGranted
        viewModel.refreshContacts()
        if (isGranted) {
            checkRoleAndRequest()
        }
    }

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateRoleStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updatePermissionStatus()
        updateRoleStatus()

        setContent {
            MyApplicationTheme {
                MainAppContainer(
                    viewModel = viewModel,
                    hasContactsPermission = hasContactsPermission,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    onRequestPermission = {
                        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    onRequestRole = {
                        checkRoleAndRequest()
                    }
                )
            }
        }

        if (!hasContactsPermission) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            checkRoleAndRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateRoleStatus()
        viewModel.refreshContacts()
    }

    private fun updatePermissionStatus() {
        hasContactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateRoleStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            isCallScreeningRoleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else {
            isCallScreeningRoleHeld = true
        }
    }

    private fun checkRoleAndRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                requestRoleLauncher.launch(intent)
            } else {
                isCallScreeningRoleHeld = true
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: BlockedCallViewModel,
    hasContactsPermission: Boolean,
    isCallScreeningRoleHeld: Boolean,
    onRequestPermission: () -> Unit,
    onRequestRole: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isProtectionActive by viewModel.isProtectionActive.collectAsStateWithLifecycle()
    val blockedCalls by viewModel.blockedCalls.collectAsStateWithLifecycle()
    val contactsCount by viewModel.contactsCount.collectAsStateWithLifecycle()
    val contactsList by viewModel.contactsList.collectAsStateWithLifecycle()
    val totalFilteredCalls by viewModel.totalFilteredCalls.collectAsStateWithLifecycle()
    val isCallSimulationVisible by viewModel.isCallSimulationVisible.collectAsStateWithLifecycle()
    val simulatedCallerNumber by viewModel.simulatedCallerNumber.collectAsStateWithLifecycle()

    if (isCallSimulationVisible) {
        IncomingCallSimulationDialog(
            callerNumber = simulatedCallerNumber,
            onBlockAndReject = { viewModel.blockAndRejectSimulatedCall() },
            onAnswer = { viewModel.dismissCallSimulation() },
            onDismiss = { viewModel.dismissCallSimulation() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth().testTag("bottom_nav_bar"),
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                // Tab 1: Início
                NavigationBarItem(
                    selected = selectedTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleContainer,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Tab 2: Contatos
                NavigationBarItem(
                    selected = selectedTab == AppTab.CONTACTS,
                    onClick = { viewModel.selectTab(AppTab.CONTACTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Contatos") },
                    label = { Text("Contatos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleContainer,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_contacts")
                )

                // Tab 3: Bloqueio
                NavigationBarItem(
                    selected = selectedTab == AppTab.BLOCKED,
                    onClick = { viewModel.selectTab(AppTab.BLOCKED) },
                    icon = { Icon(Icons.Default.Block, contentDescription = "Bloqueio") },
                    label = { Text("Bloqueio", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleContainer,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_blocked")
                )

                // Tab 4: Configurações
                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurações") },
                    label = { Text("Configurações", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleContainer,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    isProtectionActive = isProtectionActive,
                    contactsCount = contactsCount,
                    blockedCount = blockedCalls.size,
                    totalFilteredCalls = totalFilteredCalls,
                    hasContactsPermission = hasContactsPermission,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    onRequestPermission = onRequestPermission,
                    onRequestRole = onRequestRole,
                    onNavigateTab = { viewModel.selectTab(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppTab.CONTACTS -> {
                ContactsScreen(
                    contactsList = contactsList,
                    hasPermission = hasContactsPermission,
                    onRequestPermission = onRequestPermission,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppTab.BLOCKED -> {
                BlockedListScreen(
                    blockedCalls = blockedCalls,
                    onDeleteCall = { viewModel.deleteBlockedCall(it) },
                    onClearAll = { viewModel.clearAllBlockedCalls() },
                    onSimulateCall = { viewModel.showCallSimulation() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppTab.SETTINGS -> {
                SettingsScreen(
                    isProtectionActive = isProtectionActive,
                    onToggleProtection = { viewModel.setProtectionActive(it) },
                    hasContactsPermission = hasContactsPermission,
                    onRequestContactsPermission = onRequestPermission,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    onRequestCallScreeningRole = onRequestRole,
                    onClearAllData = { viewModel.clearAllBlockedCalls() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
