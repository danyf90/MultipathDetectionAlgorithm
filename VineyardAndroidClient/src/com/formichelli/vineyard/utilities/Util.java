package com.formichelli.vineyard.utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Util {

	/**
	 * Set the height of the ListView to its maximum size since it collapse if
	 * it is placed inside a ScrollView
	 */
	public static void fixListHeight(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
			return;

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();

		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * Compute the md5 of @p value and returns its hexadecimal value
	 */
	public static String md5(String value) {

		try {
			return getHexString(MessageDigest.getInstance("MD5").digest(
					value.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

	}

	/**
	 * Hide soft-keyboard if it is showing
	 */
	public static void hideKeyboard(Activity context) {
		if (context.getCurrentFocus() != null) {
			InputMethodManager inputManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(context.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	private static String getHexString(byte[] digest) {
		return String.format("%032x", new BigInteger(1, digest));
	}

	
}
