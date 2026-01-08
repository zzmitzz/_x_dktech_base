package com.emulator.retro.console.game.data.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.emulator.retro.console.game.adapters.main.HomeGameAdapterNovoSyx
import com.emulator.retro.console.game.data.remote.RetrixApiService
import com.emulator.retro.console.game.data.remote.models.RetrixData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("LogNotTimber")
object RetrixApiRepository {

    private const val BASE_URL = "http://inhouse-api.dktechgroup.vn/api/detech/"

    const val UNINITIALIZED = -1
    const val INITIALIZING = 0
    const val INITIALIZED = 1

    private val gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    private val retrofit = try {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(getUnsafeOkHttpClient())
            .build()
    } catch (e: RuntimeException) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val trustManagerFactory: TrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers: Array<TrustManager> =
                trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + trustManagers.contentToString()
            }

            val trustManager =
                trustManagers[0] as X509TrustManager


            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.callTimeout(1, TimeUnit.MINUTES)
            builder.readTimeout(1, TimeUnit.MINUTES)
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private val apiService by lazy {
        retrofit.create(RetrixApiService::class.java)
    }

    private var mState: Int = UNINITIALIZED
    private val mLDState = MutableLiveData(UNINITIALIZED)
    val mLoadingState: LiveData<Int>
        get() = mLDState

    private val mData = mutableListOf<RetrixData>()
    private val mMutableLiveData = MutableLiveData<List<RetrixData>>(emptyList())
    val mGames: LiveData<List<RetrixData>>
        get() = mMutableLiveData

    fun init(isReload: Boolean = false) {
        if (mState == INITIALIZING || (!isReload && mState == INITIALIZED)) return

        mState = INITIALIZING
        mLDState.value = mState

        mData.clear()
        mMutableLiveData.value = mData.toList()
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("RetrixApiRepositoryTAG", "init: ${System.currentTimeMillis()}")
            val response = withContext(Dispatchers.IO) {
                try {
                    apiService.getAll().execute()
                } catch (e: Throwable) {
                    Log.e("RetrixApiRepositoryTAG", "init: ", e)
                    null
                }
            }

            Log.d("RetrixApiRepositoryTAG", "init: ${response?.body()}")
            if (response == null || !response.isSuccessful || response.body()?.status == false) {
                Log.e("RetrixApiRepositoryTAG", "init: ${response?.code()} ${response?.body()?.status}")
                mState = UNINITIALIZED
                mLDState.value = mState
                return@launch
            }

            val data = response.body()?.data
            if (data.isNullOrEmpty()) {
                mState = UNINITIALIZED
                mLDState.value = mState
            } else {
                mState = INITIALIZED
                mLDState.value = mState
                mData.addAll(data)
                mMutableLiveData.value = mData
            }
        }
    }

    suspend fun getGameWithKey(mGames: List<RetrixData>, key: String): List<RetrixData> {
        return withContext(Dispatchers.IO) {
            val data = if (key.isEmpty()) mGames else search(
                key,
                mGames.map { it to it.title + " " + it.region + " " + it.platform }
            )
            if (data.isEmpty()) data else data.toMutableList().apply {
                add(0, HomeGameAdapterNovoSyx.itemAds)
            }.toList()
        }
    }

    private fun <T> search(key: String, sources: List<Pair<T, String>>): List<T> {
        if (sources.isEmpty()) return listOf()

        val keyWords = key.split("[^a-zA-Z0-9]+".toRegex())
        return sources.map {
            Pair(it.first, getRank(keyWords, it.second))
        }.filter {
            it.second > 0
        }.sortedByDescending {
            it.second
        }.map { it.first }
    }

    private fun getRank(keyWords: List<String>, source: String): Int {
        val sourceWords = source.split("[^a-zA-Z0-9]+".toRegex()).toMutableList()

        var count = 0
        keyWords.forEach { word ->
            while (sourceWords.any { it.contains(word, true) }) {
                count += sourceWords.count { it.contains(word, true) }
                sourceWords.removeIf { it.contains(word, true) }
            }
        }
        return count
    }
}