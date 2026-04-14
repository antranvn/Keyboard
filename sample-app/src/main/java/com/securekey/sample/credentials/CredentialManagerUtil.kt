package com.securekey.sample.credentials

import android.app.Activity
import androidx.credentials.CreateCredentialRequest
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PrepareGetCredentialResponse

suspend fun getCredential(
    activity: Activity,
    request: GetCredentialRequest
): GetCredentialResponse {
    val credentialManager = CredentialManager.create(activity)
    return credentialManager.getCredential(activity, request)
}

suspend fun createCredential(
    activity: Activity,
    request: CreateCredentialRequest
): CreateCredentialResponse {
    val credentialManager = CredentialManager.create(activity)
    return credentialManager.createCredential(activity, request)
}

suspend fun prepareGetCredential(
    activity: Activity,
    request: GetCredentialRequest
): PrepareGetCredentialResponse {
    val credentialManager = CredentialManager.create(activity)
    return credentialManager.prepareGetCredential(request)
}
