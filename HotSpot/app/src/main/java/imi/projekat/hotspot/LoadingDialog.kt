package imi.projekat.hotspot

import android.app.Activity
import android.app.AlertDialog

class LoadingDialog(val myActivity:Activity) {
    private lateinit var isdialog:AlertDialog
    fun startLoading(){
        val infalter=myActivity.layoutInflater
        val dialogView=infalter.inflate(R.layout.custom_dialog,null)
        val builder=AlertDialog.Builder(myActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog=builder.create()
        isdialog.show()
    }

    fun isDismiss(){
        isdialog.dismiss()
    }
}