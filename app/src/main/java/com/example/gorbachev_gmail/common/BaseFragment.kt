package com.example.gorbachev_gmail.common

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.gorbachev_gmail.base.toast
import com.example.gorbachev_gmail.util.alertDialog

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
	
	fun toast(mes: String) {
		requireContext().toast(mes)
	}
	
	fun alertDialog(
		title: String,
		mes: String,
		positiveBtn: String,
		neutralBtn: String = "",
		NegativeBtn: String = "",
		countBtn: Int = 1,
	) {
		requireContext().alertDialog(title, mes, positiveBtn,neutralBtn,NegativeBtn,countBtn, requireContext())
	}
}