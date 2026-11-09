package com.example.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.BlockedCallViewModel
import com.example.R
import com.example.ui.theme.MintGreenBorder
import com.example.ui.theme.MintGreenCheck
import com.example.ui.theme.MintGreenContainer
import com.example.ui.theme.MintGreenText
import com.example.ui.theme.PurplePrimary

@Composable
fun HomeScreen(
    viewModel: BlockedCallViewModel,
    isProtectionActive: Boolean,
    contactsCount: Int,
    blockedCount: Int,
    totalFilteredCalls: Int,
    hasContactsPermission: Boolean,
    isCallScreeningRoleHeld: Boolean,
    onRequestPermission: () -> Unit,
    onRequestRole: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .testTag("home_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Header Bar matching the mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("app_logo_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spamblocker_logo),
                        contentDescription = "SpamBlocker Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // App Title & Logomark matching the image
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Spam",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.SpamNavy,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Blocker",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.BlockerBlue,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("home_more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = Color(0xFF334155)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Simular chamada spam") },
                        onClick = {
                            showMenu = false
                            viewModel.showCallSimulation()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Configurações") },
                        onClick = {
                            showMenu = false
                            onNavigateTab(AppTab.SETTINGS)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Proteção Ativa (Mint Green Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isProtectionActive) MintGreenBorder else Color(0xFFFECACA),
                    shape = RoundedCornerShape(18.dp)
                )
                .testTag("protection_status_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isProtectionActive) MintGreenContainer else Color(0xFFFEF2F2)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shield / Status icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isProtectionActive) MintGreenCheck.copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isProtectionActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isProtectionActive) MintGreenCheck else Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isProtectionActive) "Proteção ativa" else "Proteção desativada",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProtectionActive) MintGreenText else Color(0xFF991B1B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chamadas de spam e números fora da lista de contatos são bloqueadas automaticamente.",
                        fontSize = 12.sp,
                        color = if (isProtectionActive) Color(0xFF166534) else Color(0xFFB91C1C),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Toggle Switch
                Switch(
                    checked = isProtectionActive,
                    onCheckedChange = { viewModel.setProtectionActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MintGreenCheck,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("protection_active_switch")
                )
            }
        }

        // Warning banner if permissions/roles are missing
        if (!hasContactsPermission || !isCallScreeningRoleHeld) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!hasContactsPermission) "Permissão de contatos pendente" else "Definir como app de triagem padrão",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Necessário para identificar e bloquear números fora da agenda.",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                    Text(
                        text = "Ativar",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .clickable {
                                if (!hasContactsPermission) onRequestPermission() else onRequestRole()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Card 2: Interactive grouped card with 3 rows
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("metrics_card_container"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row 1: Contatos permitidos (Lista segura)
                // Row 1: Contatos permitidos (Lista segura)
                MetricRowItem(
                    icon = Icons.Default.People,
                    iconTint = Color(0xFF2563EB),
                    title = "Contatos permitidos",
                    subtitle = "(Lista segura)",
                    countText = "$contactsCount",
                    onClick = { onNavigateTab(AppTab.CONTACTS) },
                    testTag = "row_allowed_contacts"
                )

                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Row 2: Números bloqueados
                MetricRowItem(
                    icon = Icons.Default.Block,
                    iconTint = Color(0xFFDC2626),
                    title = "Números bloqueados",
                    subtitle = null,
                    countText = "$blockedCount",
                    onClick = { onNavigateTab(AppTab.BLOCKED) },
                    testTag = "row_blocked_numbers"
                )

                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Row 3: Chamadas filtradas (últimos 7 dias)
                MetricRowItem(
                    painter = painterResource(id = R.drawable.ic_filtered_calls_bars),
                    iconTint = Color(0xFF25284B),
                    title = "Chamadas filtradas",
                    subtitle = "(últimos 7 dias)",
                    countText = "$totalFilteredCalls",
                    onClick = { onNavigateTab(AppTab.BLOCKED) },
                    testTag = "row_filtered_calls"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Call Simulation Trigger Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showCallSimulation() }
                .testTag("simulate_call_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDFF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simular chamada spam",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B0764)
                    )
                    Text(
                        text = "Teste como o SpamBlocker intercepta e bloqueia a chamada em tempo real.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B21A8),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurplePrimary
                ) {
                    Text(
                        text = "Testar",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun MetricRowItem(
    icon: ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    iconTint: Color,
    title: String,
    subtitle: String?,
    countText: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = countText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
