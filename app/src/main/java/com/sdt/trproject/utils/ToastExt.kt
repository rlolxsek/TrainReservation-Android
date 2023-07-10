package com.sdt.trproject.utils

import android.content.Context
import android.widget.Toast

public fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).apply {
        cancel()
        show()
    }
}