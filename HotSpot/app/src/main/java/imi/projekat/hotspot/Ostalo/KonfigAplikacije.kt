package imi.projekat.hotspot

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.coroutineContext

object KonfigAplikacije{
    val instanca: ModelConfigAplikacije by lazy {
        getInstanca(SetupActivity.Companion.getContext()) as ModelConfigAplikacije
    }
    private fun getInstanca(kontekst:Context): ModelConfigAplikacije?= synchronized(this){

        val jsonString: String
        try {
            jsonString = kontekst.assets.open("ConfigAplikacije.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val type = object : TypeToken<ModelConfigAplikacije>() {}.type
            return gson.fromJson(jsonString, type)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
        return null
    }
}


data class ModelConfigAplikacije(
    val AppSettings: AppSettings
)

data class AppSettings(
    val baseURL: String,
    val MAPS_API_KEY:String
)