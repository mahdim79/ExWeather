package com.dust.exweather.model.csv

import android.os.Environment
import com.dust.exweather.di.contributefragments.scopes.WeatherHistoryScope
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.utils.DataStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@WeatherHistoryScope
class CsvFactory @Inject constructor() {

    fun createWeatherHistorySheet(
        fileName: String,
        data: Forecastday,
        location: String
    ): DataWrapper<String> {
        val rootDir = Environment.getExternalStorageDirectory().absolutePath

        // insert data to excel file
        val hssfWorkbook = HSSFWorkbook()
        hssfWorkbook.createSheet("Report History").apply {
            createRow(0).apply {
                createCell(0).setCellValue("Location")
                createCell(1).setCellValue(location)
            }
            createRow(1).apply {
                createCell(0).setCellValue("Date")
                createCell(1).setCellValue(data.date)
            }
            createRow(2).apply {
                createCell(0).setCellValue("Condition")
                createCell(1).setCellValue(data.day.condition.text)
            }
            createRow(4).apply {
                createCell(0).setCellValue("Condition Code")
                createCell(1).setCellValue(data.day.condition.code.toString())
            }
            createRow(5).apply {
                createCell(0).setCellValue("dayOfWeek")
                createCell(1).setCellValue(data.day.dayOfWeek)
            }
            createRow(6).apply {
                createCell(0).setCellValue("avgvis_miles")
                createCell(1).setCellValue(data.day.avgvis_miles.toString())
            }

            createRow(7).apply {
                createCell(0).setCellValue("avgvis_km")
                createCell(1).setCellValue(data.day.avgvis_km.toString())
            }

            createRow(8).apply {
                createCell(0).setCellValue("avgtemp_f")
                createCell(1).setCellValue(data.day.avgtemp_f.toString())

            }

            createRow(9).apply {
                createCell(0).setCellValue("avgtemp_c")
                createCell(1).setCellValue(data.day.avgtemp_c.toString())
            }

            createRow(10).apply {
                createCell(0).setCellValue("maxtemp_c")
                createCell(1).setCellValue(data.day.maxtemp_c.toString())
            }

            createRow(11).apply {
                createCell(0).setCellValue("maxtemp_f")
                createCell(1).setCellValue(data.day.maxtemp_f.toString())
            }

            createRow(12).apply {
                createCell(0).setCellValue("mintemp_c")
                createCell(1).setCellValue(data.day.mintemp_c.toString())

            }

            createRow(13).apply {
                createCell(0).setCellValue("mintemp_f")
                createCell(1).setCellValue(data.day.mintemp_f.toString())

            }

            createRow(14).apply {
                createCell(0).setCellValue("maxwind_kph")
                createCell(1).setCellValue(data.day.maxwind_kph.toString())
            }

            createRow(15).apply {
                createCell(0).setCellValue("maxwind_mph")
                createCell(1).setCellValue(data.day.maxwind_mph.toString())
            }

            createRow(16).apply {
                createCell(0).setCellValue("totalprecip_in")
                createCell(1).setCellValue(data.day.totalprecip_in.toString())
            }

            createRow(17).apply {
                createCell(0).setCellValue("totalprecip_mm")
                createCell(1).setCellValue(data.day.totalprecip_mm.toString())
            }

            createRow(18).apply {
                createCell(0).setCellValue("uv")
                createCell(1).setCellValue(data.day.uv.toString())
            }

            createRow(19).apply {
                createCell(0).setCellValue("date_epoch")
                createCell(1).setCellValue(data.date_epoch.toString())
            }

            createRow(20).apply {
                createCell(0).setCellValue("avghumidity")
                createCell(1).setCellValue(data.day.avghumidity.toString())
            }
        }

        hssfWorkbook.createSheet("Hourly Data").apply {
            createRow(0).apply {
                createCell(0).setCellValue("Hourly Data")
            }
            createRow(2).apply {
                createCell(0).setCellValue("Time")
                createCell(1).setCellValue("Condition")
                createCell(2).setCellValue("Condition Code")
                createCell(3).setCellValue("dewpoint_c")
                createCell(4).setCellValue("dewpoint_f")
                createCell(5).setCellValue("feelslike_c")
                createCell(6).setCellValue("feelslike_f")
                createCell(7).setCellValue("gust_kph")
                createCell(8).setCellValue("gust_mph")
                createCell(9).setCellValue("heatindex_c")
                createCell(10).setCellValue(".heatindex_f")
                createCell(11).setCellValue(".precip_in")
                createCell(12).setCellValue(".precip_mm")
                createCell(13).setCellValue(".pressure_in")
                createCell(14).setCellValue(".pressure_mb")
                createCell(15).setCellValue(".temp_c")
                createCell(16).setCellValue(".temp_f")
                createCell(17).setCellValue(".vis_km")
                createCell(18).setCellValue(".vis_miles")
                createCell(19).setCellValue(".wind_dir")
                createCell(20).setCellValue(".wind_kph")
                createCell(21).setCellValue(".wind_mph")
                createCell(22).setCellValue(".windchill_c")
                createCell(23).setCellValue(".windchill_f")
            }

            for (i in data.hour.indices) {
                createRow(i + 3).apply {
                    createCell(0).setCellValue(data.hour[i].time)
                    createCell(1).setCellValue(data.hour[i].condition.text)
                    createCell(2).setCellValue(data.hour[i].condition.code.toString())
                    createCell(3).setCellValue(data.hour[i].dewpoint_c.toString())
                    createCell(4).setCellValue(data.hour[i].dewpoint_f.toString())
                    createCell(5).setCellValue(data.hour[i].feelslike_c.toString())
                    createCell(6).setCellValue(data.hour[i].feelslike_f.toString())
                    createCell(7).setCellValue(data.hour[i].gust_kph.toString())
                    createCell(8).setCellValue(data.hour[i].gust_mph.toString())
                    createCell(9).setCellValue(data.hour[i].heatindex_c.toString())
                    createCell(10).setCellValue(data.hour[i].heatindex_f.toString())
                    createCell(11).setCellValue(data.hour[i].precip_in.toString())
                    createCell(12).setCellValue(data.hour[i].precip_mm.toString())
                    createCell(13).setCellValue(data.hour[i].pressure_in.toString())
                    createCell(14).setCellValue(data.hour[i].pressure_mb.toString())
                    createCell(15).setCellValue(data.hour[i].temp_c.toString())
                    createCell(16).setCellValue(data.hour[i].temp_f.toString())
                    createCell(17).setCellValue(data.hour[i].vis_km.toString())
                    createCell(18).setCellValue(data.hour[i].vis_miles.toString())
                    createCell(19).setCellValue(data.hour[i].wind_dir.toString())
                    createCell(20).setCellValue(data.hour[i].wind_kph.toString())
                    createCell(21).setCellValue(data.hour[i].wind_mph.toString())
                    createCell(22).setCellValue(data.hour[i].windchill_c.toString())
                    createCell(23).setCellValue(data.hour[i].windchill_f.toString())
                }
            }
        }

        // output xls file
        val file = File(rootDir, "/ExWeather/exports")
        if (!file.exists())
            file.mkdirs()

        val file2 = File(file, "$fileName.xls")
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file2)
            hssfWorkbook.write(fileOutputStream)
        } catch (e: Exception) {
            return DataWrapper(e.message, DataStatus.DATA_SAVE_FAILURE)
        } finally {
            try {
                fileOutputStream?.let {
                    it.flush()
                    it.close()
                }
            } catch (e: Exception) {
            }
        }
        return DataWrapper(file2.absolutePath, DataStatus.DATA_SAVED_SUCCESS)
    }
}