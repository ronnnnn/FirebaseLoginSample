package com.ronnnnn.firebaseloginsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * This Activity shows current login user profile.
 * Users can sign out if they push the button.
 */
public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    static final String KEY_USER_BUNDLE = "key_user_bundle";
    static final String KEY_USER_UID = "key_user_uid";
    static final String KEY_USER_EMAIL = "key_user_email";
    static final String KEY_USER_PROVIDER = "key_user_provider";

    public static Intent createIntent(Context context, Bundle userInfoBundle) {
        Intent intent = new Intent(context, ResultActivity.class);
        return intent.putExtra(KEY_USER_BUNDLE, userInfoBundle);
    }

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        firebaseAuth  = FirebaseAuth.getInstance();

        initializeViews();
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle userInfoBundle = getIntent().getBundleExtra(KEY_USER_BUNDLE);
        String userUid = userInfoBundle.getString(KEY_USER_UID);
        String userEmail = userInfoBundle.getString(KEY_USER_EMAIL);
        String userProvider = userInfoBundle.getString(KEY_USER_PROVIDER);

        ((TextView) findViewById(R.id.user_uid_text_view)).setText(userUid);
        ((TextView) findViewById(R.id.user_email_text_view)).setText(userEmail);
        ((TextView) findViewById(R.id.user_provider_text_view)).setText(userProvider);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            finish();
        }
    }
}
