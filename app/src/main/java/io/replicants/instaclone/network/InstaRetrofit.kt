package io.replicants.instaclone.network

import android.util.Log
import io.replicants.instaclone.utilities.Prefs
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

class InstaRetrofit {

    companion object {
        var domain = "danielchan.io"
        private var apiUrl = getApiUrl()
//        private var apiUrl = "http://10.0.0.3:8080/instacopy/v1/"

        private val httpclient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()

                    val jwt = Prefs.getInstance().readString(Prefs.JWT,"")
                    val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", jwt)
                            .build()

                    Log.d("API", "HTTP REQUEST :" + newRequest.url())
                    chain.proceed(newRequest)
                }.build()

        private var retrofit = getRetrofit()

        @JvmField
        var api = retrofit.create(PhotoApi::class.java)

        @JvmStatic
        fun rebuild(){
            apiUrl = getApiUrl()
            retrofit = getRetrofit()
            api = retrofit.create(PhotoApi::class.java)
        }

        fun getApiUrl():String{
            return "https://$domain/instacopy/v1/"
//            return "http://192.168.1.4:8080/instacopy/v1/"
        }

        fun getRetrofit():Retrofit{
            return Retrofit.Builder()
                    .client(httpclient)
                    .baseUrl(apiUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }


    public interface PhotoApi {

        @POST("user/register")
        fun userRegister(@Body json: String): Call<String>

        @POST("user/login")
        fun userLogin(@Body json: String): Call<String>

        @GET("user/validate")
        fun validate(): Call<String>

        @GET("feed")
        fun getFeed(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("search")
        fun discoverSearch(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("suggested/users")
        fun discoverUsers(): Call<String>

        @GET("suggested/photos")
        fun discoverPhotos(@QueryMap queries:HashMap<String, String>): Call<String>

        @Multipart
        @POST("photo/upload")
        fun photoUpload(@Part("photo") file:RequestBody, @Part("json") json:RequestBody): Call<String>

        @POST("photo/specific")
        fun specificPhotos(@Body json: String): Call<String>

        @GET("photo/comments/retrieve")
        fun getComments(@QueryMap queries:HashMap<String, String>): Call<String>

        @POST("photo/comments/new")
        fun newComment(@Body json: String): Call<String>

        @POST("photo/comments/delete")
        fun deleteComment(@Body json: String): Call<String>

        @GET("photo/likes/retrieve")
        fun getLikes(@QueryMap queries:HashMap<String, String>): Call<String>

        @POST("photo/likes/like")
        fun likePhoto(@Body json: String): Call<String>

        @POST("photo/likes/unlike")
        fun unlikePhoto(@Body json: String): Call<String>

        @GET("activity/self")
        fun getSelfActivity(): Call<String>

        @GET("activity/following")
        fun getFollowingActivity(): Call<String>

        @GET("user/info")
        fun getUserInfo(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("user/photos")
        fun getUserPhotos(@QueryMap queries:HashMap<String, String>): Call<String>

        @POST("user/follow")
        fun followUser(@Body json: String): Call<String>

        @POST("user/unfollow")
        fun unfollowUser(@Body json: String): Call<String>

        @GET("user/requests")
        fun getRequests(): Call<String>

        @POST("user/approve")
        fun approveUser(@Body json: String): Call<String>

        @GET("user/followers")
        fun getFollowers(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("user/following")
        fun getFollowing(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("user/followingWhoFollow")
        fun getFollowingWhoFollow(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("user/getDetails")
        fun getDetails(): Call<String>

        @Multipart
        @POST("user/update")
        fun updateDetails(@Part("profile_image") file:RequestBody, @Part("json") json:RequestBody): Call<String>

        @Multipart
        @POST("user/update")
        fun updateDetails(@Part("json") json:RequestBody): Call<String>
    }
}