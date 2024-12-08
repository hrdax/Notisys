package com.example.notisys

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notisys.ui.DashboardScreen
import com.example.notisys.ui.LoginScreen
import com.example.notisys.ui.MiniChatScreen
import com.example.notisys.ui.theme.NotisysTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificaciones
        checkNotificationPermission()

        // Crear canal de notificaciones
        createNotificationChannel()

        // Instancia de FirebaseAuth y Firestore
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Escuchar mensajes recientes (global y específicos)
        listenForMessages(firestore)

        setContent {
            NotisysTheme {
                // Estado para manejar la navegación entre pantallas
                var currentScreen by remember { mutableStateOf("loading") } // Pantalla inicial de carga
                var selectedRamo by remember { mutableStateOf("") }

                // Comprobar el estado del usuario autenticado al cargar
                LaunchedEffect(Unit) {
                    currentScreen = if (auth.currentUser != null) "dashboard" else "login"
                }

                // Lógica de navegación basada en el estado actual
                when (currentScreen) {
                    "loading" -> {
                        // Pantalla de carga inicial
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                    "login" -> LoginScreen(onLoginSuccess = {
                        currentScreen = "dashboard" // Navega al Dashboard tras iniciar sesión
                    })
                    "dashboard" -> DashboardScreen(
                        onRamoClick = { ramo ->
                            selectedRamo = ramo
                            currentScreen = "miniChat"
                        },
                        onLogout = {
                            auth.signOut()
                            currentScreen = "login"
                        },
                        sendNotification = { tipo, mensaje, ramo ->
                            sendNotification(tipo, mensaje, ramo)
                        }
                    )
                    "miniChat" -> MiniChatScreen(
                        ramo = selectedRamo,
                        onBack = { currentScreen = "dashboard" },
                        sendNotification = { tipo, mensaje, ramo ->
                            sendNotification(tipo, mensaje, ramo)
                        }
                    )
                }
            }
        }
    }

    private fun listenForMessages(firestore: FirebaseFirestore) {
        val oneHourAgo = Calendar.getInstance().apply {
            add(Calendar.HOUR, -1)
        }.time

        firestore.collection("mensajes")
            .whereGreaterThan("timestamp", oneHourAgo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error al escuchar mensajes: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documents?.forEach { document ->
                    val tipo = document.getString("tipo") ?: "Normal"
                    val mensaje = document.getString("mensaje") ?: ""
                    val ramo = document.getString("ramo") ?: "General"

                    if (tipo == "Información" || tipo == "Importante") {
                        println("Se detectó un mensaje del tipo $tipo: $mensaje")
                        sendNotification(tipo, mensaje, ramo)
                    }
                }
            }
    }

    private fun sendNotification(tipo: String, mensaje: String, ramo: String) {
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                println("Permiso de notificaciones no otorgado.")
                return
            }
        }

        val builder = NotificationCompat.Builder(this, "chat_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Notificación $tipo")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("Permiso de notificaciones otorgado.")
        } else {
            println("Permiso de notificaciones denegado.")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Chat"
            val descriptionText = "Canal para notificaciones de mensajes recientes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("chat_notifications", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
