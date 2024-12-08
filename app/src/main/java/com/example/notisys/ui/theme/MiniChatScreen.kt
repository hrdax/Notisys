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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniChatScreen(ramo: String, onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var message by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }
    var notificationType by remember { mutableStateOf("Información") }
    var notificationMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        firestore.collection("mensajes")
            .whereEqualTo("ramo", ramo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error al cargar mensajes: ${error.message}")
                    return@addSnapshotListener
                }
                val newMessages = snapshot?.documents?.map { doc ->
                    Pair(
                        doc.getString("autor") ?: "Desconocido",
                        doc.getString("contenido") ?: ""
                    )
                } ?: listOf()
                messages = newMessages
            }
    }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
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
                IconButton(onClick = { isMenuExpanded = true }) {
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
                            showModal = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Text(
                    text = "No hay mensajes en el chat.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
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
                                    if (sender == "Profe Lucho") Color(0xFFDCE8FF) else Color(0xFFE8D4FF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val newMessage = hashMapOf(
                        "autor" to "Profe Lucho",
                        "contenido" to message,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "ramo" to ramo
                    )
                    firestore.collection("mensajes").add(newMessage)
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

    if (showModal) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            title = { Text("Crear notificación", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado entre elementos
                ) {
                    Text("Tipo de notificación:", fontWeight = FontWeight.Bold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = notificationType == "Información",
                            onClick = { notificationType = "Información" }
                        )
                        Text(
                            text = "Información",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = notificationType == "Importante",
                            onClick = { notificationType = "Importante" }
                        )
                        Text(
                            text = "Importante",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = notificationMessage,
                        onValueChange = { notificationMessage = it },
                        placeholder = { Text("Escribir mensaje...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },


            confirmButton = {
                Button(
                    onClick = {
                        val notification = hashMapOf(
                            "tipo" to notificationType,
                            "mensaje" to notificationMessage,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                        firestore.collection("notificaciones").add(notification)
                        showModal = false
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
