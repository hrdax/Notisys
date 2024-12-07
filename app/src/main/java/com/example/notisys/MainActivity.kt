package com.example.notisys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.notisys.ui.DashboardScreen
import com.example.notisys.ui.LoginScreen
import com.example.notisys.ui.MiniChatScreen
import com.example.notisys.ui.theme.NotisysTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotisysTheme {
                // Estado para manejar la navegación entre pantallas
                var currentScreen by remember { mutableStateOf("login") }
                var selectedRamo by remember { mutableStateOf("") }

                // Lógica de navegación basada en el estado actual
                when (currentScreen) {
                    "login" -> LoginScreen(onLoginSuccess = { currentScreen = "dashboard" })
                    "dashboard" -> DashboardScreen(
                        onRamoClick = { ramo ->
                            selectedRamo = ramo
                            currentScreen = "miniChat"
                        },
                        onLogout = { currentScreen = "login" } // Maneja el evento de cerrar sesión
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
