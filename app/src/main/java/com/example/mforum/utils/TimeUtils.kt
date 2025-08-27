package com.example.mforum.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.mforum.R

object TimeUtils {
    fun formatTime(timestamp: Long, context: Context): String {
        val adjustedTimestamp = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
        val now = System.currentTimeMillis()
        val diff = now - adjustedTimestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val resources = context.resources

        return when {
            seconds < 60 -> resources.getString(R.string.just_now)
            minutes < 60 -> resources.getQuantityString(R.plurals.minutes_ago, minutes.toInt(), minutes)
            hours < 24 -> resources.getQuantityString(R.plurals.hours_ago, hours.toInt(), hours)
            days == 1L -> resources.getString(R.string.yesterday)
            days < 7 -> resources.getQuantityString(R.plurals.days_ago, days.toInt(), days)
            else -> {
                val date = Date(adjustedTimestamp)
                val format = if (isSameYear(adjustedTimestamp, now)) {
                    SimpleDateFormat("MM-dd", Locale.getDefault())
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                }
                format.format(date)
            }
        }
    }

    private fun isSameYear(timestamp1: Long, timestamp2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
    }
}