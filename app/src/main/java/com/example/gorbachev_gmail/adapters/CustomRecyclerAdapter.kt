package com.example.gorbachev_gmail.adapters

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.gorbachev_gmail.R
import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CustomRecyclerAdapter(
	private val emailsList: MutableList<Map<String, String>>,
	private val mailsAttachments: MutableList<MessagePart?>,
	private val service: Gmail?,
	private val context: Context
) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {
	
	class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		var from: TextView
		var subject: TextView
		var date: TextView
		var AttachmentsLayout: LinearLayout
		
		init {
			from = itemView.findViewById(R.id.fromTV)
			subject = itemView.findViewById(R.id.subjectTV)
			date = itemView.findViewById(R.id.dateTV)
			AttachmentsLayout = itemView.findViewById(R.id.AttachmentsLayout)
			AttachmentsLayout.isVisible = false
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
		val itemView =
			LayoutInflater.from(parent.context)
				.inflate(R.layout.recycler_item, parent, false)
		return MyViewHolder(itemView)
	}
	
	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		holder.from.text = emailsList[position]["From"]
		holder.subject.text = emailsList[position]["Subject"]
		holder.date.text = emailsList[position]["Date"]
		if (service != null) {
			if (emailsList[position]["Attachments"] != "false") {
				holder.AttachmentsLayout.isVisible = true
				holder.AttachmentsLayout.setOnClickListener {
					if (mailsAttachments[holder.adapterPosition] == null) {
						Toast.makeText(
							context,
							"This attachment cannot be downloaded",
							Toast.LENGTH_SHORT
						).show()
					} else {
						GlobalScope.async(Dispatchers.Main) {
							println(mailsAttachments[holder.adapterPosition])
							downloadAttachments(
								mailsAttachments[holder.adapterPosition]!!,
								service!!
							)
						}
					}
				}
				
			}
		}
	}
	
	
	override fun getItemCount() = emailsList.size
	
	suspend fun downloadAttachments(part: MessagePart, service: Gmail) {
		val filename: String = part.filename
		val attId: String = part.body.attachmentId
		var attachPart: MessagePartBody? = null
		val file = File(
			"${
				Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS
				)
			}/${filename}"
		)
		try {
			try {
				attachPart =
					withContext(Dispatchers.IO) {
						service.users().messages().attachments().get("me", part.partId, attId)
							.execute()
					}
			} catch (ex: Exception) {
				println("------------------------${ex.stackTraceToString()}")
			}
			val fileByteArray: ByteArray = Base64.decodeBase64(attachPart?.data)
			file.createNewFile()
			val fOut = FileOutputStream(file)
			fOut.write(fileByteArray)
			fOut.close()
			Toast.makeText(context, "Attachment was saved", Toast.LENGTH_SHORT).show()
			println("------------------------READY")
		} catch (ex: IOException) {
			println("------------------------${ex.stackTraceToString()}")
		}
	}
}