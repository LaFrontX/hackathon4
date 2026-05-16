package com.example.blatplat

import android.app.Application
import com.example.blatplat.data.local.DemoProfileStore
import com.example.blatplat.data.remote.NetworkModule
import com.example.blatplat.data.repository.TransportRepositoryImpl
import com.example.blatplat.data.session.SessionManager
import com.example.blatplat.domain.repository.TransportRepository

/**
 * Точка сборки зависимостей без DI-фреймворка: для хакатона достаточно явной инициализации в Application,
 * чтобы жюри видело простой жизненный цикл объектов (Session → OkHttp → Retrofit → Repository).
 */
class TransportApplication : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var repository: TransportRepository
        private set

    lateinit var demoProfileStore: DemoProfileStore
        private set

    override fun onCreate() {
        super.onCreate()
        demoProfileStore = DemoProfileStore()
        sessionManager = SessionManager(this)
        val okHttp = NetworkModule.createOkHttpClient(sessionManager)
        val api = NetworkModule.createApiService(okHttp)
        repository = TransportRepositoryImpl(api)
    }
}
