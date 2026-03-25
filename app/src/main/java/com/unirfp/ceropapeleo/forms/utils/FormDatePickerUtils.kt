package com.unirfp.ceropapeleo.forms.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberTodayMillis(): Long {
    return remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

@Composable
fun rememberOneYearFromTodayMillis(): Long {
    return remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.YEAR, 1)
        }.timeInMillis
    }
}