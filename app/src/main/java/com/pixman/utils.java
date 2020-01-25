package com.pixman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class utils {

    // Draw Text on Image at Center of the image
    @NotNull
    public static Bitmap addTextOnBitmap(@Nullable Bitmap bitmap, Context context, ArrayList<Bitmap> map) {
        String text = "GreedyGain";
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(context.getResources().getColor(R.color.colorPrimary));
        paint.setTextSize(72);

        // draw text to the Canvas center
        Rect boundsText = new Rect();
        paint.getTextBounds(text, 0,
                text.length(), boundsText);
        int x = (canvas.getWidth() / 2);
        int y = (bitmap.getHeight() + boundsText.height()) / 2;

        canvas.drawText(text, x, y, paint);

        map.add(mutableBitmap);
        return mutableBitmap;
    }

    // Save method to save image on internal storage.
    public static boolean saveImage(@Nullable Bitmap bitmap) {


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_.jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File folder = new File(storageDir, "PIXMAN");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File imagesFolder = new File(folder, "Images");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }


        FileOutputStream outStream = null;

        File outFile = new File(imagesFolder, imageFileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
            outStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    // Add Opacity on image
    public static Bitmap setOpacity(@Nullable Bitmap bitmap, @NotNull ArrayList<Bitmap> map) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        // config paint
        final Paint paint = new Paint();
        paint.setAlpha(127);                // 50% opacity
        canvas.drawBitmap(bitmap, 0, 0, paint);
        map.add(transBitmap);
        return transBitmap;


    }

    // Flip image Vertically
    public Bitmap verticalFlip(Bitmap original, ArrayList<Bitmap> map) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        Bitmap bitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        map.add(bitmap);
        return bitmap;
    }

    // Flip Image Horizontally
    public Bitmap horizontalFlip(Bitmap original, ArrayList<Bitmap> map) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        Bitmap bitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        map.add(bitmap);
        return bitmap;
    }
}
