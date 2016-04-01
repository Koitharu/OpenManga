package org.nv95.openmanga.utils;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.EditImageActivity;

/**
 * Created by Владимир on 17.03.2016.
 */
public class DrawerHeaderImageTool implements View.OnClickListener {

    static int REQUEST_IMAGE_HEADER = 434;

    private final ImageView imageView;
    private final TextView textView;
    private AppCompatActivity activity;
    private NavigationView navigationView;
    private String imageUrl;

    public DrawerHeaderImageTool(AppCompatActivity activity, NavigationView navigationView){
        this.activity = activity;
        this.navigationView = navigationView;
        View v = navigationView.getHeaderView(0);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        textView = (TextView) v.findViewById(R.id.textView);
        imageUrl = PreferenceManager.getDefaultSharedPreferences(activity).getString(Constants.SAVED_IMAGE_HEAD, null);

        imageView.setOnClickListener(this);
    }

    public void initDrawerImage() {
        if(imageUrl!=null) {
            if(!imageUrl.startsWith("http") && !imageUrl.startsWith("file"))
                imageUrl = "file://" + imageUrl;

            ImageLoader.getInstance().displayImage(imageUrl, imageView, OpenMangaApplication
                    .getImageLoaderOptionsBuilder().cacheOnDisk(false).cacheInMemory(false).build());
            textView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(R.drawable.side_nav_bar);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void changeImage(){
        Intent intent = new Intent(activity, EditImageActivity.class);
        intent.putExtra(EditImageActivity.IMAGE_URL, imageUrl);
        activity.startActivityForResult(intent, REQUEST_IMAGE_HEADER);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_HEADER && resultCode == Activity.RESULT_OK) {
            imageUrl = data.getStringExtra(EditImageActivity.IMAGE_URL);
            PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .putString(Constants.SAVED_IMAGE_HEAD, imageUrl)
                    .apply();
            imageView.setImageResource(R.drawable.side_nav_bar);
            initDrawerImage();
        }
    }

    @Override
    public void onClick(View v) {
        changeImage();
    }
}
