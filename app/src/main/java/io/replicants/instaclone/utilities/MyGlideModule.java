package io.replicants.instaclone.utilities;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@com.bumptech.glide.annotation.GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setMemoryCache(new LruResourceCache(10000000));
    }


    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

    }


}
