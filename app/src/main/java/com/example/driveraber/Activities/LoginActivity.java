package com.example.driveraber.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driveraber.Activities.Main.MainActivity;
import com.example.driveraber.Activities.Register.RegisterActivity;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEditText, passwordEditText,forgetEmailEditText;
    private MaterialButton loginButton, registerButton,sentButton;
    private FirebaseUtil firebaseManager;

    private TextView forgetPassword;

    private ImageView close;

    private LinearLayout loginBackground;

    private Spinner spinnerLanguage;
    public static final String[] languages = {"Language","English","Tiếng Việt"};

    Dialog dialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseManager = new FirebaseUtil();
        progressDialog = new ProgressDialog(LoginActivity.this);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        forgetPassword = findViewById(R.id.forget_password_text);
        loginBackground = findViewById(R.id.login_background);
        spinnerLanguage = findViewById(R.id.language_spinner);


        //Forget password dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_forget);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        dialog.setCancelable(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        forgetEmailEditText = dialog.findViewById(R.id.email_forget_edit_text);
        sentButton = dialog.findViewById(R.id.sent_button);
        close = dialog.findViewById(R.id.close);

        //        Language Changer
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,languages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(arrayAdapter);
        spinnerLanguage.setSelection(0);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLang = parent.getItemAtPosition(position).toString();
                if(selectedLang.equals("English")){
                    setLocal(LoginActivity.this,"en");
                    finish();
                    startActivity(getIntent());

                } else if (selectedLang.equals("Tiếng Việt")){
                    setLocal(LoginActivity.this,"vi");
                    finish();
                    startActivity(getIntent());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                    firebaseManager.login(email, password, new FirebaseUtil.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            message = getString(R.string.login_successfully);
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

        //Forget password
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBackground.setAlpha(0.2F);
                dialog.show();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBackground.setAlpha(1.0F);
                dialog.hide();
            }
        });

        sentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = forgetEmailEditText.getText().toString();
                Log.d("Forget Password","User : " + emailAddress);
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                                    Log.d("Forget Password", "Email sent.");
                                }
                            }
                        });
            }
        });
    }

    //Set local for language option
    public void setLocal(Activity activity, String langCode){
        Locale locale = new Locale(langCode);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config,resources.getDisplayMetrics());


    }
}