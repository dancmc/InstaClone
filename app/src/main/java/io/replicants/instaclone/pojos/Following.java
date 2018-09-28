package io.replicants.instaclone.pojos;

import android.util.Log;
import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Following {

    public int typeId;
    public long timestamp;
    public String display_name;
    public String profile_image;
    public int total_liked;
    public String photoLiked_id;
    public String photoLiked_url;
    public Map<String, String> somePhotosLiked_url = new HashMap<>();
    public int total_followed;
    public String usersFollowed_displayName;
    public String usersFollowed_profileImg;
    public String usersFollowed_profileName;
    public boolean usersFollowed_areFollowing;
    public String previewUser_displayName;
    public String previewUser_profileImg;


    public static ArrayList<Following> followingFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Following> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject following = jsonArray.getJSONObject(i);
            if (following != null) {
                results.addAll(followingParseFromJson(following));
            }
        }
        return results;
    }


    public static ArrayList<Following> followingParseFromJson(JSONObject jsonObject) throws JSONException {
        ArrayList<Following> followingList = new ArrayList<>();

        if (jsonObject.optInt("type") == 1) {

            JSONArray photosLike = jsonObject.optJSONArray("photos_liked");
            if (photosLike == null) {
                return followingList;
            }
            Following following = new Following();
            following.typeId = jsonObject.optInt("type");
            following.timestamp = jsonObject.optLong("timestamp");
            following.display_name = jsonObject.optString("display_name");
            following.profile_image = jsonObject.optString("profile_image");
            following.total_liked = jsonObject.optInt("total_liked");
            for (int i = 0; i < photosLike.length(); i++) {
                String photoId = following.photoLiked_id = photosLike.getJSONObject(i).optString("photo_id");
                String photoUrl = following.photoLiked_url = photosLike.getJSONObject(i).getJSONObject("url").getJSONObject("thumb").optString("link");
                following.somePhotosLiked_url.put(photoId, photoUrl);
            }

        } else if (jsonObject.optInt("type") == 2) {

            JSONArray previewUsers = jsonObject.optJSONArray("preview_users");
            if (previewUsers == null) {
                return followingList;
            }
            for (int i = 0; i < previewUsers.length(); i++) {
                Following following = new Following();
                following.typeId = jsonObject.optInt("type");
                following.timestamp = jsonObject.optLong("timestamp");
                following.previewUser_displayName = previewUsers.getJSONObject(i).optString("display_name");
                following.previewUser_profileImg = previewUsers.getJSONObject(i).optString("profile_image");
                following.total_liked = jsonObject.optInt("total_liked");
                following.photoLiked_id = jsonObject.optString("photo_id");
                following.photoLiked_url = jsonObject.getJSONObject("url").getJSONObject("thumb").optString("link");
                followingList.add(following);
            }

        } else if (jsonObject.optInt("type") == 3) {

            JSONArray usersFollowed = jsonObject.optJSONArray("users_followed");
            if (usersFollowed == null) {
                return followingList;
            }
            for (int i = 0; i < usersFollowed.length(); i++) {
                Following following = new Following();
                following.typeId = jsonObject.optInt("type");
                following.timestamp = jsonObject.optLong("timestamp");
                following.display_name = jsonObject.optString("display_name");
                following.profile_image = jsonObject.optString("profile_image");
                following.total_followed = jsonObject.optInt("total_followed");
                following.usersFollowed_displayName = usersFollowed.getJSONObject(i).optString("display_name");
                following.usersFollowed_profileImg = usersFollowed.getJSONObject(i).optString("profile_image");
                following.usersFollowed_profileName = usersFollowed.getJSONObject(i).optString("profile_name");
                following.usersFollowed_areFollowing = usersFollowed.getJSONObject(i).optBoolean("are_following");
                followingList.add(following);
                Log.d("following: ", following.usersFollowed_profileImg);
            }

        }

        return followingList;
    }
}
