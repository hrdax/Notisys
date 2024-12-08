package com.example.notisys.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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

    // Mapeo de UID a autores
    val userMap = mapOf(
        "X9tGXRvx2nUVzOAtOE3bB6jgGIh1" to "Profe Lucho",
        "iZceWvVPMBSx0WUz0PVO3QPlThx1" to "Delegado Marcelo"
    )

    // Lista para almacenar mensajes desde Firestore
    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var message by remember { mutableStateOf("") }

    // Cargar mensajes de Firestore
    LaunchedEffect(Unit) {
        firestore.collection("mensajes")
            .whereEqualTo("ramo", ramo) // Filtrar por ramo
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

    // Manejar el botón de atrás del dispositivo
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensajes o texto de "No hay mensajes"
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay mensajes en el chat.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            }
        } else {
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

        // Mostrar campo de entrada solo si el UID no está restringido
        if (currentUid != "L242j5RV6NX3pQOhNipRifLwARK2") {
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
                        val author = userMap[currentUid] ?: "Desconocido"
                        val newMessage = hashMapOf(
                            "autor" to author,
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
        } else {
            Text(
                text = "No tienes permisos para enviar mensajes.",
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
