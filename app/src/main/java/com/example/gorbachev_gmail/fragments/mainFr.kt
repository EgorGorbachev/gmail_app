package com.example.gorbachev_gmail.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import com.example.gorbachev_gmail.R
import com.example.gorbachev_gmail.USER_REMEMBER
import com.example.gorbachev_gmail.common.BaseFragment
import com.example.gorbachev_gmail.sharedPref.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.io.IOException

class mainFr : BaseFragment(R.layout.fragment_main) {
	
	private lateinit var rememberCheckBox: CheckBox
	
	private lateinit var SP: SharedPreferences
	
	private lateinit var auth: FirebaseAuth
	private val RC_SIGN_IN: Int = 111
	private var googleSignInClient: GoogleSignInClient? = null
	
	private lateinit var SingInBtn: SignInButton
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_main, container, false)
		
		auth = FirebaseAuth.getInstance()
		
		createRequest()
		
		SP = SharedPreferences(requireContext())
		
		rememberCheckBox = view.findViewById(R.id.rememberCheckBox)
		
		SingInBtn = view.findViewById(R.id.sign_in_button)
		SingInBtn.setOnClickListener {
			signIn()
			if (rememberCheckBox.isChecked) {
				SP.setPref(USER_REMEMBER, true)
			} else {
				SP.setPref(USER_REMEMBER, false)
			}
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
		mailActivityTransition()
		if (!isOnline()) {
			toast("You have no internet connection")
		}
	}
	
	private fun mailActivityTransition() {
		val user = auth.currentUser
		if (SP.getPrefBool(USER_REMEMBER) == true) {
			if (user != null) {
				Navigation.findNavController(requireView()).navigate(R.id.action_mainFr_to_mailsFr)
			}
		}
	}
	
	
	private fun createRequest() {
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(getString(R.string.default_web_client_id))
			.requestEmail()
			.build()
		
		googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
	}
	
	private fun signIn() {
		val signInIntent = googleSignInClient?.signInIntent
		startActivityForResult(signInIntent, RC_SIGN_IN)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		
		if (requestCode == RC_SIGN_IN) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			try {
				val account = task.getResult(ApiException::class.java)!!
				firebaseAuthWithGoogle(account.idToken!!)
			} catch (e: ApiException) {
			}
		}
	}
	
	private fun firebaseAuthWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnCompleteListener(requireActivity()) { task ->
				if (task.isSuccessful) {
					val user = auth.currentUser
					Navigation.findNavController(requireView())
						.navigate(R.id.action_mainFr_to_mailsFr)
				}
			}
	}
	
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
	
}
