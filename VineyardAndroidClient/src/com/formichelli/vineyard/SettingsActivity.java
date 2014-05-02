package com.formichelli.vineyard;

import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = "SettingsActivity";

	private enum Validity {
		VALID, NOT_VALID_SERVER_URL, NOT_VALID_SERVER_PORT,
	}

	PreferenceScreen ps;
	String oldValue;
	private MenuItem restoreDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		ps = getPreferenceScreen();

		// TODO why doesn't
		// ps.findPreference(getString(R.string.preference_general_settings))
		// work instead of ps.getPreference(0)?
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
			((PreferenceCategory) ps.getPreference(0))
					.removePreference(ps
							.findPreference(getString(R.string.preference_immersive_mode)));

		initPreferences();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);

		restoreDefault = menu.findItem(R.id.action_settings_restore);

		restoreDefault.setOnMenuItemClickListener(this.restoreSettings);

		return true;
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

			case NOT_VALID_SERVER_URL:
				msgId = R.string.preference_server_url_error;
				break;

			case NOT_VALID_SERVER_PORT:
				msgId = R.string.preference_server_port_error;
				break;
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

		if (p.getKey() == getString(R.string.preference_immersive_mode)) {
			onWindowFocusChanged(false);
			onWindowFocusChanged(true);
			onWindowFocusChanged(false);
		}

		return true;
	}

	/**
	 * Set the preference summary to the preference value
	 * 
	 * @param p
	 *            Preference objet
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
		int serverPort;
		String serverURL;

		serverURL = sharedPreferences.getString(
				getString(R.string.preference_server_url), null);

		try {
			serverPort = Integer.parseInt(sharedPreferences.getString(
					getString(R.string.preference_server_port), "-1"));
		} catch (NumberFormatException nfe) {
			return Validity.NOT_VALID_SERVER_PORT;
		}

		if (serverURL == null) {
			// is this a valid check ?
			try {
				new URL(serverURL);
			} catch (MalformedURLException e) {
				return Validity.NOT_VALID_SERVER_URL;
			}
		}

		if (serverPort < 1 || serverPort > 65535)
			return Validity.NOT_VALID_SERVER_PORT;

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
			setTextPreference(R.string.preference_server_url,
					getString(R.string.preference_server_url_default));
			setTextPreference(R.string.preference_server_port,
					getString(R.string.preference_server_port_default));
			setBooleanPreference(
					R.string.preference_preload_all,
					Boolean.valueOf(getString(R.string.preference_preload_all_default)));
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

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		boolean immersiveMode = ps
				.getPreferenceManager()
				.getSharedPreferences()
				.getBoolean(
						getString(R.string.preference_immersive_mode),
						Boolean.valueOf(getString(R.string.preference_immersive_mode_default)));

		if (immersiveMode) {
			if (hasFocus && android.os.Build.VERSION.SDK_INT >= 19)
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
		else {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}

}