package imi.projekat.hotspot.Ostalo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import imi.projekat.hotspot.R

object MenadzerSesije {

    const val USER_TOKEN = "user_token"
    const val USER_REFRESHTOKEN = "user_refresh_token"
    var logOut=false
    fun saveAuthToken(context: Context, token: String) {
        saveString(context, USER_TOKEN, token)
        APIservis.ResetServis()
    }

    fun saveRefteshToken(context: Context, token: String) {
        saveString(context, USER_REFRESHTOKEN, token)
        APIservis.ResetServis()
    }

    fun getToken(context: Context): String? {
        return getString(context, USER_TOKEN)
    }

    fun getRefreshToken(context: Context): String? {
        return getString(context, USER_REFRESHTOKEN)
    }

    fun saveString(context: Context, key: String, value: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun clearData(context: Context){
        val editor = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
        APIservis.ResetServis()
    }
}