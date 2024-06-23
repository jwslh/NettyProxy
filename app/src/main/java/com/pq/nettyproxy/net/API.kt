package com.pq.nettyproxy.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object API {
    private val okHttpClient by lazy {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
        OkHttpClient.Builder()
            .hostnameVerifier(HostnameVerifier { s, _ ->
                s == "localhost"
            })
            .sslSocketFactory(
                SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf(trustManager), SecureRandom())
                }.socketFactory,
                trustManager
            )
            .build()
    }

    suspend fun send(request: Request): Response? {
        return try {
            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }
}