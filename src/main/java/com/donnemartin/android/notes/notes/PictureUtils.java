package com.donnemartin.android.notes.notes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;

import android.widget.ImageView;

public class PictureUtils {

    @SuppressWarnings("deprecation")
    public static BitmapDrawable getScaledDrawable(Activity activity,
                                                   String path) {
        Display display = activity.getWindowManager().getDefaultDisplay();

        // Display.getWidth() and Display.getHeight() are deprecated
        // It would be best to scale the image so that it fits the ImageView
        // perfectly, but the size of the view in which the image will be
        // displayed is often not available, such as onCreateView, where
        // you cannot get the size of the ImageView.
        // Instead, scale the image to the default display for the device
        float destWidth = display.getWidth();
        float destHeight = display.getHeight();

        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;

        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round((float)srcHeight / (float)destHeight);
            } else {
                inSampleSize = Math.round((float)srcWidth / (float)destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(activity.getResources(), bitmap);
    }

    public static void cleanImageView(ImageView imageView) {
        if ((imageView.getDrawable() instanceof BitmapDrawable)) {
            // Clean up the view's image for the sake of memory
            BitmapDrawable bitmapDrawable =
                (BitmapDrawable) imageView.getDrawable();

            // Frees up the native storage for the bitmap (most of the meat)
            // If you don't call this the memory will not be freed up at
            // garbage collection time, but in a finalizer, which might cause
            // you to run out of memory.
            bitmapDrawable.getBitmap().recycle();
            imageView.setImageDrawable(null);
        }
    }
}

