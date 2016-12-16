package com.ronnnnn.firebaseloginsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

/**
 * This Activity shows providers users can login or sign up and handle authentication
 * when users push each buttons with using firebase.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, FirebaseAuth.AuthStateListener {

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 100;
    private static final int REQUEST_CODE_EMAIL_SIGN_IN = 101;
    private static final int REQUEST_CODE_PROFILE = 102;

    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;
    private CallbackManager callbackManager;
    private TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get firebase instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_client_id))
                        .requestEmail()
                        .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        FacebookSdk.sdkInitialize(getApplicationContext());

        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key),
                getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);

        initializeViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInWithGoogleResult(result);
        } else if (requestCode == REQUEST_CODE_EMAIL_SIGN_IN) {
            firebaseAuthWithEmailAndPassword(data);
        } else if (requestCode == REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            logoutFacebook();
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize google sign in button
        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(this);

        // initialize facebook login button
        callbackManager = CallbackManager.Factory.create();
        FacebookCallback<LoginResult> facebookLoginCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                if (error != null) {
                    DialogManager.createDialog(MainActivity.this, error).show();
                }
            }
        };
        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(callbackManager, facebookLoginCallback);

        // initialize twitter login button
        Callback<TwitterSession> twitterLoginCallback = new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // with your app's user model
                firebaseAuthWithTwitter(session);
            }

            @Override
            public void failure(TwitterException exception) {
                if (exception != null) {
                    DialogManager.createDialog(MainActivity.this, exception).show();
                }
            }
        };
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(twitterLoginCallback);

        findViewById(R.id.email_login_button).setOnClickListener(this);
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void handleSignInWithGoogleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // succeed sign in
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            // failed sign in
            DialogManager.createDialog(MainActivity.this,
                    CommonStatusCodes.getStatusCodeString(result.getStatus().getStatusCode()))
                    .show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            DialogManager.createDialog(MainActivity.this, task.getException())
                                    .show();
                        }
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            DialogManager.createDialog(MainActivity.this, task.getException())
                                    .show();
                        }
                    }
                });
    }

    private void firebaseAuthWithTwitter(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
                session.getAuthToken().secret);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            DialogManager.createDialog(MainActivity.this, task.getException())
                                    .show();
                        }
                    }
                });
    }

    private void firebaseAuthWithEmailAndPassword(Intent data) {
        if (data.getBooleanExtra(FormActivity.KEY_HAS_ACCOUNT, true)) {
            firebaseAuth.signInWithEmailAndPassword(data.getStringExtra(FormActivity.KEY_EMAIL),
                    data.getStringExtra(FormActivity.KEY_PASSWORD))
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                DialogManager.createDialog(MainActivity.this, task.getException())
                                        .show();
                            }
                        }
                    });
        } else {
            firebaseAuth.createUserWithEmailAndPassword(data.getStringExtra(FormActivity.KEY_EMAIL),
                    data.getStringExtra(FormActivity.KEY_PASSWORD))
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                DialogManager.createDialog(MainActivity.this, task.getException())
                                        .show();
                            }
                        }
                    });
        }
    }

    private void logoutFacebook() {
        LoginManager.getInstance().logOut();
    }

    /**
     * check auth state
     * this method is called when auth state is changed, the listener is registered and user's token is changed
     *
     * @param firebaseAuth
     */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User has already signed in
            Bundle userInfoBundle = new Bundle();
            userInfoBundle.putString(ProfileActivity.KEY_USER_UID, user.getUid());
            userInfoBundle.putString(ProfileActivity.KEY_USER_EMAIL, user.getEmail());
            if (user.getProviders() != null && !user.getProviders().isEmpty()) {
                userInfoBundle.putString(ProfileActivity.KEY_USER_PROVIDER, user.getProviders().get(0));
            } else {
                userInfoBundle.putString(ProfileActivity.KEY_USER_PROVIDER, getString(R.string.unknown_user_provider));
            }
            startActivityForResult(ProfileActivity.createIntent(MainActivity.this, userInfoBundle), REQUEST_CODE_PROFILE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_sign_in_button:
                signInWithGoogle();
                break;

            case R.id.email_login_button:
                startActivityForResult(FormActivity.createIntent(MainActivity.this), REQUEST_CODE_EMAIL_SIGN_IN);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DialogManager.createDialog(MainActivity.this, connectionResult.getErrorMessage()).show();
    }
}
