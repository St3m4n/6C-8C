package com.example.a6c8c

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a6c8c.ui.theme._6C8CTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _6C8CTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    // RoleManager logic
    val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    } else {
        null
    }
    
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                BlockedNumbersRepository.block600 = true
                BlockedNumbersRepository.block809 = true
                Toast.makeText(context, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                BlockedNumbersRepository.block600 = false
                BlockedNumbersRepository.block809 = false
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission logic for contacts
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            BlockedNumbersRepository.blockUnknown = true
        } else {
            BlockedNumbersRepository.blockUnknown = false
            Toast.makeText(context, "Se requiere permiso de contactos", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-request permission logic
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                BlockedNumbersRepository.block600 = true
                BlockedNumbersRepository.block809 = true
            } else {
                if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    roleLauncher.launch(intent)
                }
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Block, contentDescription = "600") },
                    label = { Text("600") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Block, contentDescription = "809") },
                    label = { Text("809") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Otros") },
                    label = { Text("Otros") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text(
                text = "Bloqueador de Llamadas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Configuración de Bloqueo", style = MaterialTheme.typography.titleLarge)

            // 600 Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bloquear +56 600 *")
                Switch(
                    checked = BlockedNumbersRepository.block600,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                roleLauncher.launch(intent)
                            } else {
                                BlockedNumbersRepository.block600 = true
                            }
                        } else {
                            BlockedNumbersRepository.block600 = false
                        }
                    }
                )
            }

            // 809 Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bloquear +56 809 *")
                Switch(
                    checked = BlockedNumbersRepository.block809,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                roleLauncher.launch(intent)
                            } else {
                                BlockedNumbersRepository.block809 = true
                            }
                        } else {
                            BlockedNumbersRepository.block809 = false
                        }
                    }
                )
            }

            // Unknown Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bloquear desconocidos")
                Switch(
                    checked = BlockedNumbersRepository.blockUnknown,
                    onCheckedChange = { 
                        if (it) {
                            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        } else {
                            BlockedNumbersRepository.blockUnknown = false
                        }
                    }
                )
            }

            // Manual Block List Input
            var newNumber by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newNumber,
                    onValueChange = { newNumber = it },
                    label = { Text("Número a bloquear") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newNumber.isNotBlank()) {
                        BlockedNumbersRepository.addNumber(newNumber)
                        newNumber = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Tab Content (History)
            Text(
                text = when(selectedTab) {
                    0 -> "Historial 600"
                    1 -> "Historial 809"
                    else -> "Historial Otros"
                }, 
                style = MaterialTheme.typography.titleMedium
            )
            
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> BlockedCallList(BlockType.TYPE_600)
                    1 -> BlockedCallList(BlockType.TYPE_809)
                    2 -> BlockedCallList(BlockType.TYPE_OTHER)
                }
            }
        }
    }
}

@Composable
fun BlockedCallList(type: BlockType) {
    val calls = BlockedCallHistoryRepository.history.filter { it.type == type }
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    LazyColumn {
        items(calls) { call ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = call.number, style = MaterialTheme.typography.bodyLarge)
                    Text(text = dateFormat.format(Date(call.timestamp)), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}