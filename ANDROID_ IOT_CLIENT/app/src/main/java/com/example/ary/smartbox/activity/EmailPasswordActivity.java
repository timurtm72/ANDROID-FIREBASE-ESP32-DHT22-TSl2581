package com.example.ary.smartbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ary.smartbox.view.MainView;
import com.example.ary.smartbox.utils.Preferences;
import com.example.ary.smartbox.R;
import com.example.ary.smartbox.databinding.ActivityEmailpasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "EP";
    private ActivityEmailpasswordBinding mBinding;
    private boolean userIsPresent = false;
    FirebaseUser currentUser = null;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    Timer timer;
    public Preferences pref;

    //==================================================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityEmailpasswordBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setProgressBar(mBinding.progressBar);
        pref = new Preferences();
        // Buttons
        mBinding.emailSignInButton.setOnClickListener(this);
        mBinding.emailCreateAccountButton.setOnClickListener(this);
        mBinding.signOutButton.setOnClickListener(this);
        mBinding.verifyEmailButton.setOnClickListener(this);
        mBinding.reloadButton.setOnClickListener(this);
        //mBinding.entryButton.setOnClickListener(this);
        //Icon clicked
        mBinding.icon.setOnClickListener(this);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        pref = new Preferences();
    }

    static boolean switchCount = false;

    //==============================================================================================
    public void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (switchCount) {
                            if (userIsPresent) {
                                mBinding.icon.setVisibility(View.INVISIBLE);
                            }
                            switchCount = false;
                        } else {
                            mBinding.icon.setVisibility(View.VISIBLE);
                            switchCount = true;
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    //==================================================================================================
    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //==================================================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    //==================================================================================================
    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        startTimer();
    }

    // [END on_start_check_user]
//==================================================================================================
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressBar();
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    //==================================================================================================
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressBar();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            // [START_EXCLUDE]
                            //checkForMultiFactorFailure(task.getException());
                            // [END_EXCLUDE]
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mBinding.status.setText(R.string.auth_failed);
                        }
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    //==================================================================================================
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    //==================================================================================================
    private void sendEmailVerification() {
        // Disable button
        mBinding.verifyEmailButton.setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        mBinding.verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this,
                                    getString(R.string.verification_email_send) + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailPasswordActivity.this,
                                    R.string.failed_to_send_verification,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    //==================================================================================================
    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(EmailPasswordActivity.this,
                            R.string.reload_successful,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(EmailPasswordActivity.this,
                            R.string.failed_to_reload_user,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //==================================================================================================
    private boolean validateForm() {
        boolean valid = true;

        String email = mBinding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError(getString(R.string.requerid));
            valid = false;
        } else {
            mBinding.fieldEmail.setError(null);
        }

        String password = mBinding.fieldPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mBinding.fieldPassword.setError(getString(R.string.requerid));
            valid = false;
        } else {
            mBinding.fieldPassword.setError(null);
        }

        return valid;
    }

    //==================================================================================================
    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.emailPasswordButtons.setVisibility(View.GONE);
            mBinding.emailPasswordFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);
            userIsPresent = true;
            if (user.isEmailVerified()) {
                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);
            userIsPresent = false;
            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
        }
    }

    //    private void checkForMultiFactorFailure(Exception e) {
//        // Multi-factor authentication with SMS is currently only available for
//        // Google Cloud Identity Platform projects. For more information:
//        // https://cloud.google.com/identity-platform/docs/android/mfa
//        if (e instanceof FirebaseAuthMultiFactorException) {
//            Log.w(TAG, "multiFactorFailure", e);
//            Intent intent = new Intent();
//            MultiFactorResolver resolver = ((FirebaseAuthMultiFactorException) e).getResolver();
//            intent.putExtra("EXTRA_MFA_RESOLVER", resolver);
//            setResult(MultiFactorActivity.RESULT_NEEDS_MFA_SIGN_IN, intent);
//            finish();
//        }
//    }
//==================================================================================================
    //==============================================================================================
    String userId = null;
    private void openProgressActivity() {
        if (userIsPresent == true) {
            currentUser = mAuth.getCurrentUser();
            stopTimer();
            Intent intent = new Intent(this, MainView.class);
            userId = pref.loadId(this);
            if(userId.equals("DEFAULT")){
                pref.saveId(currentUser.getUid(),this);
            }
            if (userId != null) {
                if (!userId.equals(currentUser.getUid())) {
                    pref.saveId(currentUser.getUid(),this);
                }
            }
            intent.putExtra("key", currentUser.getUid()); //Optional parameters
            this.startActivity(intent);
        } else {
            Toast.makeText(EmailPasswordActivity.this,
                    R.string.failed_to_present_user,
                    Toast.LENGTH_SHORT).show();
        }
    }

    //==================================================================================================
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            createAccount(mBinding.fieldEmail.getText().toString(), mBinding.fieldPassword.getText().toString());
        } else if (i == R.id.emailSignInButton) {
            signIn(mBinding.fieldEmail.getText().toString(), mBinding.fieldPassword.getText().toString());
        } else if (i == R.id.signOutButton) {
            signOut();
        } else if (i == R.id.verifyEmailButton) {
            sendEmailVerification();
        } else if (i == R.id.reloadButton) {
            reload();
        } else if (i == R.id.icon) {
            openProgressActivity();
        }
    }
}
//==================================================================================================