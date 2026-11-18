package io.github.mauriciogamedev.vertice.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AuthBackground = Color(0xFF090C12)
private val AuthSurface = Color(0xFF121824)
private val AuthAccent = Color(0xFF9B8CFF)
private val AuthMuted = Color(0xFF9AA5B5)
private val AuthSuccess = Color(0xFF64D6A0)

@Composable
fun AuthScreen(
    repository: VerticeAuthRepository,
    onAuthenticated: (AuthSession) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var serverUrl by rememberSaveable {
        mutableStateOf(repository.currentServerUrl().orEmpty())
    }
    var serverReady by rememberSaveable { mutableStateOf(false) }
    var serverMessage by rememberSaveable { mutableStateOf("Configure o servidor HTTPS do VÉRTICE.") }
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isBusy by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(serverUrl) {
        serverReady = false
        if (serverUrl.isNotBlank() && repository.currentServerUrl() == serverUrl.trim().trimEnd('/')) {
            runCatching { repository.checkServerConnection() }
                .onSuccess {
                    serverReady = true
                    serverMessage = "Servidor conectado."
                }
                .onFailure {
                    serverMessage = it.message ?: "Não foi possível conectar ao servidor."
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.width(44.dp).height(44.dp),
                shape = RoundedCornerShape(15.dp),
                color = AuthAccent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "V",
                        color = Color(0xFF17122D),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "VÉRTICE",
                    color = Color(0xFFF2F3F8),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "SEU ESPAÇO SOCIAL",
                    color = AuthMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isRegister) "Sua conta começa aqui." else "Sua turma está mais perto.",
            color = Color(0xFFF2F3F8),
            fontSize = 27.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Entre para conversar, encontrar seus amigos e participar dos seus Círculos.",
            color = AuthMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        )

        Surface(
            color = AuthSurface,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "ENDEREÇO DO SERVIDOR",
                    color = AuthMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = {
                        serverUrl = it
                        serverReady = false
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("https://api.seudominio.com") },
                    supportingText = {
                        Text("Use o endereço HTTPS da API. Ainda não existe servidor público configurado.")
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = serverMessage,
                        color = if (serverReady) AuthSuccess else AuthMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        enabled = !isBusy && serverUrl.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isBusy = true
                                errorMessage = null
                                try {
                                    repository.configureServer(serverUrl)
                                    repository.checkServerConnection()
                                    serverReady = true
                                    serverMessage = "Servidor conectado."
                                } catch (failure: Exception) {
                                    serverReady = false
                                    serverMessage = failure.message ?: "Falha ao conectar."
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                    ) {
                        Text("Verificar")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = {
                    isRegister = false
                    errorMessage = null
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Entrar",
                    color = if (!isRegister) AuthAccent else AuthMuted,
                    fontWeight = if (!isRegister) FontWeight.Bold else FontWeight.Normal,
                )
            }
            TextButton(
                onClick = {
                    isRegister = true
                    errorMessage = null
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Criar conta",
                    color = if (isRegister) AuthAccent else AuthMuted,
                    fontWeight = if (isRegister) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        if (isRegister) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nome de exibição") },
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '_' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nome de usuário") },
                supportingText = { Text("3–24 caracteres: letras, números e _.") },
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("E-mail") },
            )
        } else {
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("E-mail ou nome de usuário") },
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = Color(0xFFFF8585),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isBusy = true
                    errorMessage = null
                    try {
                        if (!serverReady) {
                            repository.configureServer(serverUrl)
                            repository.checkServerConnection()
                            serverReady = true
                            serverMessage = "Servidor conectado."
                        }
                        val session = if (isRegister) {
                            repository.register(email, username, displayName, password)
                        } else {
                            repository.login(identifier, password)
                        }
                        onAuthenticated(session)
                    } catch (failure: Exception) {
                        errorMessage = failure.message ?: "Não foi possível concluir a solicitação."
                    } finally {
                        isBusy = false
                    }
                }
            },
            enabled = !isBusy && serverUrl.isNotBlank() &&
                password.isNotBlank() &&
                if (isRegister) {
                    email.isNotBlank() && username.isNotBlank() && displayName.isNotBlank()
                } else {
                    identifier.isNotBlank()
                },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp),
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = Color(0xFF17122D),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = if (isRegister) "Criar minha conta" else "Entrar no VÉRTICE",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Seu refresh token fica criptografado pelo Android Keystore. O access token não é salvo em disco.",
            color = AuthMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
fun AuthLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(AuthBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AuthAccent)
    }
}
