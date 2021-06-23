package com.example.gorbachev_gmail.data

import androidx.lifecycle.LiveData

class MailsRepository(private val mailsDao: MailsDao) {
	
	val readAllData: LiveData<List<MailsData>> = mailsDao.readAllData()
	
	suspend fun addMail(mail: MailsData) {
		mailsDao.addMail(mail)
	}
	
	suspend fun deleteAllMails() {
		mailsDao.deleteAllMails()
	}
}