package com.authtest.seong.authtest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuthActivity";

    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                /*
                * This Callback will be invoked in two situations:
                * 1. Instant verification. In some cases the phone number can be instantly verified
                *       without needing to send or enter a verification code.
                * 2. Auto-retrieval. On some devices Google Play services can automatically detect
                *       the incoming verification SMS and perform verification without user action
                * */
                Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential);

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made
                // for instance if the phone number format is not valid
                Log.w(TAG, "onVerificationFailed: ", e);

                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    //Invalid request
                }else if(e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we now need
                // to ask the user to enter the code and then construct a credential by
                // combining the code with a verification ID

                Log.d(TAG, "onCodeSent: " + s);

                // Save verification ID and resending token so we can use them later
                mVerificationId = s;
                mResendToken = forceResendingToken;
            }
        };
    }

    private void startPhoneNumberVerification(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        //Phone number to Verify
                60,              // Timeout Duration
                TimeUnit.SECONDS,    // Unit of Timeout
                this,               // Activity (for callback binding)
                mCallbacks          // OnVerificationStateChangedCallbakcs
        );
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential: success");

                        FirebaseUser user = task.getResult().getUser();
                    }else{
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential: failure", task.getException());

                        if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                            // The verification code entered was invalid
                        }
                    }
                }
            }
        );
    }
}
