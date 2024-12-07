package com.example.notisys.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniChatScreen(ramo: String, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val messages = listOf(
        Pair("Profesor lucho", "No hay sistema"),
        Pair("Delegado Marcelo", "https://youtu.be/o9OHcPv-26k?si=9Dlcy2POHlzaqju")
    )

    // Manejar el botón de atrás del dispositivo
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        // Barra superior con el título del ramo y opciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ramo,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Box {
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Crear notificación") },
                        onClick = {
                            isMenuExpanded = false
                            // Acción del botón
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensajes del chat
        Column(modifier = Modifier.weight(1f)) {
            for ((sender, content) in messages) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = sender,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .background(
                                if (sender == "Profesor lucho") Color(0xFFDCE8FF) else Color(0xFFE8D4FF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para escribir mensajes y botón de envío
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Escribir mensaje...") },
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent, // Fondo del TextField transparente
                    focusedIndicatorColor = Color.Transparent, // Sin línea al enfocar
                    unfocusedIndicatorColor = Color.Transparent // Sin línea al desenfocar
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    // Acción para enviar mensaje
                    message = ""
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFB2F5E9), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar mensaje",
                    tint = Color(0xFF00796B)
                )
            }
        }
    }
}
