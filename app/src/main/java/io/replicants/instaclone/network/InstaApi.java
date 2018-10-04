package io.replicants.instaclone.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InstaApi {



    private static String TAG = "INSTA_API";

    public static Call<String> userRegister(
            String username, String password,
            @Nullable String firstName, String lastName, String displayName, String email) {


        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
            json.put("first_name", firstName);
            json.put("last_name", lastName);
            json.put("display_name", displayName);
            json.put("email", email);

        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.userRegister(json.toString());
    }

    public static Call<String> userLogin(
            String username, String password) {


        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);

        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.userLogin(json.toString());
    }

    public static Call<String> validate() {
        return InstaRetrofit.api.validate();
    }

    public static Call<String> getFeed(
            Sort sort, @Nullable Double latitude, @Nullable Double longitude, @Nullable String lastPhotoID) {


        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("sort", sort.toString());
        if (longitude != null) {
            queryMap.put("longitude", longitude.toString());
        }
        if (latitude != null) {
            queryMap.put("latitude", latitude.toString());
        }
        if (lastPhotoID != null) {
            queryMap.put("last_photo_fetched", lastPhotoID);
        }

        return InstaRetrofit.api.getFeed(queryMap);
    }

    public static Call<String> discoverSearch(
            String displayName, @Nullable Integer page) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if (page != null && page != 0) {
            queryMap.put("page", page.toString());
        }

        Call<String> a = InstaRetrofit.api.discoverSearch(queryMap);



        return InstaRetrofit.api.discoverSearch(queryMap);
    }

    public static Call<String> discoverUsers() {
        return InstaRetrofit.api.discoverUsers();
    }

    public static Call<String> discoverPhotos(@Nullable String seed) {

        HashMap<String, String> queryMap = new HashMap<>();
        if (seed != null) {
            queryMap.put("seed", seed);
        }
        return InstaRetrofit.api.discoverPhotos(queryMap);
    }

    public static Call<String> uploadPhoto(File file, String caption,
                                   @Nullable Double latitude, @Nullable Double longitude, @Nullable String locationName) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("caption", caption);
            if (latitude != null) {
                jsonObject.put("latitude", latitude);
            }
            if (longitude != null) {
                jsonObject.put("longitude", longitude);
            }
            if (locationName != null) {
                jsonObject.put("location_name", locationName);
            }
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        RequestBody photo = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        RequestBody jsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), jsonObject.toString());

        return InstaRetrofit.api.photoUpload(photo,jsonBody);
    }

    public static Call<String> specificPhotos(ArrayList<String> photoIDs) {

        JSONObject json = new JSONObject();
        JSONArray photoIDArray = new JSONArray();
        try {
            for(String photoID:photoIDs){
                if(!photoID.equals("")) {
                    photoIDArray.put(photoID);
                }
            }
            json.put("photo_ids", photoIDArray);

        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.specificPhotos(json.toString());
    }

    public static Call<String> getComments(String photoID, @Nullable String lastCommentFetched) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("photo_id", photoID);
        if (lastCommentFetched != null) {
            queryMap.put("last_comment_fetched", lastCommentFetched);
        }
        return InstaRetrofit.api.getComments(queryMap);
    }

    public static Call<String> newComment(String photoID, String text) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
            json.put("text", text);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.newComment(json.toString());
    }

    public static Call<String> deleteComment(String photoID, String commentID) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
            json.put("comment_id", commentID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.deleteComment(json.toString());
    }

    public static Call<String> getLikes(String photoID, @Nullable Integer recent, @Nullable String lastFetched) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("photo_id", photoID);
        if (recent != null) {
            queryMap.put("recent", recent.toString());
        }
        if (lastFetched != null) {
            queryMap.put("last_fetched", lastFetched);
        }
        return InstaRetrofit.api.getLikes(queryMap);
    }

    public static Call<String> likePhoto(String photoID) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.likePhoto(json.toString());
    }

    public static Call<String> unlikePhoto(String photoID) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.unlikePhoto(json.toString());
    }

    public static Call<String> getSelfActivity() {
        return InstaRetrofit.api.getSelfActivity();
    }

    public static Call<String> getFollowingActivity() {
        return InstaRetrofit.api.getFollowingActivity();
    }

    public static Call<String> getUserInfo(String displayName) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        return InstaRetrofit.api.getUserInfo(queryMap);
    }

    public static Call<String> getUserPhotos(String displayName, Sort sort, @Nullable Double latitude,@Nullable Double longitude,
            @Nullable String lastPhotoFetchedID) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        queryMap.put("sort", sort.toString());
        if(latitude!=null) {
            queryMap.put("latitude", latitude.toString());
        }
        if(longitude!=null) {
            queryMap.put("longitude", longitude.toString());
        }
        if(lastPhotoFetchedID!=null){
            queryMap.put("last_photo_fetched", lastPhotoFetchedID);
        }
        return InstaRetrofit.api.getUserPhotos(queryMap);
    }

    public static Call<String> followUser(String displayName) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.followUser(json.toString());
    }

    public static Call<String> unfollowUser(String displayName) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.unfollowUser(json.toString());
    }

    public static Call<String> getRequests() {
        return InstaRetrofit.api.getRequests();
    }

    public static Call<String> approveUser(String displayName) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        return InstaRetrofit.api.approveUser(json.toString());
    }

    public static Call<String> getFollowers(String displayName, @Nullable String lastFollowerFetchedName) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if(lastFollowerFetchedName!=null) {
            queryMap.put("last_follower_fetched", lastFollowerFetchedName);
        }
        return InstaRetrofit.api.getFollowers(queryMap);
    }

    public static Call<String> getFollowing(String displayName, @Nullable String lastFollowingFetchedName) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if(lastFollowingFetchedName!=null) {
            queryMap.put("last_following_fetched", lastFollowingFetchedName);
        }
        return InstaRetrofit.api.getFollowing(queryMap);
    }

    public static Call<String> getFollowingWhoFollow(String displayName, @Nullable String lastFetchedName) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if(lastFetchedName!=null) {
            queryMap.put("last_fetched", lastFetchedName);
        }
        return InstaRetrofit.api.getFollowing(queryMap);
    }

    public static Call<String> getDetails() {
        return InstaRetrofit.api.getDetails();
    }

    public static Call<String> updateDetails(@Nullable File profileImageFile, @Nullable String password, @Nullable String email,
                                     @Nullable String firstName, @Nullable String lastName, @Nullable String displayName,
                                     @Nullable String profileName, @Nullable String profileDesc, @Nullable Boolean isPrivate) {

        JSONObject jsonObject = new JSONObject();
        try {
            if (password != null) {
                jsonObject.put("password", password);
            }
            if (email != null) {
                jsonObject.put("email", email);
            }
            if (firstName != null) {
                jsonObject.put("first_name", firstName);
            }
            if (lastName != null) {
                jsonObject.put("last_name", lastName);
            }
            if (displayName != null) {
                jsonObject.put("display_name", displayName);
            }
            if (profileName != null) {
                jsonObject.put("profile_name", profileName);
            }
            if (profileDesc != null) {
                jsonObject.put("profile_desc", profileDesc);
            }
            if (isPrivate != null) {
                jsonObject.put("is_private", isPrivate);
            }
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        RequestBody jsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), jsonObject.toString());

        if(profileImageFile!=null){

            RequestBody photoFile = RequestBody.create(MediaType.parse("multipart/form-data"), profileImageFile);
//            MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", profileImageFile.getName(), requestFile);
            return InstaRetrofit.api.updateDetails(photoFile,jsonBody);
        } else {
            return InstaRetrofit.api.updateDetails(jsonBody);
        }
    }



    public static Callback<String> generateCallback(final Context context, final InstaApiCallback apiCallback){
        return new Callback<String>(){
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if(response!=null){

                    if(response.code() == 200) {

                        try {
                            String body = response.body();
                            JSONObject jsonResponse = new JSONObject(body);
                            boolean success = jsonResponse.optBoolean("success");
                            if (success) {
                                apiCallback.success(jsonResponse);
                            } else {
                                apiCallback.failure(context, jsonResponse);
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Invalid response from server", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.getMessage());
                        }
                    } else {
                        Toast.makeText(context, "HTTP "+response.code()+" error", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                apiCallback.networkFailure(context);
            }
        };
    }


    public enum Sort {
        DATE {
            @Override
            public String toString() {
                return "date";
            }
        }, LOCATION {
            @Override
            public String toString() {
                return "location";
            }
        }
    }
}
