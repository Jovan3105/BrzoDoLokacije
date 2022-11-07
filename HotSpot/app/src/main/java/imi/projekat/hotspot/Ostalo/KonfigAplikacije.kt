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
    private var instanca:ModelConfigAplikacije?=null
    fun getInstanca(kontekst:Context): ModelConfigAplikacije?= synchronized(this){

        val jsonString: String
        if(instanca==null){
            try {
                jsonString = kontekst.assets.open("ConfigAplikacije.json").bufferedReader().use { it.readText() }
                val gson = Gson()
                val type = object : TypeToken<ModelConfigAplikacije>() {}.type
                instanca = gson.fromJson(jsonString, type)
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }

        }
        return instanca
    }
}


data class ModelConfigAplikacije(
    val AppSettings: AppSettings
)

data class AppSettings(
    val baseURL: String
)