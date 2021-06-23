package com.example.gorbachev_gmail.fragments

import android.accounts.Account
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gorbachev_gmail.R
import com.example.gorbachev_gmail.USER_REMEMBER
import com.example.gorbachev_gmail.adapters.CustomRecyclerAdapter
import com.example.gorbachev_gmail.common.BaseFragment
import com.example.gorbachev_gmail.data.MailsData
import com.example.gorbachev_gmail.data.MailsViewModel
import com.example.gorbachev_gmail.sharedPref.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.io.IOException


class mailsFr : BaseFragment(R.layout.fragment_mails) {
	
	private lateinit var mGoogleSignInClient: GoogleSignInClient
	
	private var messageTV: TextView? = null
	private lateinit var btnSuingOut: Button
	private lateinit var progressBar: ProgressBar
	private lateinit var progressBar2: ProgressBar
	
	private lateinit var SP: SharedPreferences
	
	private lateinit var credential: GoogleAccountCredential
	
	private var service: Gmail? = null
	
	private val RQ_GMAIL_ACCESS = 111
	
	private lateinit var allEmails: MutableList<Map<String, String>>
	private var mailsAttachments: MutableList<MessagePart?> = mutableListOf()
	
	// Database
	private lateinit var mMailsViewModel: MailsViewModel
	
	//Adapter
	private lateinit var adapter: CustomRecyclerAdapter
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_mails, container, false)
		
		SP = SharedPreferences(requireContext())
		
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.build()
		
		mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
		
		
		messageTV = view.findViewById(R.id.message)
		
		allEmails = mutableListOf()
		
		
		progressBar = view.findViewById(R.id.progressBar)
		progressBar2 = view.findViewById(R.id.progressBar2)
		progressBar2.isVisible = false
		
		btnSuingOut = view.findViewById(R.id.singOut)
		btnSuingOut.setOnClickListener {
			signOut()
			Navigation.findNavController(requireView()).navigate(R.id.action_mailsFr_to_mainFr)
			SP.setPref(USER_REMEMBER, false)
		}
		
		mMailsViewModel = ViewModelProvider(this).get(MailsViewModel::class.java)
		
		
		
		if (!isOnline()) {
			getMesFromDatabase()
		} else {
			getCredentials()
			service()
		}
		
		
		
		GlobalScope.launch(Dispatchers.Main) {
			delay(5000)
			adapter = CustomRecyclerAdapter(allEmails, mailsAttachments, service, requireContext())
			val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
			recyclerView.layoutManager = LinearLayoutManager(requireContext())
			recyclerView.adapter = adapter
			Log.d("lol", "$allEmails")
			progressBar.isVisible = false
			progressBar2.isVisible = true
		}
		
		val callback: OnBackPressedCallback =
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					activity?.finish()
				}
			}
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
		
		
		return view
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (!isOnline()) {
			Toast.makeText(context, "You have no internet connection", Toast.LENGTH_LONG).show()
		}
	}

// Database ///////////////////////////////////////////////////////
	
	private fun insertDataToDatabase() {
		Log.d("lol", "${allEmails.size}")
		for (el in 0 until allEmails.size) {
			val from = allEmails[el]["From"]!!
			val subject = allEmails[el]["Subject"]!!
			val date = allEmails[el]["Date"]!!
			
			val mail = MailsData(0, from, subject, date)
			mMailsViewModel.addMail(mail)
		}
	}
	
	
	private fun deleteAllMails() {
		mMailsViewModel.deleteAllMails()
	}
	
	
	private fun getMesFromDatabase() {
		mMailsViewModel.readAllData.observe(requireActivity(), Observer { mails ->
			Log.d("mails", "$mails")
			for (mail in mails) {
				val currEmail = mutableMapOf<String, String>()
				currEmail["From"] = mail.from
				currEmail["Subject"] = mail.subject
				currEmail["Date"] = mail.date
				allEmails.add(currEmail)
			}
			
		})
	}


// Check internet connection //////////////////////////////////////////////
	
	private fun isOnline(): Boolean {
		val runtime = Runtime.getRuntime()
		try {
			val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
			val exitValue = ipProcess.waitFor()
			return exitValue == 0
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}
		return false
	}


// Sing out ////////////////////////////////////////////////////////////
	
	private fun signOut() {
		mGoogleSignInClient.signOut()
			.addOnCompleteListener((context as Activity?)!!, OnCompleteListener<Void?> {
				toast("Signed out successfully")
			})
	}


// Get mails /////////////////////////////////////////////////////////
	
	
	private fun getCredentials() {
		credential = GoogleAccountCredential.usingOAuth2(
			requireContext(), listOf(GmailScopes.GMAIL_READONLY)
		)
			.setBackOff(ExponentialBackOff())
			.setSelectedAccount(
				Account(
					FirebaseAuth.getInstance().currentUser?.email,
					BuildConfig.APPLICATION_ID
				)
			)
	}
	
	private fun service() {
		service = Gmail.Builder(
			NetHttpTransport(), AndroidJsonFactory.getDefaultInstance(), credential
		)
			.setApplicationName("MyApp")
			.build()
		
		GlobalScope.launch {
			readMes()
		}
		
	}
	
	private suspend fun readMes() {
		try {
			val executeResult: ListMessagesResponse? =
				withContext(Dispatchers.IO) {
					service?.users()?.messages()?.list("me")?.setQ("to:me")?.execute()
				}
			
			
			val message: List<Message>? = executeResult!!.messages
			var count = 0
			for (i in message!!.indices) {
				
				val user = "me"
				val request = service?.users()?.messages()?.list(user)
				val messagesResponse: ListMessagesResponse = request!!.execute()
				
				
				Log.d("size", "$count")
				count++
				
				val messageId: String = messagesResponse.messages[i].id
				val currMessage = service?.users()?.messages()?.get(user, messageId)?.execute()
				val parts = currMessage?.payload?.parts
				
				val currEmail = mutableMapOf<String, String>()
				var attachments = false
				
				if (parts != null) {
					for (part in parts) {
						if (part.getValue("filename").toString().isNotEmpty()) {
							attachments = true
							mailsAttachments.add(part)
						}
						currEmail["Attachments"] = attachments.toString()
					}
					if (!attachments) {
						attachments = false
						currEmail["Attachments"] = attachments.toString()
						mailsAttachments.add(null)
					}
				} else {
					mailsAttachments.add(null)
					attachments = false
					currEmail["Attachments"] = attachments.toString()
				}
				
				for (value in currMessage?.payload!!.headers) {
					when (value.name) {
						"From" -> {
							val senderName = value.value.substring(0, value.value.indexOf("<"))
								.replace("\"", "")
							currEmail["From"] = senderName
						}
						"Subject" -> {
							if (value.value.isEmpty()) {
								currEmail["Subject"] = "No subject"
							} else {
								currEmail["Subject"] = value.value
							}
						}
						"Date" -> {
							val date = value.value.substring(0, 25)
							currEmail["Date"] = date
						}
						else -> {
							false
						}
					}
				}
				allEmails.add(currEmail)
				
				Log.d("lol", "${allEmails[i]}")
			}
			GlobalScope.launch(Dispatchers.Main) {
				adapter.notifyDataSetChanged()
				progressBar2.isVisible = false
			}
			
			
			
			deleteAllMails()
			insertDataToDatabase()
			
		} catch (e: UserRecoverableAuthIOException) {
			startActivityForResult(e.intent, RQ_GMAIL_ACCESS);
		}
		Log.d("aaa", "$allEmails")
		
	}
}
