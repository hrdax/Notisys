package com.example.notisys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit) {
    // Estados para almacenar correo y contraseña
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") } // Estado para mostrar errores

    // Diseño de la pantalla de login
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // Fondo claro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título/Logo
            Text(
                text = "Notysis",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF2D2D2D)
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Campo de texto para correo
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("correo") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Correo")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Campo de texto para contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("clave") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Clave")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Mensaje de error (si las credenciales no son válidas)
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Botón para iniciar sesión
            Button(
                onClick = {
                    if (email == "admin" && password == "admin") {
                        onLoginSuccess() // Redirige si las credenciales son correctas
                    } else {
                        errorMessage = "Correo o clave incorrectos"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "Iniciar",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }
        }
    }
}
