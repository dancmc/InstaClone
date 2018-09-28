package io.replicants.instaclone.pojos;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class You {

    public int typeId;
    public long timestamp;
    public String unknownFollower_displayName;
    public String unknownFollower_profileImg;
    public String unknownFollower_profileName;
    public boolean areFollowing;
    public String photoLiked_id;
    public String photoLiked_url;
    public String previewUser_displayName;
    public String previewUser_profileImg;
    public int recent_likess;
    public int recent_commnets;
    public String previewComment_displayName;
    public String previewComment_profileImg;
    public String previewComment_text;
    public String request_displayName;
    public String request_profileImg;
    public String request_profileName;


    public static ArrayList<You> youFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<You> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject follower = jsonArray.getJSONObject(i);
            results.addAll(youParseFromJson(follower));
        }
        return results;
    }


    public static ArrayList<You> youParseFromJson(JSONObject jsonObject) throws JSONException {
        ArrayList<You> youList = new ArrayList<>();

        if (jsonObject.optInt("type") == 1) {

            JSONArray users = jsonObject.optJSONArray("users");
            if (users == null) {
                return youList;
            }
            for (int i = 0; i < users.length(); i++) {
                You follower = new You();

                follower.typeId = jsonObject.optInt("type");
                follower.timestamp = jsonObject.optLong("timestamp");
                follower.unknownFollower_displayName = users.getJSONObject(i).optString("display_name");
                follower.unknownFollower_profileImg = users.getJSONObject(i).optString("profile_image");
                follower.unknownFollower_profileName = users.getJSONObject(i).optString("profile_name");
                follower.areFollowing = users.getJSONObject(i).optBoolean("are_following");
                youList.add(follower);
            }
        } else if (jsonObject.optInt("type") == 2) {
            JSONArray users = jsonObject.optJSONArray("preview_users");
            if (users == null) {
                return youList;
            }
            for (int i = 0; i < users.length(); i++) {
                You follower = new You();
                follower.typeId = jsonObject.optInt("type");
                follower.timestamp = jsonObject.optLong("timestamp");
                follower.photoLiked_id = jsonObject.optString("photo_id");
                follower.photoLiked_url = jsonObject.getJSONObject("url").getJSONObject("thumb").optString("link");
                follower.recent_likess = jsonObject.optInt("recent_likes");
                follower.previewUser_displayName = users.getJSONObject(i).optString("display_name");
                follower.previewUser_profileImg = users.getJSONObject(i).optString("profile_image");
                youList.add(follower);
            }

        } else if (jsonObject.optInt("type") == 3) {
            JSONArray users = jsonObject.optJSONArray("preview_comment");
            if (users == null) {
                return youList;
            }
            for (int i = 0; i < users.length(); i++) {
                You follower = new You();
                follower.typeId = jsonObject.optInt("type");
                follower.timestamp = jsonObject.optLong("timestamp");
                follower.photoLiked_id = jsonObject.optString("photo_id");
                follower.photoLiked_url = jsonObject.getJSONObject("url").getJSONObject("thumb").optString("link");
                follower.recent_commnets = jsonObject.getInt("recent_comments");
                follower.previewComment_displayName = users.getJSONObject(i).optString("display_name");
                follower.previewComment_profileImg = users.getJSONObject(i).optString("profile_image");
                follower.previewComment_text = users.getJSONObject(i).optString("comment_text");
                youList.add(follower);
            }
        } else {
            JSONArray users = jsonObject.optJSONArray("requests");
            if (users == null) {
                return youList;
            }
            for (int i = 0; i < users.length(); i++) {
                You follower = new You();
                follower.typeId = jsonObject.optInt("type");
                follower.timestamp = users.getJSONObject(i).optLong("timestamp");
                follower.request_displayName = users.getJSONObject(i).optString("display_name");
                follower.request_profileName = users.getJSONObject(i).optString("profile_name");
                follower.request_profileImg = users.getJSONObject(i).optString("profile_image");
                follower.areFollowing = users.getJSONObject(i).optBoolean("are_following");
                youList.add(follower);
            }
        }
        Log.d("youDebug: ", youList.toString());

        return youList;
    }
}
