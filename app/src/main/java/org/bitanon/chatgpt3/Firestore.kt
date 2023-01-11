package org.bitanon.chatgpt3

import android.util.Log
import androidx.annotation.Keep
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

private const val TAG = "Firestore"
class Firestore {

	private val db = Firebase.firestore
	private var user: User? = null

	private fun updateUserTimeLastLogin(u: FirebaseUser?) {
		if (u != null) {
			db.collection("users").document(u.uid)
				.update("timeLastLogin", Date().time)
				.addOnSuccessListener { Log.d(TAG, "updateUserTimeLastLogin: success") }
				.addOnFailureListener { e -> Log.w(TAG, "updateUserTimeLastLogin: fail", e) }
		}
	}

	fun readUser(u: FirebaseUser?) {
		if (u == null)
			return

		val docRef = db.collection("users").document(u.uid)
		docRef.get()
			.addOnSuccessListener { document ->
				var user: User? = null
				if (document != null) {
					Log.d(TAG, "readUser: DocumentSnapshot data: ${document.data}")
					user = document.toObject<User>()
				} else {
					Log.d(TAG, "readUser: No such document")
				}

				// write new logged in user to firestore if doesn't exist
				if (user == null)
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
			u.displayName,
			u.hashCode(),
			u.email,
			u.isEmailVerified,
			u.phoneNumber,
			u.photoUrl.toString(),
			Date().time,
			Date().time,
			0,
		)
	}
}

@Keep
data class User(
	val displayName: String? = null,
	val hashCode: Int? = null,
	val email: String? = null,
	val isEmailVerified: Boolean? = null,
	val phoneNumber: String? = null,
	val photoUrl: String? = null,
	val timeLastLogin: Long? = null,
	val timeFirstCreated: Long? = null,
	val numPrompts: Int? = null,
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
