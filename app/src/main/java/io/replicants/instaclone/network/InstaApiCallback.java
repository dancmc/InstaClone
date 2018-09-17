package io.replicants.instaclone.network;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONObject;

public abstract class InstaApiCallback {
    public abstract void success(JSONObject jsonResponse);

    public void failure(JSONObject jsonResponse){}

    public void networkFailure(Activity context){
        Toast.makeText(context, "Network Failure", Toast.LENGTH_SHORT).show();
    }
}