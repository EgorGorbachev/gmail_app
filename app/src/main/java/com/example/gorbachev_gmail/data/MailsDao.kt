package com.example.gorbachev_gmail.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MailsDao {
	
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun addMail(Mail: MailsData)
	
	@Query("SELECT * FROM mails_table ORDER BY id ASC")
	fun readAllData(): LiveData<List<MailsData>>
	
	@Query("DELETE FROM mails_table")
	fun deleteAllMails()
}