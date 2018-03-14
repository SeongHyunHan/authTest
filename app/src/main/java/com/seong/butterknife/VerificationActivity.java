package com.seong.butterknife;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Created by hans on 2018-03-07.
 */

public class VerificationActivity extends AppCompatActivity {
    private static final String TAG = "Verification Activity";

    private Button btnVerify, btnResend, btnSignOut;
    private EditText edtCode;

    private FirebaseAuth fAuth;
    private String phoneVerificationID;
    private String phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();
        phoneVerificationID = intent.getStringExtra("phoneVerificationID");
        phoneNumber = intent.getStringExtra("phoneNumber");
        Toast.makeText(this, phoneVerificationID + ", " + phoneNumber, Toast.LENGTH_SHORT).show();

        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);
        edtCode = findViewById(R.id.edtCode);
        btnSignOut = findViewById(R.id.btnSignOuts);

        btnVerify.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                verifyCode(view);
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendCode(view);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut(view);
            }
        });

    }

    private void setUpVerificationCallbacks(){
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(VerificationActivity.this, "Success", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    Log.d(TAG, "Invalid Credentials");
                }else if(e instanceof FirebaseTooManyRequestsException) {
                    Log.d(TAG, "SMS Quota exceeded");
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                phoneVerificationID = s;
                resendToken = forceResendingToken;
            }
        };
    }



    public void verifyCode(View view){
        String code = "123456";
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationID, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    Toast.makeText(VerificationActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
                                }else{
                                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                        // The verification code entered was invalid
                                    }
                                }
                            }
                        }
                );

    }

    public void resendCode(View view){
        setUpVerificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60
                ,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks
        );
    }

    public void signOut(View view){
        fAuth.signOut();
    }
}
