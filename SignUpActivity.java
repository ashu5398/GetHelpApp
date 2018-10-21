package com.commercial.askitloud.japhhi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.UserProfileChangeRequest.Builder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    Button LoginButton,GoButton;
    EditText UserNameEt,EmailEt,PasswordEt;
    String username,email,password;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        UserNameEt = findViewById(R.id.username);
        EmailEt  =findViewById(R.id.email);
        PasswordEt = findViewById(R.id.password);

        LoginButton = findViewById(R.id.loginbtn);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUpActivity.this,LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_up, R.anim.stay);
            }
        });
        GoButton = findViewById(R.id.signin);
        GoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = UserNameEt.getText().toString();
                email = EmailEt.getText().toString();
                password = PasswordEt.getText().toString();
                if(email.matches("") || password.matches("") || username.matches("")){
                    Toast.makeText(SignUpActivity.this, "Fill all the info.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ProgressDialog progressDialog = ProgressDialog.show(SignUpActivity.this,"","Loading...",true);
                Log.e("tag","i am here");
                mAuth = FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Log.e("tag","succesful");
                                    user = mAuth.getCurrentUser();
                                    user.updateProfile(new Builder().setDisplayName(username).build());
                                    updateUI(user);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
                }

        });

    }

    void updateUI(FirebaseUser user){
        if(user != null){
            Intent i = new Intent(SignUpActivity.this,MapsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}
