package com.example.administrator.faceiden0418;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 58 on 2015/10/10.
 */
public class BitmapUtil {
    public static Bitmap getBitmapFromDisk(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return null;
        return getBitmapFromDisk(new File(fileName));
    }

    public static Bitmap getBitmapFromDisk(File file) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(fis);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    public static Bitmap compress(Bitmap image) {
        double time = System.currentTimeMillis();
        image = cutImage(image, 30);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        image.recycle();
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        int STANDARD_WIDTH = 128;
        int STANDARD_HEIGHT = 192;

        int be = (newOpts.outWidth / STANDARD_WIDTH + newOpts.outHeight / STANDARD_HEIGHT) / 2 + 2;
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        newOpts.inJustDecodeBounds = false;
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }

    public static Bitmap cutImage(Bitmap src, int cutY) {
        //if (RT.DEBUG) DLOG.d("choices", "cutImage!!");

        int bit_x, bit_y, bit_width, bit_height;
        bit_y = cutY;
        bit_height = src.getHeight() - 2 * bit_y;
        bit_width = bit_height * 10 / 16;
        bit_x = (src.getWidth() - bit_width) / 2 + 1;
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(src, bit_x, bit_y, bit_width, bit_height);
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = src;
        }
        return bitmap;
    }

    public static void saveBitmapToDisk(File file, Bitmap bitmap) {
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqHeight <= 0 || reqWidth <= 0) {
            return inSampleSize;
        }

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            //             This offers some additional logic in case the image has a strange
            //             aspect ratio. For example, a panorama may have a much larger
            //             width than height. In these cases the total pixels might still
            //             end up being too large to fit comfortably in memory, so we should
            //             be more aggressive with sample down the image (=largerinSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
}
