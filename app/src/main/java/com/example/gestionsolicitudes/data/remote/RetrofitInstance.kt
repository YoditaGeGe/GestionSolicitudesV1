package com.example.gestionsolicitudes.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

object RetrofitInstance {

    fun crear(context: Context): ApiService {
        val cliente = OkHttpClient.Builder()
            .addInterceptor(claveDeLaApp(context.applicationContext))
            .build()
        return Retrofit.Builder()
            .baseUrl("https://firestore.googleapis.com/v1/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun claveDeLaApp(context: Context) = Interceptor { cadena ->
        val pedido = cadena.request().newBuilder()
            .header("X-Android-Package", context.packageName)
            .header("X-Android-Cert", firmaSha1(context))
            .build()
        cadena.proceed(pedido)
    }

    private fun firmaSha1(context: Context): String {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        }
        val firma = info.signingInfo?.apkContentsSigners?.firstOrNull() ?: return ""
        val digest = MessageDigest.getInstance("SHA-1").digest(firma.toByteArray())
        return digest.joinToString("") { "%02X".format(it) }
    }
}
