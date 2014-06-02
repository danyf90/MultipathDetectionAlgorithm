package com.formichelli.vineyard;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.formichelli.vineyard.entities.Worker;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.Util;
import com.formichelli.vineyard.utilities.VineyardServer;

/**
 * Activity which manages application settings
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	public static final String SERVER_URL = null;

	private enum Validity {
		VALID
	}

	PreferenceScreen ps;
	String oldValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		ps = getPreferenceScreen();

		initPreferences();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);

		menu.findItem(R.id.action_settings_restore).setOnMenuItemClickListener(
				restoreSettings);
		menu.findItem(R.id.action_settings_change_password)
				.setOnMenuItemClickListener(changePassword);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings_restore:
		case R.id.action_settings_change_password:
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener
		ps.getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener
		ps.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
				this);
	}

	/**
	 * Check if the new settings are allowed and restore the previous ones if
	 * needed
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences Object
	 * @param key
	 *            key of the modified setting
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (oldValue != null) { // avoid to reevaluate settings after the
								// restore
			int msgId = 0;

			Validity v = isValid(sharedPreferences);

			switch (v) {
			case VALID:
				setPreferenceSummary(findPreference(key));
				return;

			}

			Toast.makeText(this, getString(msgId), Toast.LENGTH_SHORT).show();
			restoreValue(sharedPreferences, key);
		}
	}

	private void restoreValue(SharedPreferences sharedPreferences, String key) {
		if (sharedPreferences == null || key == null)
			return;

		Preference p = ps.findPreference(key);
		if (p instanceof EditTextPreference)
			((EditTextPreference) p).setText(oldValue);

		Editor e = sharedPreferences.edit();
		e.putString(key, oldValue);
		oldValue = null;
		e.commit();

	}

	/**
	 * Store the old value, and select all the text
	 * 
	 * @param p
	 *            clicked Preference object
	 */
	@Override
	public boolean onPreferenceClick(Preference p) {
		if (p instanceof EditTextPreference) {
			EditTextPreference etp = (EditTextPreference) p;

			oldValue = etp.getText();
			etp.getEditText().selectAll();
		}

		return true;
	}

	/**
	 * Set the preference summary to the preference value
	 * 
	 * @param p
	 *            Preference object
	 */
	protected void setPreferenceSummary(Preference p) {
		if (p instanceof EditTextPreference) {
			EditTextPreference etp = (EditTextPreference) p;
			etp.setSummary(etp.getText());
		}
	}

	protected void initPreferences() {
		Preference p;

		for (int i = 0; i < ps.getPreferenceCount(); i++) {
			p = ps.getPreference(i);

			if (p instanceof PreferenceCategory) {
				PreferenceCategory pref = (PreferenceCategory) p;
				for (int j = 0; j < pref.getPreferenceCount(); j++)
					initPreference(pref.getPreference(j));
			} else
				initPreference(p);
		}
	}

	protected void initPreference(Preference p) {
		p.setOnPreferenceClickListener(this);
		setPreferenceSummary(p);
	}

	/**
	 * Check the validity of the settings
	 * 
	 * @param sharedPreferences
	 *            SharedPreferences object
	 * @return result of the check as Validity enum
	 */
	protected Validity isValid(SharedPreferences sharedPreferences) {
		return Validity.VALID;
	}

	/**
	 * Restore default settings
	 */
	OnMenuItemClickListener restoreSettings = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {

			showConfirmDialog();

			return true;
		}

		private void showConfirmDialog() {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						Toast.makeText(SettingsActivity.this,
								R.string.preference_settings_restored,
								Toast.LENGTH_LONG).show();
						restorePreferences();
						break;

					default:
						break;
					}
				}
			};

			(new AlertDialog.Builder(SettingsActivity.this))
					.setMessage(R.string.preference_settings_restore_title)
					.setPositiveButton(
							R.string.preference_settings_restore_confirm,
							dialogClickListener)
					.setNegativeButton(
							R.string.preference_settings_restore_cancel,
							dialogClickListener).show();
		}

		private void restorePreferences() {
			setTextPreference(R.string.prefs_request_timeout,
					getString(R.string.request_timeout_default));
			setBooleanPreference(
					R.string.prefs_issues_notifications,
					Boolean.valueOf(getString(R.string.issues_notifications_default)));
			setBooleanPreference(
					R.string.prefs_tasks_notifications,
					Boolean.valueOf(getString(R.string.tasks_notifications_default)));
		}

		private void setTextPreference(int id, String value) {
			Editor e;

			((EditTextPreference) ps.findPreference(getString(id)))
					.setText(value);

			e = ps.getSharedPreferences().edit();
			e.putString(getString(id), value);
			e.commit();

			setPreferenceSummary(findPreference(getString(id)));
		}

		private void setBooleanPreference(int id, Boolean value) {
			Editor e;
			((CheckBoxPreference) ps.findPreference(getString(id)))
					.setChecked(value);

			e = ps.getSharedPreferences().edit();
			e.putBoolean(getString(id), value);
			e.commit();

			setPreferenceSummary(findPreference(getString(id)));
		}
	};

	/**
	 * Show password change dialog
	 */
	OnMenuItemClickListener changePassword = new OnMenuItemClickListener() {
		Dialog dialog;

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			dialog = new Dialog(SettingsActivity.this);
			dialog.setContentView(R.layout.dialog_change_password);
			dialog.setTitle(SettingsActivity.this
					.getString(R.string.change_password_title));
			dialog.findViewById(R.id.change_password_button)
					.setOnClickListener(changePasswordRequest);
			dialog.show();
			return false;
		}

		private OnClickListener changePasswordRequest = new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences sp = ps.getSharedPreferences();
				String oldPassword = ((EditText) dialog
						.findViewById(R.id.old_password)).getText().toString();
				String actualOldPassword = sp.getString(
						getString(R.string.prefs_password), null);
				String newPassword = ((EditText) dialog
						.findViewById(R.id.new_password)).getText().toString();
				String newPasswordConfirm = ((EditText) dialog
						.findViewById(R.id.new_password_confirm)).getText()
						.toString();

				if (oldPassword.compareTo(actualOldPassword) != 0)
					((EditText) dialog.findViewById(R.id.old_password))
							.setError(SettingsActivity.this
									.getString(R.string.old_password_error));
				else if (newPassword.compareTo(newPasswordConfirm) != 0)
					((EditText) dialog.findViewById(R.id.new_password_confirm))
							.setError(SettingsActivity.this
									.getString(R.string.new_password_confirm_error));
				else {
					String serverUrl = sp.getString(
							getString(R.string.prefs_server_url), null);
					int userId = sp.getInt(getString(R.string.prefs_user_id),
							-1);

					new AsyncPasswordChange(serverUrl
							+ VineyardServer.WORKERS_API + userId, newPassword)
							.execute();
				}

			}
		};

		class AsyncPasswordChange extends AsyncHttpRequest {
			private final static String TAG = "AsyncPasswordChange";

			String newPassword;

			public AsyncPasswordChange(String serverUrl, String password) {
				super(serverUrl, Type.PUT);
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(Worker.PASSWORD, Util
						.md5(password)));
				setParams(params);

				newPassword = password;
			}

			@Override
			protected void onPreExecute() {
				dialog.findViewById(R.id.settings_change_password_form)
						.setVisibility(View.INVISIBLE);
				dialog.findViewById(R.id.settings_change_password_progress)
						.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onPostExecute(Pair<Integer, String> response) {
				if (response != null
						&& response.first == HttpStatus.SC_ACCEPTED) {
					ps.getSharedPreferences()
							.edit()
							.putString(getString(R.string.prefs_password),
									newPassword).apply();
					dialog.dismiss();
					Toast.makeText(
							SettingsActivity.this,
							SettingsActivity.this
									.getString(R.string.change_password_done),
							Toast.LENGTH_SHORT).show();
				} else {
					if (response != null)
						Log.e(TAG, response.first + ": " + response.second);

					dialog.findViewById(R.id.settings_change_password_form)
							.setVisibility(View.VISIBLE);
					dialog.findViewById(R.id.settings_change_password_progress)
							.setVisibility(View.INVISIBLE);

					Toast.makeText(
							SettingsActivity.this,
							SettingsActivity.this
									.getString(R.string.change_password_error),
							Toast.LENGTH_SHORT).show();
				}
			}

		}

	};
}