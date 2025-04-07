# HavayeMan

Android Application for track weather status, made with kotlin.

## About This Project

This application helps users to track weather status for specific location. they can observe current weather status, weather
forecast and 5 days weather history.

This project uses room presistance library to cache weather data and uses retrofit library for sending requests to server and receive weather data. it also use dagger for dependency injection and kotlin coroutine for asynchronize processes.

The most important challenges that i faced:
implement the system that send many retrofit requests to server and receive response in just 2 sec.
In future and next versions i will improve application user interface (ui) and add some other features to track weather status most better.

HavayeMan supports two languages (fa and en) and has light and dark theme.

This application uses:
- Retrofit for rest api requests
- Mvvm architecture
- Room Presistance ORM for local cache
- Dagger Support library for dependency injection
- Google map service
- MpAndroidChart library for charts
- Fcm for push notification
- Glide library for load and cache images
- Jetpack Navigation Component for fragment transactions
- Kotlin Coroutine for asynchronize tasks(multi thread tasks)
- Live data
- Rx android Rx java
- JobService for notifications and update application widget

## How to use application

HavayeMan has three main sections:
- Current weather : to track current weather status
- Forecast weather : helps people know how the weather is going to be in next days.
- History weathe : in this section users can select specific location and see 5 days weather history.

## ScreenShots

![alt text](https://i.postimg.cc/8Pb3t8sC/Pixel-True-Mockup.png)  ![alt text](https://i.postimg.cc/g2k77M6H/Pixel-True-Mockup-1.png)  ![alt text](https://i.postimg.cc/MKwN7Z64/Pixel-True-Mockup-2.png)  ![alt text](https://i.postimg.cc/kGQH0zcf/Pixel-True-Mockup-3.png) 
