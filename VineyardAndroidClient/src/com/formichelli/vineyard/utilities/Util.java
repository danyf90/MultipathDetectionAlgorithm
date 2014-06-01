package com.formichelli.vineyard.utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.view.View;
import android.view.ViewGroup;
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

	private static String getHexString(byte[] digest) {
		return String.format("%032x", new BigInteger(1, digest));
	}

}
