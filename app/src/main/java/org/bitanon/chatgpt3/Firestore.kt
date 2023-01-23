package org.bitanon.chatgpt3

import android.util.Log
import androidx.annotation.Keep
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

private const val TAG = "Firestore"
class Firestore {

	private lateinit var db: FirebaseFirestore

	fun init(): Firestore {
		db = Firebase.firestore

		// try reading currently signed in user from Cloud Firestore
		if (userState.value == null) {
			readUser(Firebase.auth.currentUser)
		}

		return this
	}

	companion object {
		// Backing property to avoid state updates from other classes
		private val _userState: MutableStateFlow<User?> = MutableStateFlow(null)
		// The UI collects from this StateFlow to get its state updates
		var userState: StateFlow<User?> = _userState

		fun logOutUser() {
			_userState.value = null
		}

		fun isUserLoggedIn(): Boolean {
			return userState.value != null
		}

		fun getUserEmail(): String? {
			return userState.value?.email
		}

		fun getUserPaidWords(): Int {
			return userState.value?.purchasedWords ?: 0
		}

	}

	private fun updateUserTimeLastLogin(u: FirebaseUser?) {
		if (u != null) {
			db.collection("users").document(u.uid)
				.update("timeLastLogin", Date().time)
				.addOnSuccessListener { Log.d(TAG, "updateUserTimeLastLogin: success") }
				.addOnFailureListener { e -> Log.w(TAG, "updateUserTimeLastLogin: fail", e) }
		}
	}

	fun incrementUserPrompts() {
		if (_userState.value != null) {
			_userState.value!!.uid?.let {
				db.collection("users").document(it)
					.update("promptCount", FieldValue.increment(1))
					.addOnSuccessListener { Log.d(TAG, "incrementUserPromptCount: success") }
					.addOnFailureListener { e -> Log.w(TAG, "incrementUserPromptCount: fail", e) }
			}
		}
	}

	fun consumePurchasedWords(toConsume: Int) {
		// ignore negative or zero word consumption and anon prompts
		if (toConsume <= 0 || _userState.value == null)
			return

		var consumeLong: Long = toConsume.toLong()

		// don't allow saving of negative paid words to db
		val existingPaidWords = userState.value!!.purchasedWords
		if (existingPaidWords - toConsume <= 0)
			// consume exact remaining purchased words
			consumeLong = existingPaidWords.toLong()

		// ignore negative or zero consumed words
		if (consumeLong <= 0L)
			return

		Log.d(TAG, "consumingWords: $consumeLong")

		// update db user purchased words
		_userState.value?.uid?.let {
			db.collection("users").document(it)
				.update("purchasedWords", FieldValue.increment(- consumeLong))
				.addOnSuccessListener { Log.d(TAG, "incrementUserPromptCount: success") }
				.addOnFailureListener { e -> Log.w(TAG, "incrementUserPromptCount: fail", e) }
		}

		// read updated user info from database
		readUser(FirebaseAuth.getInstance().currentUser)

	}

	fun readUser(u: FirebaseUser?) {
		if (u == null)
			return

		val docRef = db.collection("users").document(u.uid)
		docRef.get()
			.addOnSuccessListener { document ->
				if (document != null) {
					Log.d(TAG, "readUser: DocumentSnapshot data: ${document.data}")
					_userState.value = document.toObject<User>()
				} else {
					Log.d(TAG, "readUser: No such document")
				}

				// write new logged in user to firestore if doesn't exist
				if (_userState.value == null)
					writeNewUser(u)
				else // if exists, then update last login time
					updateUserTimeLastLogin(u)

			}
			.addOnFailureListener { exception ->
				Log.d(TAG, "readUser: failed with ", exception)
			}
	}

	private fun writeNewUser(fbU: FirebaseUser?) {
		if (fbU == null) {
			Log.w(TAG, "createNewUser Fail: fbU null")
			return
		}

		// set user data as document named as user uid
		db.collection("users")
			.document(fbU.uid)
			.set(makeNewUser(fbU), SetOptions.merge())
			.addOnSuccessListener {
				Log.d(TAG, "createNewUser Result: documentSnapshot set with ID: ${fbU.uid}")
			}
			.addOnFailureListener { e ->
				Log.w(TAG, "createNewUser Result: Error adding document", e)
			}
	}

	private fun makeNewUser(u: FirebaseUser): User {
		return User(
			u.uid,
			u.displayName,
			u.hashCode(),
			u.email,
			u.photoUrl.toString(),
			Date().time,
			Date().time,
		)
	}
}

@Keep
data class User(
	val uid: String? = null,
	val displayName: String? = null,
	val hashCode: Int? = null,
	val email: String? = null,
	val photoUrl: String? = null,
	val timeLastLogin: Long? = null,
	val timeFirstCreated: Long? = null,
	val promptCount: Int = 0,
	val purchasedWords: Int = 0,
)

/*
val user = hashMapOf(
	"displayName" to u.displayName,
	"hashCode" to u.hashCode(),
	"email" to u.email,
	"isEmailVerified" to u.isEmailVerified,
	"phoneNumber" to u.phoneNumber,
	"photoUrl" to u.photoUrl,
	"dateLastLogin" to Date()
)*/
