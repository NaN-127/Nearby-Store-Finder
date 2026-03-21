package com.nan.nearbystorefinder.presentation.auth.screen

import android.R.attr.password
import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.nan.nearbystorefinder.R
import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.presentation.auth.components.CustomInputField
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.nan.nearbystorefinder.ui.theme.NearoTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SignUpScreen(
    onLoginClick: () -> Unit,
    onSignUpSuccess: () -> Unit
) {


    val contextForToast = LocalContext.current
    val viewModel: AuthViewModel = koinViewModel()
    val state = viewModel.state

    val googleAuthClient: GoogleAuthClient = koinInject()

    LaunchedEffect(state.user, state.isAuthReady, state.error) {
        if (state.isAuthReady && state.user != null) {
            onSignUpSuccess()
        }
        if (state.error != null) {
            android.widget.Toast.makeText(contextForToast, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        result ->

        if(result.resultCode == RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try{
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if(idToken != null){
                    viewModel.signInWithGoogle(idToken = idToken)
                } else {
                    android.widget.Toast.makeText(contextForToast, "Google Sign-In: ID Token is null", android.widget.Toast.LENGTH_LONG).show()
                }
            }catch (e: ApiException){
                e.printStackTrace()
                android.widget.Toast.makeText(contextForToast, "Google Sign-In failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }

        }
    }


    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current



    NearoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B0B0F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))


                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_send),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Nearo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Join the community today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                CustomInputField(
                    value = state.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    focusManager = focusManager,
                    errorMessage = state.fullNameError
                )

                CustomInputField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email",
                    placeholder = "Enter your email address",
                    keyboardType = KeyboardType.Email,
                    focusManager = focusManager,
                    errorMessage = state.emailError
                )

                Text(
                    text = "Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = { Text("Create a password", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    isError = state.passwordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A1E),
                        unfocusedContainerColor = Color(0xFF1A1A1E),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                if (state.passwordError != null) {
                    Text(
                        text = state.passwordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.signUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if(state.isLoading){
                        CircularProgressIndicator(color = Color.White)
                    }else{
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.Gray)
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        launcher.launch(googleAuthClient.getSignInIntent())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333336)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    enabled = !state.isLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Already have an account? ", color = Color.Gray)
                    TextButton(
                        onClick = {
                            onLoginClick()

                    }, contentPadding = PaddingValues(0.dp)) {
                        Text("Log in", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
