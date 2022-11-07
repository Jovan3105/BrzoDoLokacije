package imi.projekat.hotspot.Ostalo

import android.content.Context

class UpravljanjeResursima {
    companion object {
        fun getResourceString(name: String, context: Context): String? {
            val nameResourceID: Int = context.getResources().getIdentifier(
                name,
                "string", context.getApplicationInfo().packageName
            )
            return if (nameResourceID == 0) {
                throw IllegalArgumentException(
                    "No resource string found with name $name"
                )
            } else {
                context.getString(nameResourceID)
            }
        }
    }
}