# ExWeather

professional android application for checking weather status, developed with kotlin.

## About This Project

This application helps users to track weather status for specific location. they can observe current weather status, weather
forecast and 5 days weather history.

This project uses room presistance library in order to cache weather data and uses retrofit library for sending requests to server and receive data. it also use dagger library for dependency injection and kotlin coroutine for asynchronize processes.
This application is developed with kotlin language cause kolin has a lot of features that java dosent have.
The most important challenges that i faced were to implement the system that send many retrofit requests to server and receive response in just 2 sec. and finally it happened thanks to coroutines!
In future and next versions i will improve application user interface (ui) and add some other features in order to track weather status most better.

And finally exWeather supports two languages (fa and en) and has light and dark theme.

This application uses:
- Retrofit for rest api requests
- Gson convertor factory for convert retrofit requests responses to objects
- Mvvm(model view viewmodel) for application architecture
- Room Presistance library as ORM for cache data in sqlite
- Dagger Support library for dependency injection
- Google map service for map
- MpAndroidChart library for charts
- GCM Firebase for firebase notification
- Glide library for load and cache images
- Jetpack Navigation Component for fragment transactions
- Kotlin Coroutine for asynchronize tasks(multi thread tasks)
- Live data
- Rx android Rx java for create an unti spam system for swipRefresh layout(throtleFirst operator) and search system using debounce operator
- ConstrantLayout for xml ui design
- KotlinExtensions plugin for getting rid of findViewById!!!
- Service for notifications and update application widget

## Development Sections

This project has 7 branches:
- master
- current_weather_component
- forecast_weather_component
- history_weather_component
- settings_component
- conclusion_component
- fix_bugs

And finally tag - v1.0.0

## How to export

The application codes doesnt obfuscated. so before sign it should become obfuscated.

## How to use application

This application has three main fragments, as it appeared in bottom navigaiton component:
- current weaher fragment: in order to track current weather status
- forecast weather component: lets people know how the weather going to be in next days.
- history weathe component: in this section users can select specific location and see 5 days weather history.

## ScreenShots

![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105138_1.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105254.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105311.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105321.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105326.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105411.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105433.png)  ![alt text](https://www.linkpicture.com/q/Screenshot_20220222-105445.png)

