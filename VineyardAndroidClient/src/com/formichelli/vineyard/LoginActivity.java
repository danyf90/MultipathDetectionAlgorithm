package com.formichelli.vineyard;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.VineyardServer;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	public static final String USERID = "id";
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mServerUrl;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mServerUrlView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(sp.getString(
				getString(R.string.preference_user_email), null));

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setText(sp.getString(
				getString(R.string.preference_user_password), null));

		mServerUrlView = (EditText) findViewById(R.id.server_url);
		mServerUrlView.setText(sp.getString(
				getString(R.string.preference_server_url), null));
		mServerUrlView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {

						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mAuthTask != null)
			mAuthTask.cancel(true);
		else
			finish();
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mServerUrl = mServerUrlView.getText().toString();

		if (!mServerUrl.startsWith("http://"))
			mServerUrl = "http://" + mServerUrl;

		sp.edit()
				.putString(getString(R.string.preference_user_email), mEmail)
				.putString(getString(R.string.preference_user_password),
						mPassword)
				.putString(getString(R.string.preference_server_url),
						mServerUrl).commit();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There is an error: don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(mEmail, mPassword, mServerUrl);
			mAuthTask.execute();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncHttpRequest {

		public UserLoginTask(String email, String password, String serverUrl) {
			super(serverUrl + VineyardServer.LOGIN_API,
					AsyncHttpRequest.Type.POST);

			addParam(new BasicNameValuePair("email", mEmail));

			try {
				String passwordHash = getHexString(MessageDigest.getInstance(
						"MD5").digest(mPassword.getBytes()));
				addParam(new BasicNameValuePair("password", passwordHash));
			} catch (NoSuchAlgorithmException e) {
				cancel(true);
			}
		}

		private String getHexString(byte[] digest) {
			return String.format("%032x", new BigInteger(1, digest));
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			mAuthTask = null;
			showProgress(false);

			if (response.first == HttpStatus.SC_ACCEPTED) {

				try {
					sp.edit()
							.putInt(USERID,
									new JSONObject(response.second)
											.getInt(USERID)).commit();
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
					setError();
				}

				startActivity(new Intent(LoginActivity.this,
						VineyardMainActivity.class));
				finish();

			} else
				setError();
		}

		private void setError() {
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
