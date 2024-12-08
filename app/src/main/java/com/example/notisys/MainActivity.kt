package com.example.notisys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
