package com.nan.nearbystorefinder.presentation.profile.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nan.nearbystorefinder.R
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar
import com.nan.nearbystorefinder.presentation.home.components.NearoTopAppBar
import com.nan.nearbystorefinder.presentation.navigation.Screen
import com.nan.nearbystorefinder.presentation.profile.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel: AuthViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val authState = authViewModel.state
    val user = authState.user
    val profileImageUri by profileViewModel.profileImageUri.collectAsState()
    val fullName by profileViewModel.fullName.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        android.util.Log.d("ProfileScreen", "Image selected: $uri")
        if (uri != null) {
            profileViewModel.updateProfileImage(uri)
        }
    }

    LaunchedEffect(authState.user) {
        if (authState.user == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.map_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B0B0F).copy(alpha = 0.85f),
                            Color(0xFF0B0B0F).copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NearoTopAppBar(location = "Profile")
            },
            bottomBar = {
                NearoBottomBar(navController)
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Box(
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF8A7CFF), Color(0xFFFF8A7C), Color(0xFF8A7CFF))
                                    ),
                                    shape = CircleShape
                                )
                                .padding(6.dp)
                                .clickable {
                                    android.util.Log.d("ProfileScreen", "Profile image clicked")
                                    galleryLauncher.launch("image/*")
                                },
                            shape = CircleShape,
                            color = Color(0xFFFFCC80).copy(alpha = 0.2f)
                        ) {
                            if (profileImageUri != null || user?.photoUrl != null) {
                                AsyncImage(
                                    model = profileImageUri ?: user?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onError = { error ->
                                        android.util.Log.e("ProfileScreen", "Coil Error: ${error.result.throwable.message}")
                                    },
                                    onLoading = {
                                        android.util.Log.d("ProfileScreen", "Coil Loading: ${profileImageUri ?: user?.photoUrl}")
                                    },
                                    onSuccess = {
                                        android.util.Log.d("ProfileScreen", "Coil Success: ${profileImageUri ?: user?.photoUrl}")
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF5E4BFF),
                            modifier = Modifier.size(32.dp).offset(x = (-4).dp, y = (-4).dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, Modifier.size(16.dp), Color.White)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = fullName ?: user?.displayName ?: "Explorer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = user?.email ?: "No email available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(40.dp))
                }

                item {
                    ProfileSectionHeader("ACCOUNT ACTIVITY")
                    ProfileMenuItem("Orders", "View your active & past orders", Icons.Default.ShoppingBag, Color(0xFF8A7CFF).copy(0.2f), Color(0xFF8A7CFF))
                    ProfileMenuItem("Payments", "Manage cards and billing", Icons.Default.Payments, Color(0xFF4CAF50).copy(0.2f), Color(0xFF4CAF50))
                    ProfileMenuItem("Address", "Shipping and delivery locations", Icons.Default.LocationOn, Color(0xFFFF9800).copy(0.2f), Color(0xFFFF9800))
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    ProfileSectionHeader("GENERAL")
                    ProfileMenuItem("Help & Support", "FAQs and direct assistance", Icons.Default.Help, Color.Gray.copy(0.2f), Color.White)
                    ProfileMenuItem(
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        icon = Icons.Default.Logout,
                        iconBg = Color(0xFFFF5252).copy(0.2f),
                        iconTint = Color(0xFFFF5252),
                        onClick = {
                            authViewModel.logout()
                        }
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color(0xFF1A1A1E),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(24.dp), iconTint)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
