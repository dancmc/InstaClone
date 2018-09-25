package io.replicants.instaclone.utilities;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import io.replicants.instaclone.network.InstaApi;
import io.replicants.instaclone.network.InstaApiCallback;
import retrofit2.Call;
import retrofit2.Callback;

public class ExampleClass {

    public ExampleClass(Context context){

        // This is how you call the API

        // This returns a retrofit Call object - contains everything needed to make a network call
        // You actually run it by calling .execute (synchronous) or .enqueue (asynchronous) on it
        Call<String> call = InstaApi.discoverUsers();

        // We will be using only enqueue - this requires a retrofit2.Callback object passed in
        // However, I have created another class to help with the creation of this Callback object

        // to generate a Callback<String> object, just call InstaApi.generateCallback
        // this takes an instance of the abstract class InstaApiCallback
        // you only need to implement the success method. Failure and networkFailure have default implementations already
        // The success method passes you the JSONObject, and you can then get what info you need
        Callback<String> callback = InstaApi.generateCallback(context, new InstaApiCallback(){
            @Override
            public void success(JSONObject jsonResponse) {
                JSONArray array = jsonResponse.optJSONArray("activities");
                Log.d("EXAMPLE : ", jsonResponse.toString());
            }

            @Override
            public void failure(Context context, JSONObject jsonResponse) {
                super.failure(context, jsonResponse);
            }

            @Override
            public void networkFailure(Context context) {
                super.networkFailure(context);
            }
        });


        // Now just execute the asynchronous call, and the callback you created earlier will handle the result
        call.enqueue(callback);

    }
}
