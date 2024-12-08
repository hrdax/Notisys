package com.example.notisys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import java.util.Calendar

@Composable
fun DashboardScreen(onRamoClick: (String) -> Unit, onLogout: () -> Unit, sendNotification: (String, String, String) -> Unit ) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    var ramos by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Escuchar mensajes recientes
    LaunchedEffect(Unit) {
        val oneHourAgo = Calendar.getInstance().apply {
            add(Calendar.HOUR, -1)
        }.time

        firestore.collection("mensajes")
            .whereGreaterThan("timestamp", oneHourAgo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error en Dashboard: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documents?.forEach { document ->
                    val tipo = document.getString("tipo") ?: "Normal"
                    val mensaje = document.getString("mensaje") ?: ""
                    val ramo = document.getString("ramo") ?: ""

                    if (tipo == "Información" || tipo == "Importante") {
                        println("Mensaje recibido en Dashboard: $mensaje")
                        sendNotification(tipo, mensaje, ramo) // Llama a la función de notificación
                    }
                }
            }
    }


    // Recuperar ramos desde Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            Log.d("FirestoreDebug", "userID: $userId")
            firestore.collection("ramos")
                .whereArrayContains("usuario", userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fetchedRamos = task.result?.documents?.map { it.getString("nombre").orEmpty() } ?: listOf()
                        ramos = fetchedRamos
                    } else {
                        println("Error al obtener ramos: ${task.exception?.message}")
                    }
                    isLoading = false
                }
        } else {
            println("No se encontró un usuario autenticado.")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Notysis",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFF2D2D2D)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Barra de opciones (Chats y Alertas)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "chats",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "alertas",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(8.dp)
            )
        }

        // Mostrar ramos o un mensaje de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        } else if (ramos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // Asegura que el mensaje ocupe el espacio disponible
                contentAlignment = Alignment.Center // Centra el contenido
            ) {
                Text(
                    text = "No tienes ramos asignados.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            }
        } else {
            Column(
                modifier = Modifier.weight(1f) // Asegura que los ramos ocupen el espacio disponible
            ) {
                for (ramo in ramos) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onRamoClick(ramo) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCE8FF))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 16.dp)
                            )
                            Text(
                                text = ramo,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Botón de "Cerrar Sesión"
        Button(
            onClick = { onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp) // Espaciado adicional
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                text = "Cerrar Sesión",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
        }
    }
}

