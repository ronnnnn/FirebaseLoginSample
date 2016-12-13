package com.ronnnnn.firebaseloginsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class FormActivity extends AppCompatActivity implements View.OnClickListener {

    static final String KEY_EMAIL = "key_email";
    static final String KEY_PASSWORD = "key_password";
    static final String KEY_HAS_ACCOUNT = "key_has_account";

    public static Intent createIntent(Context context) {
        return new Intent(context, FormActivity.class);
    }

    private EditText emailFormEditText;
    private EditText passwordFormEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        initializeViews();
    }

    public void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView messageTextView = (TextView) findViewById(R.id.message_text_view);
        messageTextView.setText(getString(R.string.sign_up_with_email_and_password_message));

        emailFormEditText = (EditText) findViewById(R.id.email_form_edit_text);
        passwordFormEditText = (EditText) findViewById(R.id.password_form_edit_text);

        findViewById(R.id.sign_up_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!TextUtils.isEmpty(emailFormEditText.getText()) &&
                !TextUtils.isEmpty(passwordFormEditText.getText())) {
            Intent data = new Intent();
            data.putExtra(KEY_EMAIL, emailFormEditText.getText().toString());
            data.putExtra(KEY_PASSWORD, passwordFormEditText.getText().toString());
            if (view.getId() == R.id.sign_up_button) {
                data.putExtra(KEY_HAS_ACCOUNT, false);
            } else {
                data.putExtra(KEY_HAS_ACCOUNT, true);
            }
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
