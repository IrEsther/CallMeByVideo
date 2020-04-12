package com.estherkom.videochatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {

    private CountryCodePicker countryCodePicker;
    private EditText phoneText;
    private EditText codeText;
    private Button continueBTN;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
private FirebaseAuth mAuth;
private String mVerificationId;
private PhoneAuthProvider.ForceResendingToken mResebdToken;
private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        loadingBar =  new ProgressDialog(this);
        setContentView(R.layout.activity_registration);
        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueBTN = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        countryCodePicker = (CountryCodePicker)findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneText);

        continueBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueBTN.getText().equals("Submit") || checker.equals("Code Sent")){
String verificationCode = codeText.getText().toString();
if (verificationCode.equals("")){
    Toast.makeText(RegistrationActivity.this,"Please write verification code",Toast.LENGTH_SHORT).show();

}else {
    loadingBar.setTitle("Code Verification");
    loadingBar.setMessage("Please wait, we are verifying your code");
    loadingBar.setCanceledOnTouchOutside(false);
    loadingBar.show();
    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
    signInWithPhoneAuthCredential(phoneAuthCredential);
}
                }else {
phoneNumber = countryCodePicker.getFullNumberWithPlus();
if (!phoneNumber.equals("")){
loadingBar.setTitle("Phone Number Verification");
loadingBar.setMessage("Please wait, we are verifying your phone number");
loadingBar.setCanceledOnTouchOutside(false);
loadingBar.show();
    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, RegistrationActivity.this, mCallbacks);

}else {
    Toast.makeText(RegistrationActivity.this,"Oops...Please write valid phone number...",Toast.LENGTH_SHORT).show();
}
                }
            }
        });


mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    @Override
    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
signInWithPhoneAuthCredential(phoneAuthCredential);
    }

    @Override
    public void onVerificationFailed(FirebaseException e) {
    Toast.makeText(RegistrationActivity.this,"Invalid phone number",Toast.LENGTH_SHORT).show();
   loadingBar.dismiss();
    relativeLayout.setVisibility(View.VISIBLE);
        continueBTN.setText("Continue");
        codeText.setVisibility(View.GONE);
    }

    //dft sim card

    @Override
    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        super.onCodeSent(s, forceResendingToken);

        mVerificationId = s;
        mResebdToken = forceResendingToken;
        relativeLayout.setVisibility(View.GONE);
checker = "Code Sent";
continueBTN.setText("Submit");
codeText.setVisibility(View.VISIBLE);
loadingBar.dismiss();
Toast.makeText(RegistrationActivity.this,"Code has been sent, please check", Toast.LENGTH_SHORT).show();
    }
};
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            Intent intent = new Intent(RegistrationActivity.this, ContactsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                           Toast.makeText(RegistrationActivity.this,"Wow! Congratulations, you are logged in!",Toast.LENGTH_SHORT).show();
                           sendUserToMainActivity();
                        } else {
                            loadingBar.dismiss();
                            String e = task.getException().toString();
                          Toast.makeText(RegistrationActivity.this,"Error: "+e,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void sendUserToMainActivity(){
        Intent intent = new Intent(RegistrationActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}
