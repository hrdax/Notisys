package com.example.notisys.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniChatScreen(ramo: String, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid

    // Autor basado en UID
    val authorMap = mapOf(
        "X9tGXRvx2nUVzOAtOE3bB6jgGIh1" to "Profe Lucho",
        "iZceWvVPMBSx0WUz0PVO3QPlThx1" to "Delegado Marcelo"
    )
    val canCreateNotification = currentUid == "X9tGXRvx2nUVzOAtOE3bB6jgGIh1"
    val canSendMessages = currentUid != "L242j5RV6NX3pQOhNipRifLwARK2"

    var messages by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var message by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }
    var notificationType by remember { mutableStateOf("Información") }
    var notificationMessage by remember { mutableStateOf("") }

    // Cargar mensajes de Firestore
    LaunchedEffect(Unit) {
        firestore.collection("mensajes")
            .whereEqualTo("ramo", ramo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error al cargar mensajes: ${error.message}")
                    return@addSnapshotListener
                }
                val newMessages = snapshot?.documents?.map { doc ->
                    mapOf(
                        "autor" to (doc.getString("autor") ?: "Desconocido"),
                        "mensaje" to (doc.getString("mensaje") ?: ""),
                        "tipo" to (doc.getString("tipo") ?: "Normal") // Default tipo "Normal"
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
        // Barra superior
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
            if (canCreateNotification) {
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensajes
        Column(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Text(
                    text = "No hay mensajes en el chat.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                for (messageData in messages) {
                    val autor = messageData["autor"] ?: "Desconocido"
                    val mensaje = messageData["mensaje"] ?: ""
                    val tipo = messageData["tipo"] ?: "Normal"

                    // Determinar color de fondo según autor y tipo
                    val backgroundColor = when {
                        autor == "Profe Lucho" && tipo == "Información" -> Color(0xFFDCE8FF) // Celeste
                        autor == "Profe Lucho" && tipo == "Importante" -> Color(0xFFFFD6D6) // Rojo claro
                        autor == "Profe Lucho" -> Color(0xFFB9FBC0) // Verde claro (Normal)
                        autor == "Delegado Marcelo" -> Color(0xFFFFC4C4) // Rojo pastel suave
                        else -> Color(0xFFF1F1F1) // Color default para otros
                    }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = autor,
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
                                    backgroundColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (tipo != "Normal") {
                                    Icon(
                                        imageVector = if (tipo == "Información") Icons.Default.Info else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (tipo == "Información") Color.Blue else Color.Red
                                    )
                                }
                                Text(
                                    text = mensaje,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Enviar mensajes
        if (canSendMessages) {
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
                        val autor = authorMap[currentUid] ?: "Desconocido"
                        val newMessage = hashMapOf(
                            "autor" to autor,
                            "mensaje" to message,
                            "tipo" to "Normal", // Mensaje normal por defecto
                            "ramo" to ramo,
                            "timestamp" to FieldValue.serverTimestamp()
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
        } else {
            Text(
                text = "No tienes permisos para enviar mensajes.",
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Modal para crear notificación
    if (showModal) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            title = { Text("Crear notificación", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RadioButton(
                            selected = notificationType == "Información",
                            onClick = { notificationType = "Información" }
                        )
                        Text("Información")
                        RadioButton(
                            selected = notificationType == "Importante",
                            onClick = { notificationType = "Importante" }
                        )
                        Text("Importante")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = notificationMessage,
                        onValueChange = { notificationMessage = it },
                        placeholder = { Text("Escribir mensaje...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notification = hashMapOf(
                            "autor" to "Profe Lucho",
                            "mensaje" to notificationMessage,
                            "tipo" to notificationType,
                            "ramo" to ramo,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                        firestore.collection("mensajes").add(notification)
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

