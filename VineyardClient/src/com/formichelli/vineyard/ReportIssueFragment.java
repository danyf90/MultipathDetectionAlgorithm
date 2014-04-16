package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

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

public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_SELECT_PHOTO = 2;

	VineyardMainActivity activity;
	IssueTask i;
	String currentPhotoPath;
	Gallery gallery;
	Spinner places, priorities;
	ImageView addPhoto;
	boolean actionMode = false;
	Menu menu;
	int imagePadding;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		i = new IssueTask();

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

		imagePadding = getResources().getDimensionPixelSize(
				R.dimen.gallery_padding);

		// populate priorities spinner
		priorities = (Spinner) activity
				.findViewById(R.id.report_issue_priority);
		setSpinnerAdapter(priorities, R.array.priorities);

		// populate places spinner
		places = (Spinner) activity.findViewById(R.id.report_issue_place);
		setSpinnerAdapter(places, R.array.places);
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

		this.menu = menu;
		gallery = new Gallery();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_report_issue_delete_selected_photos:
			// remove selected images from gallery
			gallery.removeSelectedImages();

			break;
		case R.id.action_report_issue_cancel:
			closeFragment();

			break;
		case R.id.action_report_issue_send:
			if (parseFields()) {
				Toast.makeText(activity, "TODO: send issue to server",
						Toast.LENGTH_LONG).show();
				gallery.removeAllImages();
				closeFragment();
				break;
			}
		default:
			return false;
		}

		return true;
	}

	// Navigate back to issuesFragment
	private void closeFragment() {
		activity.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container,
						((VineyardMainActivity) getActivity()).issuesFragment)
				.commit();
	}

	// Checks if the fields are valid and populates issueTask
	private boolean parseFields() {
		Activity activity = getActivity();
		EditText t;
		String s;

		t = (EditText) activity.findViewById(R.id.report_issue_title);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") == 0) {
				t.setError(getString(R.string.issue_title_error));
				return false;
			}

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
			File photoFile;

			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);

			if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
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

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "issue_" + timeStamp + "";
		File storageDir = activity.getExternalFilesDir(null);
		// File storageDir = Environment
		// .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
		if (requestCode == REQUEST_TAKE_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			try {
				i.addPhoto(new URL(currentPhotoPath));
			} catch (MalformedURLException e) {
			}

			// create an image view and put it in the gallery
			gallery.addImage(currentPhotoPath);
		}
	}

	// Class that manages the gallery
	class Gallery {
		LinearLayout gallery;
		Activity activity;
		int selectedColor, notSelectedColor;
		int size;
		int padding;
		HashSet<ImageView> selected;
		HashMap<ImageView, String> images;
		MenuItem deleteItem;

		public Gallery() {
			this.activity = ReportIssueFragment.this.activity;
			this.gallery = (LinearLayout) activity
					.findViewById(R.id.report_issue_gallery);
			;
			this.size = activity.getResources().getDimensionPixelSize(
					R.dimen.gallery_inner_height);
			this.padding = activity.getResources().getDimensionPixelSize(
					R.dimen.gallery_padding);
			this.deleteItem = ReportIssueFragment.this.menu
					.findItem(R.id.action_report_issue_delete_selected_photos);

			selected = new HashSet<ImageView>();
			images = new HashMap<ImageView, String>();

			selectedColor = activity.getResources().getColor(R.color.white);
			notSelectedColor = activity.getResources().getColor(R.color.wine_light);
		}

		public ImageView addImage(String path) {
			ImageView v;
			Bitmap b;

			b = getThumbnailFromFilePath(path, size);
			if (b == null)
				return null;

			v = new ImageView(activity);
			v.setImageBitmap(b);
			v.setPadding(0, padding, 0, padding);
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// select the image if it is not selected and viceversa
					setSelected((ImageView) v, !selected.contains(v));
				}
			});

			gallery.addView(v, 0);
			images.put(v, path);
			return v;
		}

		public void removeImage(ImageView v) {
			selected.remove(v);

			gallery.removeView(v);

			if (images.get(v) != null) {
				File f = new File(images.get(v));
				if (f != null)
					f.delete();

				images.remove(v);
			}
		}

		public void removeSelectedImages() {
			for (ImageView v : selected) {
				gallery.removeView(v);

				if (images.get(v) != null) {
					File f = new File(images.get(v));
					if (f != null)
						f.delete();

					images.remove(v);
				}
			}

			selected.clear();
			deleteItem.setVisible(false);
		}

		public void removeAllImages() {
			for (ImageView v : images.keySet()) {
				gallery.removeView(v);

				if (images.get(v) != null) {
					File f = new File(images.get(v));
					if (f != null)
						f.delete();

					images.remove(v);
				}
			}

			selected.clear();
			images.clear();
			deleteItem.setVisible(false);
		}

		private Bitmap getThumbnailFromFilePath(String filePath, int size) {
			return ThumbnailUtils.extractThumbnail(
					BitmapFactory.decodeFile(filePath), size, size);
		}

		private void setSelected(ImageView v, boolean select) {
			if (select) {
				if (selected.isEmpty())
					deleteItem.setVisible(true);

				selected.add(v);
				v.setBackgroundColor(selectedColor);
			} else {
				selected.remove(v);
				v.setBackgroundColor(notSelectedColor);

				if (selected.isEmpty())
					deleteItem.setVisible(false);
			}
		}
	};
};