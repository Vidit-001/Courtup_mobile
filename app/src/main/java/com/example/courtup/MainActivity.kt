package com.example.courtup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Navy = Color(0xFF16213E)
private val CardBlue = Color(0xFF1A1A2E)
private val Pink = Color(0xFFE94560)
private val Muted = Color(0xFFAAAAAA)
private val Line = Color(0xFF2A2A4A)
private val Green = Color(0xFF4CAF50)
private val Red = Color(0xFFFF4444)
private val Gold = Color(0xFFF0A500)
private val sports = listOf("Cricket", "Football", "Basketball", "Table Tennis", "Pool", "Snooker")
private fun emoji(sport: String) = when(sport) {
    "Cricket" -> "🏏"; "Football" -> "⚽"; "Basketball" -> "🏀"; "Table Tennis" -> "🏓"; else -> "🎮"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Pink, background = Navy,
                surface = CardBlue, onPrimary = Color.White, onSurface = Color.White,
                onBackground = Color.White, outline = Line)) {
                CourtUp()
            }
        }
    }
}

@Composable
private fun CourtUp(vm: CourtUpViewModel = viewModel()) {
    BackHandler(enabled = vm.username.isNotEmpty() && vm.page != "Home") { vm.back() }
    Surface(color = Navy, modifier = Modifier.fillMaxSize()) {
        if (vm.username.isEmpty()) Welcome(vm::enter)
        else Scaffold(containerColor = Navy, topBar = {
            Column(Modifier.background(CardBlue).statusBarsPadding().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("🏟️ CourtUp", color = Pink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("👤 ${vm.username}", color = Pink, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }, bottomBar = {
            NavigationBar(containerColor = CardBlue) {
                listOf("Home", "Rooms", "Create Room").forEach { destination ->
                    val page = if (destination == "Create Room") "Create" else destination
                    NavigationBarItem(selected = vm.page == page, enabled = !vm.busy,
                        onClick = { vm.navigate(page) },
                        icon = { Text(when(page) { "Home" -> "🏟️"; "Rooms" -> "👥"; else -> "+" }) },
                        label = { Text(destination) },
                        colors = NavigationBarItemDefaults.colors(selectedTextColor = Pink, indicatorColor = Navy))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
                vm.error?.let { Text(it, color = Red, modifier = Modifier.padding(16.dp)) }
                vm.message?.let { message ->
                    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message, color = Green, modifier = Modifier.weight(1f))
                        TextButton(onClick = vm::dismissMessage) { Text("OK") }
                    }
                }
                if (vm.busy || vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Pink)
                when(vm.page) {
                    "Rooms" -> RoomsScreen(vm)
                    "Create" -> CreateScreen(vm)
                    else -> HomeScreen(vm::navigate)
                }
            }
        }
    }
}

@Composable
private fun Welcome(enter: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        contentAlignment = Alignment.Center) {
        Surface(color = CardBlue, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.widthIn(max = 400.dp).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("🏟️", fontSize = 48.sp)
                Text("Welcome to CourtUp", color = Pink, fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center)
                Text("Enter your name to continue", color = Muted)
                Field("Your name", name, { name = it }, imeAction = ImeAction.Done, onDone = { enter(name) })
                ActionButton("Enter", enabled = name.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth()) { enter(name) }
            }
        }
    }
}

@Composable
private fun HomeScreen(navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Find Players. Book Courts. Play.", fontSize = 38.sp, lineHeight = 44.sp, color = Pink,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("CourtUp connects sports enthusiasts in your area. Create a game room, invite players, and get on the court.",
            color = Muted, fontSize = 19.sp, textAlign = TextAlign.Center)
        ActionButton("Browse Rooms", modifier = Modifier.fillMaxWidth()) { navigate("Rooms") }
        OutlinedButton(onClick = { navigate("Create") }, border = BorderStroke(2.dp, Pink),
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Create Room", color = Color.White) }
        Spacer(Modifier.height(8.dp))
        sports.take(4).chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { sport ->
                    Surface(color = CardBlue, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                        Text("${emoji(sport)} $sport", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateScreen(vm: CourtUpViewModel) {
    var sport by rememberSaveable { mutableStateOf("Cricket") }
    var location by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var max by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Surface(color = CardBlue, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Create a Room", color = Pink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (vm.created) Text("✅ Room created! Redirecting...", color = Green)
                Text("Sport")
                Box {
                    OutlinedButton(enabled = !vm.busy, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(sport, color = Color.White) }
                    DropdownMenu(expanded, { expanded = false }) {
                        sports.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { sport = option; expanded = false }) }
                    }
                }
                Field("Your Name", vm.username, {}, readOnly = true)
                Field("Location", location, { location = it }, "e.g. Sector 62, Noida", enabled = !vm.busy)
                Field("Time", time, { time = it }, "e.g. 6:00 PM", enabled = !vm.busy)
                Field("Max Players", max, { max = it }, "e.g. 11", enabled = !vm.busy, keyboardType = KeyboardType.Number)
                ActionButton("Create Room", !vm.busy && !vm.loading && !vm.created, Modifier.fillMaxWidth()) {
                    vm.create(sport, location, time, max)
                }
            }
        }
    }
}

@Composable
private fun RoomsScreen(vm: CourtUpViewModel) {
    var removal by remember { mutableStateOf<Pair<Room, String>?>(null) }
    removal?.let { (room, member) ->
        AlertDialog(onDismissRequest = { removal = null }, title = { Text("Remove member") },
            text = { Text("Remove $member from the room?") },
            confirmButton = { TextButton(onClick = { removal = null; vm.action(room, "remove", member) }, enabled = !vm.busy) { Text("Remove", color = Red) } },
            dismissButton = { TextButton(onClick = { removal = null }) { Text("Cancel") } })
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Active Rooms", color = Pink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionButton("+ Create Room", !vm.busy) { vm.navigate("Create") }
            OutlinedButton(onClick = vm::refresh, enabled = !vm.busy && !vm.loading) { Text("Refresh") }
        }
        if (!vm.loading && vm.rooms.isEmpty()) Text(if (vm.error == null) "No active rooms. Create a room to get started." else "Unable to load rooms. Tap Refresh.", color = Muted)
        vm.rooms.forEach { room ->
            RoomCard(room, vm.username, !vm.busy && !vm.loading,
                action = { action, person -> vm.action(room, action, person) },
                remove = { removal = room to it })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomCard(room: Room, name: String, enabled: Boolean, action: (String, String) -> Unit, remove: (String) -> Unit) {
    val state = room.state(name)
    Surface(color = CardBlue, shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, if (state == "host") Pink else Line)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(emoji(room.sport), fontSize = 32.sp)
                if (state == "host" || state == "member") Surface(color = if (state == "host") Pink else Green, shape = MaterialTheme.shapes.extraLarge) {
                    Text(if(state == "host") "HOST" else "JOINED", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }
            Text(room.sport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("📍 ${room.location}", color = Muted)
            Text("🕐 ${room.time}", color = Muted)
            Text("👤 Host: ${room.host}", color = Muted)
            Text("${room.players}/${room.maxPlayers} players", color = if (room.players >= room.maxPlayers) Red else Pink)
            when(state) {
                "join" -> ActionButton("Request Join", enabled) { action("request", "") }
                "pending" -> Text("⏳ Awaiting approval", color = Gold)
                "full" -> Text("Room Full", color = Muted)
                else -> Text("✅ In Room", color = Green)
            }
            if(room.members.isNotEmpty()) {
                HorizontalDivider(color = Line)
                Text("MEMBERS", color = Muted, fontSize = 12.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    room.members.forEach { member ->
                        Surface(color = Navy, shape = MaterialTheme.shapes.extraLarge) {
                            Row(Modifier.padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(member, modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)
                                if(state == "host" && member != name) TextButton(onClick = { remove(member) }, enabled = enabled, contentPadding = PaddingValues(8.dp)) {
                                    Text("✕", color = Red)
                                }
                            }
                        }
                    }
                }
            }
            if (state == "host" && room.pending.isNotEmpty()) {
                HorizontalDivider(color = Line)
                Text("⏳ PENDING REQUESTS (${room.pending.size})", color = Gold, fontSize = 12.sp)
                room.pending.forEach { requester ->
                    Text(requester)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton("✓ Accept", enabled, color = Green) { action("accept", requester) }
                        ActionButton("✕ Reject", enabled, color = Red) { action("reject", requester) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier, color: Color = Pink, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier, shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)) { Text(text, modifier = Modifier.padding(vertical = 4.dp)) }
}

@Composable
private fun Field(label: String, value: String, change: (String) -> Unit, placeholder: String = "",
    readOnly: Boolean = false, enabled: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next, onDone: () -> Unit = {}) {
    OutlinedTextField(value = value, onValueChange = change, label = { Text(label) },
        placeholder = { Text(placeholder) }, readOnly = readOnly, enabled = enabled, singleLine = true,
        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }))
}
