package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.formichelli.vineyard.entities.IssueTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

// TODO remove photos when are no more needed

public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_SELECT_PHOTO = 2;

	VineyardMainActivity activity;
	IssueTask i;
	String currentPhotoPath;
	LinearLayout gallery;
	Spinner places, priorities;
	ImageView addPhoto;
	boolean actionMode = false;
	Set<ImageView> selected;
	MenuItem deleteItem;
	int imageSize, selectedImageSize, imagePadding;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		i = new IssueTask();
		selected = new HashSet<ImageView>();

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_report_issue, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		activity.reportIssueFragment = this;

		addPhoto = (ImageView) activity.findViewById(R.id.action_add_photo);
		addPhoto.setOnClickListener(dispatchTakePictureIntent);

		gallery = (LinearLayout) activity
				.findViewById(R.id.report_issue_gallery);

		priorities = (Spinner) activity
				.findViewById(R.id.report_issue_priority);
		setSpinnerAdapter(priorities, R.array.priorities);

		places = (Spinner) activity.findViewById(R.id.report_issue_place);
		setSpinnerAdapter(places, R.array.places);
		
		imageSize = getResources().getDimensionPixelSize(R.dimen.gallery_inner_height);
		selectedImageSize = (int) 0.8*imageSize;
		
		imagePadding = getResources().getDimensionPixelSize(R.dimen.gallery_padding);
	}

	private void setSpinnerAdapter(Spinner s, int array) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				activity, array, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		s.setAdapter(adapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_report_issue, menu);
		// showGlobalContextActionBar();

		// deleteItem = (MenuItem)
		// activity.findViewById(R.id.action_report_issue_delete_selected_photos);
		deleteItem = menu.getItem(0);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_report_issue_delete_selected_photos:
			for (ImageView v : selected)
				gallery.removeView(v);

			selected.clear();

			break;
		case R.id.action_report_issue_cancel:
			closeFragment();

			break;
		case R.id.action_report_issue_send:
			if (parseFields()) {
				Toast.makeText(activity, "TODO: send issue to server",
						Toast.LENGTH_LONG).show();
				closeFragment();
				break;
			}
		default:
			return false;
		}

		return true;
	}

	private void closeFragment() {
		activity.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container,
						((VineyardMainActivity) getActivity()).issuesFragment)
				.commit();
	}

	private boolean parseFields() {
		Activity activity = getActivity();
		EditText t;
		String s;

		t = (EditText) activity.findViewById(R.id.report_issue_title);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") == 0)
				return false;

			i.setTitle(s);
		}

		t = (EditText) activity.findViewById(R.id.report_issue_description);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") != 0)
				i.setDescription(s);
		}

		return true;
	}

	OnClickListener dispatchTakePictureIntent = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
				// Create the File where the photo should go
				File photoFile;

				try {
					photoFile = createImageFile();
				} catch (IOException ex) {
					return;
				}

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	};

	// private void dispatchChoosePictureIntent() {
	// Intent i = new Intent(Intent.ACTION_PICK,
	// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	// startActivityForResult(i, REQUEST_SELECT_PHOTO);
	// }

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "issue_" + timeStamp + "";
		File storageDir = activity.getExternalFilesDir(null);
//		File storageDir = Environment
//				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		currentPhotoPath = image.getAbsolutePath();
		Log.e("PATH", currentPhotoPath);

		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bitmap b = null;

		if (requestCode == REQUEST_TAKE_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			try {
				if (currentPhotoPath != null)
					i.addPhoto(new URL(currentPhotoPath));
			} catch (MalformedURLException e) {
			}

			b = getBitmapFromFilePath(currentPhotoPath, getResources()
					.getDimensionPixelSize(R.dimen.gallery_inner_height));

			ImageView v = new ImageView(activity);
			v.setImageBitmap(b);
			
			v.setPadding(0, imagePadding, 0, imagePadding);
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// select if it is not selected and viceversa
					setSelected((ImageView) v, !selected.contains(v));
				}
			});

			gallery.addView(v, 0);
		}
	}

	public static Bitmap getBitmapFromFilePath(String filePath, int size) {
		return ThumbnailUtils.extractThumbnail(
				BitmapFactory.decodeFile(filePath), size, size);
	}

	private void setSelected(ImageView v, boolean select) {
		if (select) {
			if (selected.isEmpty()) {
				actionMode = true;
				deleteItem.setVisible(true);
			}

			selected.add(v);
			v.setBackgroundColor(getResources().getColor(R.drawable.wine_medium));
		} else {
			selected.remove(v);
			v.setBackgroundColor(getResources().getColor(R.drawable.white));

			if (selected.isEmpty()) {
				actionMode = false;
				deleteItem.setVisible(false);
			}
		}
	}

};