package io.replicants.instaclone.subfragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.replicants.instaclone.R;
import io.replicants.instaclone.adapters.FeedAdapter;
import io.replicants.instaclone.adapters.YouAdapter;
import io.replicants.instaclone.network.InstaApi;
import io.replicants.instaclone.network.InstaApiCallback;
import io.replicants.instaclone.pojos.You;
import retrofit2.Call;
import retrofit2.Callback;

import static io.replicants.instaclone.pojos.You.youFromJsonArray;


public class YouSubFragment extends BaseSubFragment {

    private ArrayList<You> youFeedItems;
    private YouAdapter youAdapter;
    private SwipeRefreshLayout youRefresher;
//    private ProgressBar you_progressbar;


    public static YouSubFragment newInstance() {
        YouSubFragment fragment = new YouSubFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View you_layout = inflater.inflate(R.layout.subfragment_you, container, false);
//        you_progressbar = you_layout.findViewById(R.id.progressbar);

        RecyclerView you_recyclerView = you_layout.findViewById(R.id.subfragment_you_recycler);
        you_recyclerView.setHasFixedSize(true);
        you_recyclerView.setItemViewCacheSize(40);
        you_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        youFeedItems = new ArrayList<>();
        youAdapter = new YouAdapter(youFeedItems, you_recyclerView.getContext(), you_recyclerView);
        you_recyclerView.setAdapter(youAdapter);

        you_initialLoad();
        Log.i("follower_feedItems", String.valueOf(youFeedItems));


        youRefresher = you_layout.findViewById(R.id.subfragment_you_refresh);
        youRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                you_initialLoad();
            }
        });
        youRefresher.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark);

        youAdapter.setOnLoadMoreListener(new FeedAdapter.OnLoadMoreListener() {

            public void onLoadMore() {
                if (youFeedItems.size() <= 20) {
                    youFeedItems.add(null);
                    youAdapter.notifyItemInserted(youFeedItems.size() - 1);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        youFeedItems.remove(youFeedItems.size() - 1);
                        youAdapter.notifyItemRemoved(youFeedItems.size());

                        //Generating more data
                        you_loadmore();
                        youAdapter.setLoaded();
                    }
                }, 2000);
            }
        });

        Log.i("you_recycler", "finish");

        return you_layout;
    }

    public void you_initialLoad() {
        Call<String> call1 = InstaApi.getSelfActivity();

        Callback<String> callback1 = InstaApi.generateCallback(getContext(), new InstaApiCallback() {
            @Override
            public void success(JSONObject jsonResponse) {
                JSONArray followerActivityArray = jsonResponse.optJSONArray("activities");
                try {

                    youFeedItems = youFromJsonArray(followerActivityArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("EXAMPLE_You : ", jsonResponse.toString());

                youAdapter.youSetData(youFeedItems);
                youRefresher.setRefreshing(false);
                Log.i("you: ", followerActivityArray.toString());
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

        call1.enqueue(callback1);

    }


    public void you_loadmore() {
        Call<String> call = InstaApi.getSelfActivity();
        Callback<String> callback = InstaApi.generateCallback(getContext(), new InstaApiCallback() {
            @Override
            public void success(JSONObject jsonResponse) {
                JSONArray followerActivityArray = jsonResponse.optJSONArray("activities");

                JSONArray moreFollowerActivityArray = new JSONArray();
                int start = youFeedItems.size();
                int end = start + 20;
                try {

                    for (int i = start; i < end; i++) {
                        moreFollowerActivityArray.put(followerActivityArray.get(i));
                        i++;
                    }

                    youFeedItems.addAll(youFromJsonArray(moreFollowerActivityArray));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("Following_more: ", jsonResponse.toString());
//                Log.i("feeditem: ", followingFeedItems.toString());

                youAdapter.youSetData(youFeedItems);
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

        call.enqueue(callback);

    }

}



