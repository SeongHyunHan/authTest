package com.seong.butterknife;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";
    private EditText edtPhone;
    private Button btnAuth, btnSignOut;

    private String phoneNumber;
    private String phoneVerificationID;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtPhone = findViewById(R.id.edtPhone);
        btnAuth = findViewById(R.id.btnAuth);
        btnSignOut = findViewById(R.id.btnSignOut);

        fAuth = FirebaseAuth.getInstance();

        btnAuth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendCode(view);
                Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut(view);
            }
        });

    }


    public void sendCode(View view){
        phoneNumber = edtPhone.getText().toString();

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

    private void setUpVerificationCallbacks(){
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, "onCodeSent", Toast.LENGTH_SHORT).show();
                phoneVerificationID = s;
                resendToken = forceResendingToken;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = fAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        fAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(MainActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
                    }else{
                        if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                            // The verification code entered was invalid
                        }
                    }
                }
            }
        );

    }

    public void signOut(View view){
        fAuth.signOut();
    }
}
