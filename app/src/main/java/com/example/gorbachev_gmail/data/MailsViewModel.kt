package com.example.gorbachev_gmail.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MailsViewModel(application: Application) : AndroidViewModel(application) {
	
	val readAllData: LiveData<List<MailsData>>
	private val repository: MailsRepository
	
	init {
		val mailsDao = MailsDatabase.getDatabase(application).mailsDao()
		repository = MailsRepository(mailsDao)
		readAllData = repository.readAllData
	}
	
	fun addMail(mail: MailsData) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.addMail(mail)
		}
	}
	
	fun deleteAllMails() {
		viewModelScope.launch(Dispatchers.IO) {
			repository.deleteAllMails()
		}
	}
}