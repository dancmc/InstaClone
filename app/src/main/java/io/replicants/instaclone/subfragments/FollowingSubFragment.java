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
import io.replicants.instaclone.adapters.FollowingAdapter;
import io.replicants.instaclone.network.InstaApi;
import io.replicants.instaclone.network.InstaApiCallback;
import io.replicants.instaclone.pojos.Following;
import retrofit2.Call;
import retrofit2.Callback;

import static io.replicants.instaclone.pojos.Following.followingFromJsonArray;

public class FollowingSubFragment extends BaseSubFragment {

    private ArrayList<Following> followingFeedItems;
    FollowingAdapter followingAdapter;
    SwipeRefreshLayout followingRefresher;
//    ProgressBar progressbar;


    public static FollowingSubFragment newInstance() {
        FollowingSubFragment fragment = new FollowingSubFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);


        return fragment;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = LayoutInflater.from(container.getContext()).inflate(R.layout.subfragment_following, container, false);
        RecyclerView recyclerView = layout.findViewById(R.id.subfragment_following_recycler);
//        progressbar = layout.findViewById(R.id.progressbar);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(40);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        followingFeedItems = new ArrayList<>();
        followingAdapter = new FollowingAdapter(followingFeedItems, recyclerView.getContext(), recyclerView);
        recyclerView.setAdapter(followingAdapter);


        following_initialLoad();
        Log.i("feedItems2", String.valueOf(followingFeedItems));

        followingRefresher = layout.findViewById(R.id.subfragment_following_refresh);
        followingRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                following_initialLoad();
            }
        });

        followingRefresher.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark);
        Log.i("recycler", "finish");

        followingAdapter.setOnLoadMoreListener(new FeedAdapter.OnLoadMoreListener() {

            public void onLoadMore() {
                if (followingFeedItems.size() <= 20) {
                    followingFeedItems.add(null);
                    followingAdapter.notifyItemInserted(followingFeedItems.size() - 1);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        followingFeedItems.remove(followingFeedItems.size() - 1);
                        followingAdapter.notifyItemRemoved(followingFeedItems.size());

                        //Generating more data
                        following_loadmore();
                        followingAdapter.setLoaded();
                    }
                }, 2000);
            }
        });

        return layout;
    }


    public void following_initialLoad() {
        Call<String> call = InstaApi.getFollowingActivity();

        Callback<String> callback = InstaApi.generateCallback(getContext(), new InstaApiCallback() {
            @Override
            public void success(JSONObject jsonResponse) {
                JSONArray followingActivityArray = jsonResponse.optJSONArray("activities");
                try {

                    followingFeedItems = followingFromJsonArray(followingActivityArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("EXAMPLE_Following : ", jsonResponse.toString());
//                Log.i("feeditem: ", followingFeedItems.toString());

                followingAdapter.followingSetData(followingFeedItems);
                followingRefresher.setRefreshing(false);

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

    public void following_loadmore() {
        Call<String> call = InstaApi.getFollowingActivity();
        Callback<String> callback = InstaApi.generateCallback(getContext(), new InstaApiCallback() {
            @Override
            public void success(JSONObject jsonResponse) {
                JSONArray followingActivityArray = jsonResponse.optJSONArray("activities");

                JSONArray moreFollowingActivityArray = new JSONArray();
                int start = followingFeedItems.size();
                int end = start + 20;
                try {
                    for (int i = start; i < end; i++) {
                        moreFollowingActivityArray.put(followingActivityArray.get(i));
                    }

                    followingFeedItems.addAll(followingFromJsonArray(moreFollowingActivityArray));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Following_more: ", jsonResponse.toString());
//                Log.i("feeditem: ", followingFeedItems.toString());

                followingAdapter.followingSetData(followingFeedItems);
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



