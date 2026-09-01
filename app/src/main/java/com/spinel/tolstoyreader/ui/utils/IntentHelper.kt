package com.spinel.tolstoyreader.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentHelper {

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun shareTextSafely(context: Context, text: String, errorMessage: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            val activity = context.findActivity()
            if (activity != null) {
                activity.startActivity(shareIntent)
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun openUrlSafely(context: Context, url: String, errorMessage: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val activity = context.findActivity()
            if (activity != null) {
                activity.startActivity(intent)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
