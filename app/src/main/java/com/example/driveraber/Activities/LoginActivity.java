package com.example.driveraber.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.driveraber.Activities.Main.MainActivity;
import com.example.driveraber.Activities.Register.RegisterActivity;
import com.example.driveraber.FirebaseManager;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private MaterialButton loginButton, registerButton;
    private FirebaseManager firebaseManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseManager = new FirebaseManager();
        progressDialog = new ProgressDialog(LoginActivity.this);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        Intent intent = getIntent();
        if (intent.hasExtra("email") && intent.hasExtra("password")) {
            String email = intent.getStringExtra("email");
            String password = intent.getStringExtra("password");

            emailEditText.setText(email);
            passwordEditText.setText(password);
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showLoadingDialog(progressDialog);
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()){
                    firebaseManager.login(email, password, new FirebaseManager.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            AndroidUtil.showToast(LoginActivity.this, message);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onTaskFailure(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            AndroidUtil.showToast(LoginActivity.this, message);
                        }
                    });
                } else {
                    AndroidUtil.hideLoadingDialog(progressDialog);
                    AndroidUtil.showToast(LoginActivity.this, "Email and Password can not be empty");
                }
            }
        });
    }
}