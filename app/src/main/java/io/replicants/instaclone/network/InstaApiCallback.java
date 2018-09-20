package io.replicants.instaclone.network;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

public abstract class InstaApiCallback {
    public abstract void success(JSONObject jsonResponse);

    public void failure(Context context, JSONObject jsonResponse){
        Toast.makeText(context, jsonResponse.optString("error_message", "Server returned unknown failure"), Toast.LENGTH_SHORT).show();
    }

    public void networkFailure(Context context){
        Toast.makeText(context, "Network Failure", Toast.LENGTH_SHORT).show();
    }
}