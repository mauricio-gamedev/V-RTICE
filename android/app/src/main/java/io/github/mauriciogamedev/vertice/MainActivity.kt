package io.github.mauriciogamedev.vertice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vertice.backend.identity.UserProfile
import com.vertice.backend.identity.AuthResponse
import com.vertice.android.unused
import com.vertice
import kotlinx.coroutines.launch
import com.vertice
import androidx.compose.material3.TextButton
import com.vertice
import com.vertice
import io.github.mauriciogamedev.vertice.auth.AuthLoadingScreen
import io.github.mauriciogamedev.vertice.auth.AuthScreen
import io.github.mauriciogamedev.vertice.auth.AuthSession
import io.github.mauriciogamedev.vertice.auth.VerticeAuthRepository

private val VerticeBackground = Color(0xFF090C12)
private val VerticeSurface = Color(0xFF121824)
private val VerticeSurfaceRaised = Color(0xFF192231)
private val VerticeAccent = Color(0xFF9B8CFF)
private val VerticeAccentSoft = Color(0xFF292443)
private val VerticeText = Color(0xFFF2F3F8)
private val VerticeMuted = Color(0xFF9AA5B5)
private val VerticeOnline = Color(0xFF64D6A0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VerticeApp() }
    }
}

private enum class Destination(
    val label: String,
    val symbol: String,
) {
    HOME("Início", "⌂"),
    MESSAGES("Mensagens", "✉"),
    CIRCLES("Círculos", "◈"),
    PROFILE("Perfil", "◉"),
}

@Composable
private fun VerticeApp() {
    val context = LocalContext.current
    val authRepository = remember { VerticeAuthRepository(context) }
    var session by remember { mutableStateOf<AuthSession?>(null) }
    var sessionChecked by remember { mutableStateOf(false) }

    LaunchedEffect(authRepository) {
        session = authRepository.restoreSession()
        sessionChecked = true
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = VerticeAccent,
            onPrimary = Color(0xFF17122D),
            secondary = Color(0xFF83DCE8),
            background = VerticeBackground,
            surface = VerticeSurface,
            surfaceVariant = VerticeSurfaceRaised,
            onSurface = VerticeText,
            onSurfaceVariant = VerticeMuted,
        ),
    ) {
        when {
            !sessionChecked -> AuthLoadingScreen()
            session == null -> AuthScreen(
                repository = authRepository,
                onAuthenticated = { session = it },
            )
            else -> SocialApp(
                session = session!!,
                authRepository = authRepository,
                onLoggedOut = { session = null },
            )
        }
    }
}

@Composable
private fun SocialApp(
    session: AuthSession,
    authRepository: VerticeAuthRepository,
    onLoggedOut: () -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var logoutError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = VerticeBackground,
        bottomBar = {
            NavigationBar(
                containerColor = VerticeBackground,
                contentColor = VerticeMuted,
                tonalElevation = 0.dp,
            ) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = {
                            Text(
                                text = item.symbol,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                maxLines = 1,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerticeAccent,
                            selectedTextColor = VerticeText,
                            unselectedIconColor = VerticeMuted,
                            unselectedTextColor = VerticeMuted,
                            indicatorColor = VerticeAccentSoft,
                        ),
                    )
                }
            }
        },
    ) { contentPadding ->
        when (destination) {
            Destination.HOME -> HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                displayName = session.user.displayName,
                onOpenCircles = { destination = Destination.CIRCLES },
            )

            Destination.MESSAGES -> EmptySectionScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                eyebrow = "CONVERSAS",
                title = "Suas conversas",
                description = "Suas conversas individuais e em grupo aparecerão aqui.",
                symbol = "✉",
            )

            Destination.CIRCLES -> EmptySectionScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                eyebrow = "COMUNIDADES",
                title = "Seus Círculos",
                description = "Crie um espaço para reunir seus amigos em canais, salas e atividades.",
                symbol = "◈",
            )

            Destination.PROFILE -> ProfileScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                user = session.user,
                isLoggingOut = isLoggingOut,
                logoutError = logoutError,
                onLogout = {
                    scope.launch {
                        isLoggingOut = true
                        logoutError = null
                        try {
                            authRepository.logout(session)
                            onLoggedOut()
                        } catch (failure: Exception) {
                            logoutError = failure.message ?: "Não foi possível encerrar a sessão no servidor."
                        } finally {
                            isLoggingOut = false
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    displayName: String,
    onOpenCircles: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(VerticeBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(VerticeAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "V",
                        color = Color(0xFF17122D),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "VÉRTICE",
                        color = VerticeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "SEU ESPAÇO SOCIAL",
                        color = VerticeMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = VerticeSurfaceRaised,
            ) {
                Text(
                    text = displayName.take(2).uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = VerticeText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                text = "BEM-VINDO,\n${displayName.uppercase()}.",
                color = VerticeText,
                fontSize = 29.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
            )
            Text(
                text = "Encontre seus amigos, converse e entre em uma sala sem complicação.",
                color = VerticeMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = VerticeSurface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(VerticeOnline),
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Sua presença",
                        color = VerticeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "O sistema Pulse será ativado na próxima etapa.",
                        color = VerticeMuted,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("ACONTECENDO AGORA", "—")
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = VerticeSurface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text(
                        text = "Nenhuma sala ativa",
                        color = VerticeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Quando seus amigos abrirem uma sala, você poderá encontrar e entrar nela por aqui.",
                        color = VerticeMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("SEUS CÍRCULOS", "—")
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = VerticeSurface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Sua comunidade ainda está vazia.",
                        color = VerticeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Crie um espaço para reunir sua turma, organizar conversas e abrir salas.",
                        color = VerticeMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                    Text(
                        text = "CONHECER CÍRCULOS  →",
                        color = VerticeAccent,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenCircles)
                            .background(VerticeAccentSoft)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    count: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = VerticeMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        Text(
            text = count,
            color = VerticeMuted,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun EmptySectionScreen(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
    description: String,
    symbol: String,
) {
    Box(
        modifier = modifier
            .background(VerticeBackground)
            .padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(VerticeAccentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = symbol,
                    color = VerticeAccent,
                    fontSize = 29.sp,
                )
            }
            Text(
                text = eyebrow,
                color = VerticeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            )
            Text(
                text = title,
                color = VerticeText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                color = VerticeMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    modifier: Modifier = Modifier,
    user: io.github.mauriciogamedev.vertice.auth.AuthUserDto,
    isLoggingOut: Boolean,
    logoutError: String?,
    onLogout: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(VerticeBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "SEU PERFIL",
            color = VerticeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
        )
        Text(
            text = user.displayName,
            color = VerticeText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "@${user.username}",
            color = VerticeMuted,
            fontSize = 14.sp,
        )
        Surface(
            color = VerticeSurface,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileDetail("E-MAIL", user.email)
                ProfileDetail("ID", user.id)
                ProfileDetail("CONTA CRIADA", user.createdAt)
            }
        }
        if (logoutError != null) {
            Text(
                text = logoutError,
                color = Color(0xFFFF8585),
                fontSize = 12.sp,
            )
        }
        Button(
            onClick = onLogout,
            enabled = !isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(if (isLoggingOut) "Encerrando..." else "Sair da conta")
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = VerticeMuted,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = VerticeText,
            fontSize = 13.sp,
        )
    }
}
