package com.formichelli.vineyard.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.formichelli.vineyard.R;

/**
 * A Gallery is an horizontal LinearLayout that can contain zero or more photos
 */
public class Gallery {
	Activity activity;
	MenuItem deleteItem;
	LinearLayout gallery;
	boolean deletable;
	int selectedColor, notSelectedColor;
	int size;
	int padding;
	HashSet<ImageView> selected;
	HashMap<ImageView, String> images;

	/**
	 * Create a gallery object
	 * 
	 * @param context
	 *            Activity context
	 * @param gallery
	 *            LinearLayout associated to the Gallery
	 * @param deletable
	 *            whether the photos are deletable or not
	 * @param deleteItem
	 *            menuItem associated with the deletion of a photo
	 */
	public Gallery(Activity context, LinearLayout gallery, boolean deletable,
			MenuItem deleteItem) {
		this.activity = context;
		this.gallery = gallery;
		this.deletable = deletable;
		this.deleteItem = deleteItem;

		this.size = activity.getResources().getDimensionPixelSize(
				R.dimen.gallery_height);
		this.padding = activity.getResources().getDimensionPixelSize(
				R.dimen.gallery_padding);

		selected = new HashSet<ImageView>();
		images = new HashMap<ImageView, String>();

		selectedColor = activity.getResources().getColor(R.color.white);
		notSelectedColor = activity.getResources().getColor(R.color.wine_light);
	}

	/**
	 * Add an image to the gallery
	 * 
	 * @param path
	 *            location of the image
	 * @return view of the added image
	 */
	public ImageView addImage(String path) {
		ImageView v;
		Bitmap b;

		b = getThumbnailFromFilePath(path, size);
		if (b == null)
			return null;

		v = new ImageView(activity);
		v.setImageBitmap(b);
		v.setPadding(0, padding, 0, padding);

		if (deletable)
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

	/**
	 * Remove an image from the gallery
	 * 
	 * @param v
	 *            image to be removed
	 */
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

	/**
	 * Removes the images that are currently selected
	 */
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

	/**
	 * Remove all the images from the gallery
	 */
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