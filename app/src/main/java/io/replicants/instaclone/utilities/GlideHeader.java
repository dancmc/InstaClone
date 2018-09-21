package io.replicants.instaclone.utilities;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

public class GlideHeader{

    private static String AUTHORIZATION = "QWERTYUIOPASDFGHJKL";

    public static GlideUrl getUrlWithHeaders(String url){
        if(url!=null) {
            return new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("Authorization", AUTHORIZATION)
                    .build());
        } else {
            return null;
        }
    }

    public static void setAuthorization(String jwt){
            AUTHORIZATION = jwt;
    }

}