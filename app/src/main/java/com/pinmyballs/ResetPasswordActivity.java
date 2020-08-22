package com.pinmyballs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ResetPasswordActivity extends AppCompatActivity {
    public static final String INTENT_RESET_EMAIL = "com.pinmyballs.ResetPasswordActivity.INTENT_RESET_EMAIL";

    // UI references.
    private EditText mResetEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Intent intent = getIntent();
        String emailFromLogin = intent.getStringExtra(INTENT_RESET_EMAIL);

        mResetEmailView = findViewById(R.id.reset_email);
        if (!emailFromLogin.equals("")) {
            mResetEmailView.setText(emailFromLogin);
        }

        Button mResetPasswordButton = findViewById(R.id.password_reset_button);
        mResetPasswordButton.setOnClickListener(this::resetPassword);


    }

    public void resetPassword(View view) {
        if (TextUtils.isEmpty(mResetEmailView.getText())) {
            mResetEmailView.setError("Email is required!");
        } else {
            ParseUser.requestPasswordResetInBackground(mResetEmailView.getText().toString(), new RequestPasswordResetCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // An email was successfully sent with reset instructions.
                        alertDisplayer("Email envoyé",
                                "Un email a été envoyé à " + mResetEmailView.getText().toString() + ".\nCliquez sur le lien dans l'email pour définir votre mot de passe.");

                    } else {
                        // Something went wrong. Look at the ParseException to see what's up.
                        Toast.makeText(ResetPasswordActivity.this, "Something went wrong. Email not  found.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        // don't forget to change the line below with the names of your Activities
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(LoginActivity.INTENT_FROM_RESET, mResetEmailView.getText().toString());
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }


}
