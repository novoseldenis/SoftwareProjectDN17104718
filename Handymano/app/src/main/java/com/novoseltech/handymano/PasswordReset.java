package com.novoseltech.handymano;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity {

    FirebaseAuth mAuth;

    Button btn_resetPassword;
    EditText et_Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        btn_resetPassword = findViewById(R.id.btn_resetPassword);
        et_Email = findViewById(R.id.emailPasswordReset);

        mAuth = FirebaseAuth.getInstance();


        btn_resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = et_Email.getText().toString().trim();

                if(emailAddress.isEmpty()){
                    et_Email.setError("Email address is required");
                    et_Email.requestFocus();
                    return;
                }else if(!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
                    et_Email.setError("Please enter valid email address");
                    et_Email.requestFocus();
                    return;
                }else{
                    mAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), "Password reset email sent",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });


    }
}