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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a6c8c.ui.theme._6C8CTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _6C8CTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CallBlockerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CallBlockerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // RoleManager is available from API 29 (Android 10)
    val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    } else {
        null
    }
    
    var newNumber by remember { mutableStateOf("") }
    val blockedNumbers = BlockedNumbersRepository.blockedNumbers

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                Toast.makeText(context, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Bloqueador de Llamadas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            roleLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, "Ya tienes el permiso activo", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "El rol de filtrado no está disponible", Toast.LENGTH_SHORT).show()
                    }
                } else {
                     Toast.makeText(context, "Se requiere Android 10+ para esta función", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Habilitar Bloqueo (Permiso)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Configuración de Bloqueo", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bloquear +56 600 *")
            Switch(
                checked = BlockedNumbersRepository.block600,
                onCheckedChange = { BlockedNumbersRepository.block600 = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bloquear +56 800 *")
            Switch(
                checked = BlockedNumbersRepository.block800,
                onCheckedChange = { BlockedNumbersRepository.block800 = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Números Bloqueados", style = MaterialTheme.typography.titleLarge)
        
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

        LazyColumn {
            items(blockedNumbers) { number ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = number, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { BlockedNumbersRepository.removeNumber(number) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }
}