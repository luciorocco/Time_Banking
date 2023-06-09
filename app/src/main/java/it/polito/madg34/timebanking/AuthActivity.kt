package it.polito.madg34.timebanking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class AuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    lateinit var loading : ProgressBar
    lateinit var signInButton: SignInButton
    lateinit var signinTV : TextView
    lateinit var TV8 : TextView
    lateinit var TV7 : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = Firebase.auth
        // Check if the user is already authenticated
        if (mAuth.currentUser == null) {

            // If not, build the authentication activity with the button to authenticate with Google
            setContentView(R.layout.auth_activity)

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
            /* Customize sign in Button*/
            signInButton.setSize(SignInButton.SIZE_STANDARD)
            val signin_tv = signInButton.getChildAt(0) as TextView
            signin_tv.setText("Sign in with Google")
            /* End of customizing */
            signInButton.setOnClickListener {
                signIn()
            }

        } else {
            // Else start the main activity
            setContentView(R.layout.auth_activity)
            signInButton = findViewById(R.id.sign_in_button)
            signinTV = findViewById(R.id.textView4)
            TV7 = findViewById(R.id.textView7)
            TV8 = findViewById(R.id.textView8)
            loading = findViewById(R.id.progress_bar)
            signinTV.visibility = View.GONE
            TV8.visibility = View.GONE
            TV7.visibility = View.GONE
            signInButton.visibility = View.GONE
            loading.visibility = View.VISIBLE
            startMainActivity()
        }

    }

    /*
    Function to start the MainActivity
     */
    private fun startMainActivity() {

        // Set the current user inside the companion object of the "FirestoreRepository" class
        FirestoreRepository.currentUser = mAuth.currentUser!!

        // Check if the current user already registered his account (second or following login)
        // or not (first login) by setting the flag
        FirestoreRepository().getUser().get().addOnCompleteListener {
            if (it.isSuccessful) {
                var flag = false
                if (it.result?.exists() == false){
                    flag = true
                }

                // Start the "MainActivity" with an extra flag:
                // true     -> it is needed the first registration (first login)
                // false    -> it is not needed the first registration, the user is already in the DB
                val intent = Intent(this, MainActivity::class.java).also { int ->
                    int.putExtra("INTENT_NEED_REGISTRATION_EXTRA", flag)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Firebase failure!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
    Function to start the Google Sign In
     */
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, start the MainActivity
                    startMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "GoogleLogin"
        private const val RC_SIGN_IN = 3
    }
}