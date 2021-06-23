package com.example.gorbachev_gmail.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.w3c.dom.UserDataHandler

@Database(entities = [MailsData::class], version = 2, exportSchema = false)
abstract class MailsDatabase : RoomDatabase() {
	
	abstract fun mailsDao(): MailsDao
	
	companion object {
		@Volatile
		private var INSTANCE: MailsDatabase? = null
		
		fun getDatabase(context: Context): MailsDatabase {
			val tempInstance = INSTANCE
			if (tempInstance != null) {
				return tempInstance
			}
			synchronized(this) {
				val instance = Room.databaseBuilder(
					context.applicationContext,
					MailsDatabase::class.java,
					"mails_database"
				).build()
				INSTANCE = instance
				return instance
			}
		}
	}
}