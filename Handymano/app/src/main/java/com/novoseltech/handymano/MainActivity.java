package com.novoseltech.handymano;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class MainActivity.java
 **/

public class MainActivity extends AppCompatActivity {

    //Layout components
    private EditText etEmail;
    private EditText etPassword;
    private Button btn_passwordReset;

    //Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    //Variables
    private String accountType = "";
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.loginEmailInput);
        etPassword = findViewById(R.id.loginPasswordInput);
        mAuth = FirebaseAuth.getInstance();
        btn_passwordReset = findViewById(R.id.forgotPassword);

        btn_passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PasswordReset.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null){
            Map<String, Object> userData = new HashMap<>();

            fStore = FirebaseFirestore.getInstance();
            fStore.collection("user").document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                accountType = documentSnapshot.getString("accountType");
                                email = documentSnapshot.getString("email");
                                userData.put("accountType", documentSnapshot.getString("accountType"));
                                userData.put("username", documentSnapshot.getString("username"));
                                userData.put("email", documentSnapshot.getString("email"));
                                userData.put("phoneNo", documentSnapshot.get("phoneNo"));

                                if(accountType.equals("Professional")){
                                    userData.put("category", documentSnapshot.get("category"));
                                    finish();
                                    Intent intent = new Intent(MainActivity.this, HomeActivityProfessional.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("USER_MAP", (Serializable) userData);
                                    intent.putExtra("APP_USER_USERNAME", (String)userData.get("username"));
                                    startActivity(intent);
                                }else if(accountType.equals("Standard")){
                                    finish();
                                    Intent intent = new Intent(MainActivity.this, HomeActivityStandard.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("username", (String)userData.get("username"));
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Automatic login failed. Please try manually", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Automatic login failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void ClickLogin(View view){
        LogIn();
    }

    public void ClickRegister(View view){
        Intent intent = new Intent(MainActivity.this, RegistrationChoiceActivity.class);
        startActivity(intent);
    }

    //Firebase login method
    private void LogIn(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty()){
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please enter valid email address");
            etEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            etPassword.setError("Minimum password length is 6 characters");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Sign in successful, opening new activity
                    Map<String, Object> userData = new HashMap<>();
                    fStore = FirebaseFirestore.getInstance();
                    fStore.collection("user").document(mAuth.getCurrentUser().getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();

                                        accountType = documentSnapshot.getString("accountType");
                                        userData.put("accountType", documentSnapshot.getString("accountType"));
                                        userData.put("username", documentSnapshot.getString("username"));
                                        userData.put("email", documentSnapshot.getString("email"));
                                        userData.put("phoneNo", documentSnapshot.get("phoneNo"));
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Automatic login failed", Toast.LENGTH_SHORT).show();
                                }
                            });

                    //I am using Handler throughout my application
                    //to delay certain actions for some period of time
                    //idea is from https://stackoverflow.com/questions/3072173/how-to-call-a-method-after-a-delay-in-android
                    //In this particular case I am waiting 500 ms so that data about user's account type is retrieved and then I can decide which intent to start
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(accountType.equals("Professional")){
                                finish();
                                Intent intent = new Intent(MainActivity.this, HomeActivityProfessional.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else if(accountType.equals("Standard")){
                                finish();
                                Intent intent = new Intent(MainActivity.this, HomeActivityStandard.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getApplicationContext(), "Account type not recognized. Please try to log in again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 500);


                }else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Close application");
        //Set message
        builder.setMessage("Do you want to close the application ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finishAndRemoveTask();

            }
        });

        //No button
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Dismiss dialog
                dialogInterface.dismiss();
            }
        });
        //Show dialog
        builder.show();
    }
}