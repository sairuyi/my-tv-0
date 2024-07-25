package com.lizongying.mytv0.requests

import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.conscrypt.Conscrypt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Security
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object HttpClient {
    const val TAG = "HttpClient"
    private const val HOST = "https://ghproxy.org/https://raw.githubusercontent.com/vrichv/my-tv-0/"
    const val DOWNLOAD_HOST = "https://ghproxy.org/https://github.com/vrichv/my-tv-0/releases/download/"

    val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClient()
    }

    val releaseService: ReleaseService by lazy {
        Retrofit.Builder()
            .baseUrl(HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ReleaseService::class.java)
    }

    val configService: ConfigService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .build().create(ConfigService::class.java)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        // Init Conscrypt
        val conscrypt = Conscrypt.newProvider()
        // Add as provider
        Security.insertProviderAt(conscrypt, 1)
        // OkHttp 3.12.x
        // ConnectionSpec.COMPATIBLE_TLS = TLS1.0
        // ConnectionSpec.MODERN_TLS = TLS1.0 + TLS1.1 + TLS1.2 + TLS 1.3
        // ConnectionSpec.RESTRICTED_TLS = TLS 1.2 + TLS 1.3
        val okHttpBuilder = OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))

        val userAgentInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 4.4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
                )
                .build()
            chain.proceed(requestWithUserAgent)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        try {
            //FIXME: NOT-SAFE
            //val tm: X509TrustManager = Conscrypt.getDefaultX509TrustManager()
            val tm: X509TrustManager = InternalX509TrustManager()

            val sslContext = SSLContext.getInstance("TLS", conscrypt)
            sslContext.init(null, arrayOf(tm), null)
            okHttpBuilder.sslSocketFactory(InternalSSLSocketFactory(sslContext.socketFactory), tm)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up OkHttpClient", e)
        }

        return okHttpBuilder.dns(DnsCache()).retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor).build()
    }
}