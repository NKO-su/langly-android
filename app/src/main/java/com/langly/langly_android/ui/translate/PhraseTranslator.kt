package com.langly.langly_android.ui.translate

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "PhraseTranslator"
private const val DOWNLOAD_TIMEOUT_MS = 90_000L

object PhraseTranslator {

    private val translator by lazy {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build()
        )
    }

    private val vietnameseModel = TranslateRemoteModel.Builder(TranslateLanguage.VIETNAMESE).build()

    suspend fun isReady(): Boolean {
        val ready = RemoteModelManager.getInstance().isModelDownloaded(vietnameseModel).await()
        Log.d(TAG, "isReady = $ready")
        return ready
    }

    suspend fun download() {
        Log.d(TAG, "download start")
        val finished = withTimeoutOrNull(DOWNLOAD_TIMEOUT_MS) {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            true
        }
        if (finished == null) {
            Log.e(TAG, "download timed out after $DOWNLOAD_TIMEOUT_MS ms")
            throw IllegalStateException("Model download timed out")
        }
        Log.d(TAG, "download done")
    }

    suspend fun translate(text: String): String = translator.translate(text).await()
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}