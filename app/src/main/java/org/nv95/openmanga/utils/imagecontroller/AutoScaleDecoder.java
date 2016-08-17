package org.nv95.openmanga.utils.imagecontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.nv95.openmanga.OpenMangaApplication;

/**
 * Created by nv95 on 26.07.16.
 */

public class AutoScaleDecoder extends SkiaImageDecoder {

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {

        return ImageLoader.getInstance().loadImageSync(uri.toString(),
                OpenMangaApplication.getImageLoaderOptionsBuilder()
                        .imageScaleType(ImageScaleType.NONE)
                        .postProcessor(ImageShifter.getInstance())
                        .build());
    }
}
