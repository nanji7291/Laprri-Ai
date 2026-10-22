package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AppNavigationTab
import com.example.ui.MainViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.StatusScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceHubScreen
import com.example.ui.screens.WebsiteBuilderScreen
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.AssistantAudioState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MyraApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyraApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val audioState by viewModel.voiceManager.audioState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        }
    }

    val requestMicPermission = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.toggleVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle Back Button navigation to return to VOICE_HUB when in other tabs
    if (currentTab != AppNavigationTab.VOICE_HUB) {
        BackHandler {
            viewModel.setTab(AppNavigationTab.VOICE_HUB)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CosmicDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.myra_logo),
                            contentDescription = "MYRA Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, NeonCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Laprri AI",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "UNLOCKED",
                                        color = NeonEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Zero Login • Permanent Free Access",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (audioState) {
                                    AssistantAudioState.IDLE -> NeonCyan
                                    AssistantAudioState.LISTENING -> NeonEmerald
                                    AssistantAudioState.THINKING -> ElectricViolet
                                    AssistantAudioState.SPEAKING -> NeonCyan
                                }
                            )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateDark,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 0.5.dp, color = SlateBorder)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavigationTab.VOICE_HUB,
                    onClick = { viewModel.setTab(AppNavigationTab.VOICE_HUB) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Voice Assistant") },
                    label = { Text("Assistant", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_assistant")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavigationTab.CHAT,
                    onClick = { viewModel.setTab(AppNavigationTab.CHAT) },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "AI Chat") },
                    label = { Text("Chat", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_chat")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavigationTab.WEBFORGE,
                    onClick = { viewModel.setTab(AppNavigationTab.WEBFORGE) },
                    icon = { Icon(Icons.Default.Language, contentDescription = "WebForge") },
                    label = { Text("WebForge", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_webforge")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavigationTab.TOOLS,
                    onClick = { viewModel.setTab(AppNavigationTab.TOOLS) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "System Tools") },
                    label = { Text("Tools", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_tools")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavigationTab.LICENSE_STATUS,
                    onClick = { viewModel.setTab(AppNavigationTab.LICENSE_STATUS) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Unlocked Status") },
                    label = { Text("Unlocked", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDark,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_status")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavigationTab.VOICE_HUB -> VoiceHubScreen(
                    viewModel = viewModel,
                    onRequestMicrophonePermission = { requestMicPermission() }
                )
                AppNavigationTab.CHAT -> ChatScreen(
                    viewModel = viewModel,
                    onRequestMicrophonePermission = { requestMicPermission() }
                )
                AppNavigationTab.WEBFORGE -> WebsiteBuilderScreen(viewModel = viewModel)
                AppNavigationTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                AppNavigationTab.LICENSE_STATUS -> StatusScreen(viewModel = viewModel)
            }
        }
    }
}
