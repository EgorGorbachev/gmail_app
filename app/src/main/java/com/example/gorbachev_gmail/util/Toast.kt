package com.example.gorbachev_gmail.base

import android.content.Context
import android.widget.Toast

fun Context.toast(messageStringRes: String) {
	
	val toast = Toast.makeText(this, messageStringRes, Toast.LENGTH_SHORT)
	toast.show()
}