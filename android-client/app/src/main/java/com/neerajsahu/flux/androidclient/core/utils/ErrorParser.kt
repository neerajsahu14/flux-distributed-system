package com.neerajsahu.flux.androidclient.core.utils

import android.content.Context
import com.neerajsahu.flux.androidclient.R
import org.json.JSONObject
import javax.inject.Inject

class ErrorParser @Inject constructor(private val context: Context) {
    fun parse(errorBodyString: String?): String {
        return try {
            if (errorBodyString.isNullOrBlank()) {
                return context.getString(R.string.something_went_wrong)
            }
            val jsonObject = JSONObject(errorBodyString)
            if (jsonObject.has("message")) {
                jsonObject.getString("message")
            } else if (jsonObject.has("error")) {
                jsonObject.getString("error")
            } else {
                context.getString(R.string.an_unknown_error_occurred)
            }
        } catch (e: Exception) {
             context.getString(R.string.an_error_occurred)
        }
    }
}
