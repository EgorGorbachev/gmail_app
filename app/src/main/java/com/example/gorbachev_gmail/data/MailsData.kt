package com.example.gorbachev_gmail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mails_table")
data class MailsData(
	@PrimaryKey(autoGenerate = true)
	val id: Int,
	val from: String,
	val subject: String,
	val date: String
)