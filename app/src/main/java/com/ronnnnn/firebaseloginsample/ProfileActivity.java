package com.ronnnnn.firebaseloginsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

/**
 * This Activity shows current login user profile.
 * Users can sign out if they push the button.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    static final String KEY_USER_BUNDLE = "key_user_bundle";
    static final String KEY_USER_UID = "key_user_uid";
    static final String KEY_USER_EMAIL = "key_user_email";
    static final String KEY_USER_PROVIDER = "key_user_provider";

    public static Intent createIntent(Context context, Bundle userInfoBundle) {
        Intent intent = new Intent(context, ProfileActivity.class);
        return intent.putExtra(KEY_USER_BUNDLE, userInfoBundle);
    }

    enum Provider {
        GOOGLE(GoogleAuthProvider.PROVIDER_ID),
        FACEBOOK(FacebookAuthProvider.PROVIDER_ID),
        TWITTER(TwitterAuthProvider.PROVIDER_ID),
        GITHUB(GithubAuthProvider.PROVIDER_ID),
        EMAIL(EmailAuthProvider.PROVIDER_ID);

        private final String providerId;

        Provider(String providerId) {
            this.providerId = providerId;
        }

        public String getProviderId() {
            return providerId;
        }
    }

    private FirebaseAuth firebaseAuth;
    private String userProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews();
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle userInfoBundle = getIntent().getBundleExtra(KEY_USER_BUNDLE);
        String userUid = userInfoBundle.getString(KEY_USER_UID);
        String userEmail = userInfoBundle.getString(KEY_USER_EMAIL);
        userProvider = userInfoBundle.getString(KEY_USER_PROVIDER);

        ((TextView) findViewById(R.id.user_uid_text_view)).setText(userUid);
        final TextView userEmailTextView = (TextView) findViewById(R.id.user_email_text_view);
        userEmailTextView.setText(userEmail);
        ((TextView) findViewById(R.id.user_provider_text_view)).setText(userProvider);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        if (userProvider.equals(Provider.TWITTER.getProviderId())) {
            TwitterSession session = Twitter.getSessionManager().getActiveSession();
            TwitterAuthClient authClient = new TwitterAuthClient();
            authClient.requestEmail(session, new Callback<String>() {
                @Override
                public void success(Result<String> result) {
                    userEmailTextView.setText(result.data);
                }

                @Override
                public void failure(TwitterException exception) {
                    DialogManager.createDialog(ProfileActivity.this, exception.getMessage()).show();
                }
            });
        }

    }

    @Override
    public void onClick(View view) {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            if (userProvider.equals(Provider.FACEBOOK.getProviderId())) {
                setResult(RESULT_OK);
                finish();
            } else {
                finish();
            }
        }
    }
}
