package com.dust.exweather.di.singletoncomponent.modules

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.room.Room
import com.dust.exweather.model.dataclasses.currentweather.other.WeatherStatesDetails
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.retrofit.TranslationApiRequests
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.CurrentWeatherDao
import com.dust.exweather.model.room.RoomManager
import com.dust.exweather.utils.Constants
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class SingletonComponentMainModule {
    @Singleton
    @Provides
    @Named("weather_api_com")
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
    @Named("translation_api")
    fun provideTranslationApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.TRANSLATION_API_BASE_URL)
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
    fun provideTranslationApiRequestInterface(@Named("translation_api") retrofit: Retrofit):TranslationApiRequests{
        return retrofit.create(TranslationApiRequests::class.java)
    }

    @Singleton
    @Provides
    fun provideMainApiRequestInterface(@Named("weather_api_com") retrofit: Retrofit): MainApiRequests {
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
    fun provideMainDao(roomManager: RoomManager): CityDao {
        return roomManager.getCityDao()
    }

    @Singleton
    @Provides
    fun provideCurrentWeatherDao(roomManager: RoomManager): CurrentWeatherDao {
        return roomManager.getCurrentWeatherDao()
    }

    @Singleton
    @Provides
    fun provideLocationManager(application: Application): LocationManager {
        return (application.applicationContext.getSystemService(Context.LOCATION_SERVICE)) as LocationManager
    }

    @Singleton
    @Provides
    fun provideWeatherStatesDetailsObject(application: Application): WeatherStatesDetails {
        val inputStream =
            application.applicationContext.resources.assets.open("json/weather_conditions.json")
        val bufferedInputStream = BufferedInputStream(inputStream)
        val stringBuilder = StringBuilder()
        while (bufferedInputStream.available() != -1)
            stringBuilder.append(bufferedInputStream.read() as Char)
        return Gson().fromJson(stringBuilder.toString() , WeatherStatesDetails::class.java)
    }
}