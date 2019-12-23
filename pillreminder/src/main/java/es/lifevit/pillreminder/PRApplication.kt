package es.lifevit.pillreminder

import android.app.Application

import es.lifevit.sdk.LifevitSDKManager


class PRApplication : Application() {

    lateinit var lifevitSDKManager: LifevitSDKManager
        internal set

    override fun onCreate() {
        super.onCreate()
        instance = this
        lifevitSDKManager = LifevitSDKManager(this)

        // startService(new Intent(this, LifeVitTensiService.class));
    }

    companion object {
        @JvmStatic
        var instance: PRApplication? = null
            private set
    }
}
