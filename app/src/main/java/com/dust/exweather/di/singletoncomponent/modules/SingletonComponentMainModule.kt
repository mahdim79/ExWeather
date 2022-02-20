package com.dust.exweather.di.singletoncomponent.modules

import android.app.Application
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.room.Room
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.RoomManager
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.Constants
import com.dust.exweather.widget.WidgetUpdater
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class SingletonComponentMainModule {
    @Singleton
    @Provides
    fun provideWeatherApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder().callTimeout(20000, TimeUnit.MILLISECONDS)
                    .readTimeout(20000, TimeUnit.MILLISECONDS)
                    .writeTimeout(20000, TimeUnit.MILLISECONDS).build()
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideMainApiRequestInterface(retrofit: Retrofit): MainApiRequests {
        return retrofit.create(MainApiRequests::class.java)
    }

    @Singleton
    @Provides
    fun provideRoomDatabase(application: Application): RoomManager {
        return Room.databaseBuilder(
            application.applicationContext,
            RoomManager::class.java,
            "main_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideWeatherDao(roomManager: RoomManager): WeatherDao {
        return roomManager.getWeatherDao()
    }

    @Singleton
    @Provides
    fun provideSharedPreferencesManager(application: Application): SharedPreferencesManager =
        SharedPreferencesManager(application.applicationContext)

    @Singleton
    @Provides
    fun provideUnitManager(sharedPreferencesManager: SharedPreferencesManager): UnitManager {
        return UnitManager(sharedPreferencesManager)
    }

    @Provides
    fun provideInputMethodManager(application: Application): InputMethodManager {
        return application.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    @Singleton
    @Provides
    fun provideWidgetUpdater(
        weatherDao: WeatherDao,
        sharedPreferencesManager: SharedPreferencesManager,
        application: Application,
        unitManager: UnitManager,
        mainApiRequests: MainApiRequests
    ): WidgetUpdater {
        return WidgetUpdater(
            weatherDao,
            mainApiRequests,
            application.applicationContext,
            sharedPreferencesManager,
            unitManager
        )
    }
}