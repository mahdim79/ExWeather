package com.dust.exweather.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.ui.activities.SplashActivity
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.UtilityFunctions
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(private val sharedPreferencesManager:SharedPreferencesManager) {

    private fun createNotificationChannel(context: Context): String {
        val channelId = context.getString(R.string.default_notification_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "havayeman_notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = Color.BLUE
            channel.vibrationPattern = longArrayOf(0L)
            channel.enableVibration(false)
            channel.setSound(null, null)

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return channelId
    }

    fun sendNotification(data: MainWeatherData, con: Context) {

        val context = if (sharedPreferencesManager.getLanguageSettings() == Settings.LANGUAGE_PERSIAN){
            val configuration = Configuration()
            configuration.setLocale(Locale("fa"))
            con.createConfigurationContext(configuration)
        }else{
            con
        }

        val contentIntent = Intent(context, SplashActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        sharedPreferencesManager.setLastNotificationTimeEpoch(System.currentTimeMillis())
        val notification = NotificationCompat.Builder(context, createNotificationChannel(context))
            .setCustomContentView(RemoteViews(context.packageName, R.layout.notification_normal_layout))
            .setCustomBigContentView(RemoteViews(context.packageName, R.layout.notification_big_layout))
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    201,
                    contentIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setVibrate(LongArray(3) { 200L })
            .build()

        data.current?.let { current ->
            current.current?.let { currentData ->

                notification.contentView.apply {

                    current.location?.let { locaiton ->
                        setTextViewText(R.id.locationTextView, locaiton.name)
                    }

                    setTextViewText(
                        R.id.temperatureText,
                        context.resources.getString(
                            R.string.temperatureText,
                            currentData.temp_c.toString()
                        )
                    )

                    setTextViewText(
                        R.id.precipitationText,
                        context.resources.getString(
                            R.string.precipitationText,
                            currentData.precip_mm.toString()
                        )
                    )

                    setTextViewText(R.id.weatherStateText, UtilityFunctions.getConditionText(currentData.condition.text,currentData.condition.code,context))

                    UtilityFunctions.getWeatherIconResId(current.current.condition.icon,current.current.is_day, context)?.let { icon ->
                        setImageViewResource(R.id.weatherStateImage, icon)
                    }

                }

                notification.bigContentView.apply {
                    setTextViewText(
                        R.id.temperatureTextView,
                        context.resources.getString(
                            R.string.temperatureText,
                            currentData.temp_c.toString()
                        )
                    )

                    setTextViewText(
                        R.id.windSpeedTextView,
                        (context.resources.getString(
                            R.string.windSpeedText,
                            currentData.wind_kph.toString()
                        ).plus(" ").plus(currentData.wind_dir)
                                )
                    )

                    setTextViewText(
                        R.id.weatherStateText,
                        UtilityFunctions.getConditionText(currentData.condition.text,currentData.condition.code,context)
                    )

                    current.location?.let { location ->
                        setTextViewText(
                            R.id.localTimeTextView,
                            UtilityFunctions.calculateCurrentTimeByTimeEpoch(
                                location.localtime_epoch,
                                location.tz_id
                            )
                        )

                        setTextViewText(
                            R.id.dayOfWeekTextView,
                            UtilityFunctions.getDayOfWeekByUnixTimeStamp(location.localtime_epoch,context))

                        setTextViewText(
                            R.id.localDateTextView,
                            UtilityFunctions.calculateCurrentDateByTimeEpoch(
                                location.localtime_epoch,
                                location.tz_id
                            ))

                        setTextViewText(R.id.locationNameTextView, location.name)
                    }

                    setTextViewText(
                        R.id.airPressureText,
                        context.resources.getString(
                            R.string.airPressureText,
                            currentData.pressure_mb.toString()
                        )
                    )

                    setTextViewText(
                        R.id.precipitationText,
                        context.resources.getString(
                            R.string.precipitationText,
                            currentData.precip_mm.toString()
                        )
                    )
                    setTextViewText(
                        R.id.humidityText,
                        context.resources.getString(
                            R.string.humidityText,
                            currentData.humidity.toString()
                        )
                    )

                    UtilityFunctions.getWeatherIconResId(current.current.condition.icon,current.current.is_day, context)?.let { icon ->
                        setImageViewResource(R.id.weatherStateImage, icon)
                    }

                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(101, notification)
            }
        }
    }

}