package com.ferpa.machinestock.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentLogInBinding
import com.ferpa.machinestock.model.MachineStockUser
import com.ferpa.machinestock.ui.viewmodel.LoginViewModel
import com.ferpa.machinestock.utilities.Const.REQUEST_CODE_GOOGLE_LOG_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LogInFragment : Fragment(R.layout.fragment_log_in) {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        singIn()

    }

    private fun singIn() {
        val singInIntent = googleSignInClient.signInIntent
        startActivityForResult(singInIntent, REQUEST_CODE_GOOGLE_LOG_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GOOGLE_LOG_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "Succes ${account.id}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(ContentValues.TAG, e.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCredential: succes")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(ContentValues.TAG, "singInFail")
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            viewModel.createUser(
                MachineStockUser(
                    user.uid,
                    name = user.displayName,
                    profilePhotoUrl = user.photoUrl.toString(),
                    email = user.email,
                    phoneNumber = user.phoneNumber
                )
            )
        }
        val action = LogInFragmentDirections.actionLogInFragmentToMenuFragment()
        findNavController().navigate(action)

    }

    companion object {
        const val TAG = "LogInFragment"
    }

}