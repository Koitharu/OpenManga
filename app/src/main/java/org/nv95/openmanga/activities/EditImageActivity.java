package org.nv95.openmanga.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.ImageCreator;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Владимир on 03.02.2015.
 */
public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ORIENTATION_PORT = 1;
    private static final int ORIENTATION_LAND = 2;

    public static final String IMAGE_URL = "image_url";
    // Static final constants
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 5;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
    private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
    //private static final int ON_TOUCH = 1;
    private static final int RESULT_LOAD_IMAGE = 1;

    // Instance variables
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;
    private CropImageView cropImageView;

    //    private int ROTATE_LEFT_ID = 0x1;
//    private int ROTATE_RIGHT_ID = 0x2;
//    private int ROTATE_CROP_ID = 0x3;
    private String imgUrl;
    private ImageLoader imageLoader;
    boolean isRec;
    private int rotate = 90;
    private ImageCreator imageCreator;
    private boolean imageLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cropper_image);
        findViewById(R.id.bGetImage).setOnClickListener(this);
        findViewById(R.id.bDone).setOnClickListener(this);

        imgUrl = getIntent().getStringExtra(IMAGE_URL);

        cropImageView = (CropImageView) findViewById(R.id.CropImageView);
//        cropImageView.setAspectRatio(5, 10);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setGuidelines(1);
        //cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

        imageLoader = ImageLoader.getInstance();
        imageCreator = new ImageCreator(this);

        if(imgUrl!=null)
            loadImage();
        else
            addPhoto();


        updateSize(this, cropImageView, 1.6f);
    }

    void addPhoto(){
        if(Build.VERSION.SDK_INT > 22)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
        else
            imageCreator.addPhoto();
    }

    private void loadImage() {
        String _tempUrl;
        if (imgUrl.startsWith("http"))
            _tempUrl = imgUrl;
        else
            _tempUrl = "file://" + imgUrl.replace("file://", "");
        imageLoaded = false;
        imageLoader.loadImage(_tempUrl, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                cropImageView.setImageBitmap(loadedImage);
                cropImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageLoaded = true;
                        mAspectRatioX = loadedImage.getWidth();
                        mAspectRatioY = loadedImage.getHeight();
                        //cropImageView.setFixedAspectRatio(true);
                        cropImageView.setAspectRatio(10, 5);
                    }
                });
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
        bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
    }

    // Restores the state upon rotating the screen/restarting the activity
    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
        mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bDone) {
            if(!imageLoaded){
                finish();
            } else {
                cropImage();
            }
        } else if (v.getId() == R.id.bGetImage) {
            addPhoto();
        }
    }

    /**
     * Перерисовываем сетку в зависомости от выбранных параметров
     */
    void initGrid() {
        cropImageView.setFixedAspectRatio(!isRec);
        cropImageView.setAspectRatio(
                isRec ? DEFAULT_ASPECT_RATIO_VALUES : rotate == 90 ? mAspectRatioX : mAspectRatioY,
                isRec ? DEFAULT_ASPECT_RATIO_VALUES : rotate == 90 ? mAspectRatioY : mAspectRatioX);
    }

    private void cropImage() {
        Bitmap img = cropImageView.getCroppedImage();
        cropImageView.setImageResource(0);
        System.gc();

        File myDir = getExternalFilesDir("temp");
        myDir.mkdirs();
        File file = new File(myDir, "header.jpg");
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            Intent intent = new Intent();
            intent.putExtra(IMAGE_URL, file.getAbsolutePath());
            intent.putExtra("originalmage", imgUrl);
            setResult(RESULT_OK, intent);

        } catch (Exception e) {
            Toast m = Toast.makeText(this, R.string.error_crop_image, Toast.LENGTH_SHORT);
            m.setGravity(Gravity.TOP, 0, 60);
            m.show();
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imgUrl = imageCreator.getPathFromActivityResult(requestCode, resultCode, data);
        if(imgUrl!=null)
            loadImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 777){
            for (int i = 0; i < permissions.length; i++) {
                if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    imageCreator.addPhoto();
                    break;
                }
            }
        }
    }

    /**
     * Подгоняем картинку ао размеру экрана
     *
     * @param context
     * @param photoView
     * @param videoProportion
     */
    @TargetApi(13)
    public static void updateSize(Activity context, View photoView, float videoProportion) {

        Display display = context.getWindowManager().getDefaultDisplay();

        Point screenSize = new Point();

        // Get the width of the screen
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(screenSize);
        } else {
            screenSize.set(display.getWidth(), display.getHeight());
            screenSize.set(display.getWidth(), display.getHeight());
        }

        ViewGroup.LayoutParams lp = photoView.getLayoutParams();
        int statusbar = (int) context.getResources().getDimension(R.dimen.status_bar_height);

        if (getOrientation(context) == ORIENTATION_PORT) {
            lp.width = screenSize.x - statusbar;
            lp.height = (int) ((float) screenSize.x / videoProportion);
        } else {
            lp.width = screenSize.x;
            lp.height = screenSize.y;
        }
        photoView.setLayoutParams(lp);
    }

    public static int getOrientation(Activity mContext) {
        final int orientation = mContext.getResources().getConfiguration().orientation;
        final int rotation = mContext.getWindowManager().getDefaultDisplay().getOrientation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ORIENTATION_PORT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ORIENTATION_LAND;
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ORIENTATION_PORT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ORIENTATION_LAND;
            }
        }
        return ORIENTATION_PORT;
    }

//    public static String getImageUrl(Context context, String fileName) {
//        File file = new File(context.getExternalCacheDir().getAbsolutePath() + "/saved_images/"+fileName);
//        if(file.exists())
//            return file.getAbsolutePath();
//        return null;
//    }

}
