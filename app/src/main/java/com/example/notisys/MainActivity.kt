package com.example.notisys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.notisys.ui.DashboardScreen
import com.example.notisys.ui.LoginScreen
import com.example.notisys.ui.MiniChatScreen
import com.example.notisys.ui.theme.NotisysTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia de FirebaseAuth
        val auth = FirebaseAuth.getInstance()

        setContent {
            NotisysTheme {
                // Estado para manejar la navegación entre pantallas
                var currentScreen by remember {
                    mutableStateOf(
                        if (auth.currentUser != null) "dashboard" else "login" // Comprueba si hay un usuario autenticado
                    )
                }
                var selectedRamo by remember { mutableStateOf("") }

                // Lógica de navegación basada en el estado actual
                when (currentScreen) {
                    "login" -> LoginScreen(onLoginSuccess = {
                        currentScreen = "dashboard" // Navega al dashboard tras iniciar sesión
                    })
                    "dashboard" -> DashboardScreen(
                        onRamoClick = { ramo ->
                            selectedRamo = ramo
                            currentScreen = "miniChat"
                        },
                        onLogout = {
                            auth.signOut() // Cierra sesión con Firebase
                            currentScreen = "login" // Redirige a la pantalla de login
                        }
                    )
                    "miniChat" -> MiniChatScreen(
                        ramo = selectedRamo,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
            }
        }
    }
}
