package io.replicants.instaclone.network;

import android.app.Activity;
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

    public static void userRegister(
            String username, String password,
            @Nullable String firstName, String lastName, String displayName, String email,
            Callback<String> callback) {


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

        InstaRetrofit.Companion.getApi().userRegister(json.toString()).enqueue(callback);
    }

    public static void userLogin(
            String username, String password,
            Callback<String> callback) {


        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);

        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().userLogin(json.toString()).enqueue(callback);
    }

    public static void validate(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().validate().enqueue(callback);
    }

    public static void getFeed(
            Sort sort, @Nullable Double latitude, @Nullable Double longitude, @Nullable String lastPhotoID,
            Callback<String> callback) {


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

        InstaRetrofit.Companion.getApi().getFeed(queryMap).enqueue(callback);
    }

    public static void discoverSearch(
            String displayName, @Nullable Integer page, Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if (page != null && page != 0) {
            queryMap.put("page", page.toString());
        }

        InstaRetrofit.Companion.getApi().discoverSearch(queryMap).enqueue(callback);
    }

    public static void discoverUsers(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().discoverUsers().enqueue(callback);
    }

    public static void discoverPhotos(@Nullable String seed, Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        if (seed != null) {
            queryMap.put("seed", seed);
        }
        InstaRetrofit.Companion.getApi().discoverPhotos(queryMap).enqueue(callback);
    }

    public static void uploadPhoto(File file, String caption,
                                   @Nullable Double latitude, @Nullable Double longitude, @Nullable String locationName,
                                   Callback<String> callback) {

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

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        RequestBody jsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), jsonObject.toString());

        InstaRetrofit.Companion.getApi().photoUpload(photo,jsonBody).enqueue(callback);
    }

    public static void specificPhotos(ArrayList<String> photoIDs, Callback<String> callback) {

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

        InstaRetrofit.Companion.getApi().specificPhotos(json.toString()).enqueue(callback);
    }

    public static void getComments(String photoID, @Nullable String lastCommentFetched, Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("photo_id", photoID);
        if (lastCommentFetched != null) {
            queryMap.put("last_comment_fetched", lastCommentFetched);
        }
        InstaRetrofit.Companion.getApi().getComments(queryMap).enqueue(callback);
    }

    public static void newComment(String photoID, String text, Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
            json.put("text", text);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().newComment(json.toString()).enqueue(callback);
    }

    public static void deleteComment(String photoID, String commentID, Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
            json.put("comment_id", commentID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().deleteComment(json.toString()).enqueue(callback);
    }

    public static void getLikes(String photoID, @Nullable Integer recent, @Nullable Long lastLikeTimestamp, Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("photo_id", photoID);
        if (recent != null) {
            queryMap.put("recent", recent.toString());
        }
        if (lastLikeTimestamp != null) {
            queryMap.put("last_like_timestamp", lastLikeTimestamp.toString());
        }
        InstaRetrofit.Companion.getApi().getLikes(queryMap).enqueue(callback);
    }

    public static void likePhoto(String photoID,Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().likePhoto(json.toString()).enqueue(callback);
    }

    public static void unlikePhoto(String photoID,Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("photo_id", photoID);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().unlikePhoto(json.toString()).enqueue(callback);
    }

    public static void getSelfActivity(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().getSelfActivity().enqueue(callback);
    }

    public static void getFollowingActivity(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().getFollowingActivity().enqueue(callback);
    }

    public static void getUserInfo(String displayName, Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        InstaRetrofit.Companion.getApi().getUserInfo(queryMap).enqueue(callback);
    }

    public static void getUserPhotos(String displayName, Sort sort, @Nullable Double latitude,@Nullable Double longitude,
            @Nullable String lastPhotoFetchedID, Callback<String> callback) {

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
        InstaRetrofit.Companion.getApi().getUserPhotos(queryMap).enqueue(callback);
    }

    public static void followUser(String displayName,Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().followUser(json.toString()).enqueue(callback);
    }

    public static void unfollowUser(String displayName,Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().unfollowUser(json.toString()).enqueue(callback);
    }

    public static void getRequests(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().getRequests().enqueue(callback);
    }

    public static void approveUser(String displayName,Callback<String> callback) {

        JSONObject json = new JSONObject();
        try {
            json.put("display_name", displayName);
        } catch (JSONException j) {
            Log.d(TAG, j.getMessage());
        }

        InstaRetrofit.Companion.getApi().approveUser(json.toString()).enqueue(callback);
    }

    public static void getFollowers(String displayName, @Nullable String lastFollowerFetchedName,
                                    Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if(lastFollowerFetchedName!=null) {
            queryMap.put("last_follower_fetched", lastFollowerFetchedName);
        }
        InstaRetrofit.Companion.getApi().getFollowers(queryMap).enqueue(callback);
    }

    public static void getFollowing(String displayName, @Nullable String lastFollowingFetchedName,
                                    Callback<String> callback) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("display_name", displayName);
        if(lastFollowingFetchedName!=null) {
            queryMap.put("last_following_fetched", lastFollowingFetchedName);
        }
        InstaRetrofit.Companion.getApi().getFollowing(queryMap).enqueue(callback);
    }

    public static void getDetails(Callback<String> callback) {
        InstaRetrofit.Companion.getApi().getDetails().enqueue(callback);
    }

    public static void updateDetails(@Nullable File profileImageFile, @Nullable String password, @Nullable String email,
                                     @Nullable String firstName, @Nullable String lastName, @Nullable String displayName,
                                     @Nullable String profileName, @Nullable String profileDesc, @Nullable Boolean isPrivate,
                                   Callback<String> callback) {

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
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), profileImageFile);
            MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", profileImageFile.getName(), requestFile);
            InstaRetrofit.Companion.getApi().updateDetails(photo,jsonBody).enqueue(callback);
        } else {
            InstaRetrofit.Companion.getApi().updateDetails(jsonBody).enqueue(callback);
        }
    }



    public static Callback<String> generateCallback(final Activity context, final InstaApiCallback apiCallback){
        return new Callback<String>(){
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response!=null){
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        boolean success = jsonResponse.optBoolean("success");
                        if(success){
                            apiCallback.success(jsonResponse);
                        }else {
                            apiCallback.failure(jsonResponse);
                        }
                    }catch (JSONException e){
                        Toast.makeText(context, "Invalid response from server", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
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
