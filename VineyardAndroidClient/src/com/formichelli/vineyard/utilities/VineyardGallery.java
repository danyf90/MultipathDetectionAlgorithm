package com.formichelli.vineyard.utilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.formichelli.vineyard.R;

/**
 * A Gallery is an horizontal LinearLayout that can contain zero or more photos,
 * if it is locked no photos can be added or removed, if it is unlocked photos
 * can be added (from camera) or deleted.
 */
public class VineyardGallery extends HorizontalScrollView {
	public static final int REQUEST_TAKE_PHOTO = 1;

	ImageView addDeletePhoto;
	LinearLayout gallery;
	boolean locked;
	int size, padding, selectedColor, notSelectedColor;
	HashSet<ImageView> selected;
	HashMap<ImageView, String> images, toBeDeleted;
	Drawable add, delete;
	Fragment fragment;

	public VineyardGallery(Context context) {
		super(context);
		initView();
	}

	public VineyardGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		readAttributes(attrs);
		initView();
	}

	public VineyardGallery(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
		readAttributes(attrs);
		initView();
	}

	private void readAttributes(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
				R.styleable.VineyardGallery, 0, 0);

		locked = a.getBoolean(R.styleable.VineyardGallery_locked, false);
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	private void initView() {
		Context context = getContext();
		LayoutInflater.from(context).inflate(R.layout.vineyard_gallery, this);

		gallery = (LinearLayout) this
				.findViewById(R.id.com_formichelli_vineyard_gallery_linear_layout);
		addDeletePhoto = (ImageView) this
				.findViewById(R.id.com_formichelli_vineyard_gallery_action_add_delete_photo);

		size = context.getResources().getDimensionPixelSize(
				R.dimen.gallery_height);
		padding = context.getResources().getDimensionPixelSize(
				R.dimen.gallery_padding);

		selected = new HashSet<ImageView>();
		images = new HashMap<ImageView, String>();
		toBeDeleted = new HashMap<ImageView, String>();

		selectedColor = context.getResources().getColor(R.color.white);
		notSelectedColor = context.getResources().getColor(R.color.wine_light);

		add = context.getResources().getDrawable(R.drawable.action_camera_dark);
		delete = context.getResources().getDrawable(
				R.drawable.action_delete_dark);

		if (locked)
			gallery.removeView(addDeletePhoto);

		showAddPhoto();
	}

	private void showAddPhoto() {
		addDeletePhoto.setImageDrawable(add);
		addDeletePhoto.setOnClickListener(dispatchTakePictureIntent);

	}

	private void showDeletePhoto() {
		addDeletePhoto.setImageDrawable(delete);
		addDeletePhoto.setOnClickListener(deleteSelectedImages);

	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		if (this.locked == locked)
			return;

		this.locked = locked;

		if (locked) {
			// cancel onClickListeners and hide the button
			for (int i = 0, l = gallery.getChildCount(); i < l; i++)
				gallery.getChildAt(i).setOnClickListener(null);

			deselectAll();
			addDeletePhoto.setVisibility(View.GONE);
		} else {
			// register onClickListeners and show the button
			for (int i = 0, l = gallery.getChildCount(); i < l; i++)
				gallery.getChildAt(i).setOnClickListener(selectOnClick);

			showAddPhoto();
			addDeletePhoto.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Add an image to the gallery
	 * 
	 * @param path
	 *            location of the image (either local or remote)
	 * @param mustBeDeleted
	 *            indicates if the image must be deleted once it is removed from
	 *            the gallery
	 * @return view of the added image
	 */
	public ImageView addImage(String path, boolean mustBeDeleted) {
		ImageView v;
		Bitmap b;

		if (path.startsWith("http://") || path.startsWith("https://")) {
			// the image must be load from a server
			addImageFromServer(path, null);

			return null;
		} else {
			// the image is locally stored
			b = getThumbnailFromFilePath(path, size);
			if (b == null)
				return null;

			v = new ImageView(getContext());
			v.setImageBitmap(b);
			v.setPadding(padding, padding, padding, padding);

			if (!locked)
				v.setOnClickListener(selectOnClick);

			gallery.addView(v, 0);
			images.put(v, path);
			if (mustBeDeleted)
				toBeDeleted.put(v, path);
			return v;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void addImageFromServer(String serverUrl, String path) {
		Context context = getContext();

		// create a container for the image
		LinearLayout container = new LinearLayout(context);
		ProgressBar progress = new ProgressBar(context);
		int size = context.getResources().getDimensionPixelSize(
				R.dimen.gallery_height);
		int margin = context.getResources().getDimensionPixelSize(
						R.dimen.gallery_padding);
		Drawable background = context.getResources().getDrawable(
				R.drawable.wine_light_with_wine_dark_border);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				size / 2, size / 2);
		params.setMargins(size / 4, size / 4, size / 4, size / 4);
		progress.setLayoutParams(params);
		container.addView(progress);

		params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(margin, margin, margin, margin);
		container.setLayoutParams(params);
		container.setBackgroundDrawable(background);

		// add the container to the gallery
		gallery.addView(container, 0);

		path = String.format(serverUrl, path, size, size);

		new ImageLoader(context, container, progress, path).execute();
	}

	/**
	 * Remove an image from the gallery
	 * 
	 * @param v
	 *            image to be removed
	 */
	public void removeImage(ImageView v) {
		gallery.removeView(v);
		images.remove(v);
		selected.remove(v);

		if (toBeDeleted.containsKey(v)) {
			// delete image from disk
			File f = new File(toBeDeleted.remove(v));
			if (f != null)
				f.delete();
		}

		if (selected.isEmpty())
			showAddPhoto();
	}

	/**
	 * Removes the images that are currently selected
	 */
	public void removeSelectedImages() {
		for (ImageView v : (ImageView[]) selected.toArray())
			removeImage(v);
	}

	/**
	 * Remove all the images from the gallery
	 */
	public void removeAllImages() {
		for (Entry<ImageView, String> entry : images.entrySet())
			removeImage(entry.getKey());
	}

	/*
	 * Deselect all the images
	 */
	private void deselectAll() {
		for (ImageView v : selected)
			setSelected(v, false);
	}

	/*
	 * Get a square thumbnail of a locally stored image
	 */
	private Bitmap getThumbnailFromFilePath(String filePath, int size) {
		return ThumbnailUtils.extractThumbnail(
				BitmapFactory.decodeFile(filePath), size, size);
	}

	private void setSelected(ImageView v, boolean select) {
		if (select) {
			if (selected.isEmpty())
				showDeletePhoto();

			selected.add(v);
			v.setBackgroundColor(selectedColor);
		} else {
			if (selected.isEmpty())
				showAddPhoto();

			selected.remove(v);
			v.setBackgroundColor(notSelectedColor);
		}
	}

	OnClickListener dispatchTakePictureIntent = new OnClickListener() {
		@Override
		public void onClick(View v) {
			File photoFile;
			Context context = getContext();

			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);

			if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
				try {
					photoFile = createImageFile();
				} catch (IOException ex) {
					return;
				}

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));

				if (fragment != null)
					fragment.startActivityForResult(takePictureIntent,
							REQUEST_TAKE_PHOTO);
				else
					((Activity) context).startActivityForResult(
							takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	};

	OnClickListener deleteSelectedImages = new OnClickListener() {
		@Override
		public void onClick(View v) {
			removeSelectedImages();
		}
	};

	OnClickListener selectOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// select the image if it is not selected and viceversa
			setSelected((ImageView) v, !selected.contains(v));
		}
	};

	private String currentPhotoPath;

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "issue_" + timeStamp + "";

		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				getContext().getExternalFilesDir(null) /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		currentPhotoPath = image.getAbsolutePath();

		return image;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			addImage(currentPhotoPath, true);
		}
	}
}