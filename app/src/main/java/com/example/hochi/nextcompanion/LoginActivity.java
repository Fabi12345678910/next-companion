package com.example.hochi.nextcompanion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;


/**
 * A login screen that offers login via phone number/pin.
 */
public class LoginActivity extends AppCompatActivity implements AsyncTaskCallbacks<String> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RequestHandler mAuthTask = null;

    // UI references.
    private TextView mPhoneView;
    private EditText mPinView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mPhoneView = findViewById(R.id.phone);

        mPinView = findViewById(R.id.pin);
        mPinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mPhoneSignInButton = findViewById(R.id.phone_sign_in_button);
        mPhoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid phone number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPinView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String pin = mPinView.getText().toString();
        String[] credentials = {
                "apikey=", getString(R.string.apikey),
                "mobile=", mPhoneView.getText().toString(),
                "pin=", mPinView.getText().toString()
        };

        boolean cancel = false;
        View focusView = null;

        // Check for a valid pin, if the user entered one.
        if (TextUtils.isEmpty(pin)) {
            mPinView.setError(getString(R.string.error_field_required));
            focusView = mPinView;
            cancel = true;
        }

        // Check for a valid phone address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RequestHandler(this, "POST",
                    "api/login.json", credentials);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onTaskComplete(String response) {
        //Callback called when RequestHandler finished request
        if (!response.isEmpty()) {
            try {
                JSONObject jObject = new JSONObject(response);
                JSONObject userObject = jObject.getJSONObject("user");
                String loginkey = userObject.getString("loginkey");
                // Check for username
                String username = null;
                if(userObject.has("screen_name"))
                {
                    username = userObject.getString("screen_name");
                }
                // Check for phone number
                String phone = null;
                if(userObject.has("mobile"))
                {
                    phone = "+" + userObject.getString("mobile");
                }
                SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("loginKey", loginkey);
                // Store username in sPrefs to be able to display it in the MainActivity
                editor.putString("username", username);
                // Store phone number in sPrefs to be able to display it in the MainActivity
                editor.putString("phone", phone);
                editor.apply();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        } else {
            mPinView.setError(getString(R.string.error_incorrect_pin));
            mPinView.requestFocus();
        }
    }
}

