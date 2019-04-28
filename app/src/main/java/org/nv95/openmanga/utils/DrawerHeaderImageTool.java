package org.nv95.openmanga.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import org.nv95.openmanga.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static org.nv95.openmanga.R.id.imageView;
import static org.nv95.openmanga.R.id.textView;

/**
 * Created by Владимир on 17.03.2016.
 */
public class DrawerHeaderImageTool implements View.OnClickListener {

    private static final int REQUEST_IMAGE_HEADER = 434;

    private final ImageView mImageView;
    private final TextView mTextView;
    private final AppCompatActivity mActivity;
    private final File mImageFile;

    public DrawerHeaderImageTool(AppCompatActivity activity, NavigationView navigationView) {
        mActivity = activity;
        View v = navigationView.getHeaderView(0);
        mImageView = v.findViewById(imageView);
        mTextView = v.findViewById(textView);
        mImageFile = new File(activity.getExternalFilesDir("temp"), "header");
        mImageView.setOnClickListener(this);
    }

    public void initDrawerImage() {
        if(mImageFile.exists()) {
            mImageView.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getPath()));
            mTextView.setVisibility(View.GONE);
        } else {
            mImageView.setImageResource(R.drawable.side_nav_bar);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_IMAGE_HEADER: //галлерея
                String file = getImageFile(mActivity, data.getData());
                if (file != null) {
                    beginCrop(data.getData());
                } else {
                    Toast.makeText(mActivity, R.string.error,Toast.LENGTH_SHORT).show();
                }
                break;
            case Crop.REQUEST_CROP: //crop
                Bitmap source = BitmapFactory.decodeFile(mImageFile.getPath());
                Bitmap thumb = ThumbnailUtils.extractThumbnail(source, mImageView.getWidth(), mImageView.getHeight());
                source.recycle();
                //save to file
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(mImageFile);
                    thumb.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    mImageFile.delete();
                } finally {
                    thumb.recycle();
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                initDrawerImage();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        requestImageSelection(mActivity, REQUEST_IMAGE_HEADER);
    }

    public static String getImageFile(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String filePath = null;
        Cursor cursor = context.getContentResolver().query(
                uri, filePathColumn, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(mImageFile);
        Crop.of(source, destination)
                .withAspect(mImageView.getWidth(), mImageView.getHeight())
                .start(mActivity);
    }

    public static void requestImageSelection(Activity activity, int requestCode) {
        Intent pickerIntent = new Intent(Intent.ACTION_PICK);
        pickerIntent.setType("image/*");
        pickerIntent = Intent.createChooser(pickerIntent, activity.getString(R.string.select_image));
        activity.startActivityForResult(pickerIntent, requestCode);
    }
}
