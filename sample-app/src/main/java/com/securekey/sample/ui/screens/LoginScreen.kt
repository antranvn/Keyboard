package com.securekey.sample.ui.screens

import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.securekey.sample.credentials.createCredential
import com.securekey.sample.credentials.getCredential
import com.securekey.sample.credentials.prepareGetCredential
import com.securekey.sample.ui.findActivity
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.ui.SecureEditText

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var usernameView by remember { mutableStateOf<SecureEditText?>(null) }
    var passwordView by remember { mutableStateOf<SecureEditText?>(null) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val hasSavedPassword by viewModel.hasSavedPassword.collectAsStateWithLifecycle()

    LaunchedEffect(activity) {
        val a = activity ?: run {
            Log.w(TAG, "activity not resolved; skipping prepareGetCredential")
            return@LaunchedEffect
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Log.d(TAG, "prepareGetCredential: probing for saved password")
            viewModel.checkSavedPassword(prepare = { req -> prepareGetCredential(a, req) })
        } else {
            Log.d(TAG, "API < 34: skipping probe, assuming saved password available")
            viewModel.assumeSavedPasswordAvailable()
        }
    }

    LaunchedEffect(hasSavedPassword) {
        Log.d(TAG, "hasSavedPassword = $hasSavedPassword")
    }

    LaunchedEffect(isLoading) {
        Log.d(TAG, "isLoading = $isLoading")
    }

    LaunchedEffect(statusMessage, errorMessage) {
        statusMessage?.let { Log.d(TAG, "statusMessage: $it") }
        errorMessage?.let { Log.w(TAG, "errorMessage: $it") }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            Log.d(TAG, "nav event: $event")
            when (event) {
                is LoginNavEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SecureKey Bank",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Secure Login",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                SecureEditText(ctx).apply {
                    hint = "Username"
                    inputType = InputType.TYPE_CLASS_TEXT
                    setPadding(48, 40, 48, 40)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    SecureKey.getInstance().attachTo(this, KeyboardMode.QWERTY_FULL)
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val text = s?.toString().orEmpty()
                            if (text != username) username = text
                        }
                    })
                    usernameView = this
                }
            },
            update = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                SecureEditText(ctx).apply {
                    hint = "Password"
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    setPadding(48, 40, 48, 40)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    SecureKey.getInstance().attachTo(this, KeyboardMode.QWERTY_FULL)
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val text = s?.toString().orEmpty()
                            if (text != password) password = text
                        }
                    })
                    passwordView = this
                }
            },
            update = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasSavedPassword) {
            OutlinedButton(
                onClick = {
                    val a = activity ?: return@OutlinedButton
                    Log.d(TAG, "signIn tapped: calling getCredential")
                    viewModel.signInWithSavedPassword(
                        getCredential = { req ->
                            Log.d(TAG, "getCredential request: options=${req.credentialOptions.map { it::class.simpleName }}")
                            getCredential(a, req).also {
                                Log.d(TAG, "getCredential response type=${it.credential::class.simpleName}")
                            }
                        },
                        onFilled = { id, pw ->
                            Log.d(TAG, "onFilled: id=$id passwordLength=${pw.length}")
                            usernameView?.setText(id)
                            passwordView?.setText(pw)
                        }
                    )
                },
                enabled = activity != null && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in with saved password")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val a = activity
                Log.d(TAG, "login tapped: activity=${a != null} username.blank=${username.isBlank()} password.blank=${password.isBlank()}")
                if (a != null) {
                    viewModel.saveAndProceed(
                        username = username,
                        password = password,
                        createCredential = { req ->
                            Log.d(TAG, "createCredential: type=${req.type}")
                            createCredential(a, req).also {
                                Log.d(TAG, "createCredential response type=${it.type}")
                            }
                        }
                    )
                } else {
                    Log.w(TAG, "activity null; skipping save, navigating home")
                    onLoginSuccess()
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
        }

        errorMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        statusMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "This login uses SecureKey QWERTY keyboard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
