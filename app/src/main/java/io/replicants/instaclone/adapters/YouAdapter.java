package io.replicants.instaclone.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.replicants.instaclone.R;
import io.replicants.instaclone.pojos.Following;
import io.replicants.instaclone.pojos.You;
import io.replicants.instaclone.utilities.FollowingBaseViewHolder;
import io.replicants.instaclone.utilities.Utils;
import io.replicants.instaclone.utilities.YouBaseViewHolder;


public class YouAdapter extends RecyclerView.Adapter<YouBaseViewHolder> {
    private ArrayList<You> followerActivityList;
    RecyclerView recyclerView;
    Context context;
    private int visibleThreshold = 6;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private final int VIEW_TYPE_UNKNOWN_FOLLOW = 0;
    private final int VIEW_TYPE_FOLLOW_REQUEST = 1;
    private final int VIEW_TYPE_LIKE_POST = 2;
    private final int VIEW_TYPE_COMMENT_POST = 3;
    private final int VIEW_TYPE_TOTAL = 4;
    private FeedAdapter.OnLoadMoreListener onLoadMoreListener;

    public YouAdapter(ArrayList<You> followerActivityList, Context context, RecyclerView recyclerView) {
        this.followerActivityList = followerActivityList;
        this.recyclerView = recyclerView;
        this.context = context;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public void setOnLoadMoreListener(FeedAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (followerActivityList.get(position).typeId == 1) {
            return VIEW_TYPE_UNKNOWN_FOLLOW;
        } else if (followerActivityList.get(position).typeId == 2) {
            return VIEW_TYPE_LIKE_POST;
        } else if (followerActivityList.get(position).typeId == 3) {
            return VIEW_TYPE_COMMENT_POST;
        } else if (followerActivityList.get(position).typeId == 4) {
            return VIEW_TYPE_FOLLOW_REQUEST;
        } else {
            return VIEW_TYPE_TOTAL;
        }
    }

    public YouBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_UNKNOWN_FOLLOW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.you_unknown_follow, parent, false);
            return new StartFollowHolder(v);
        } else if (viewType == VIEW_TYPE_LIKE_POST) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.you_like, parent, false);
            return new LikePostHolder(v);
        } else if (viewType == VIEW_TYPE_COMMENT_POST) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.you_like, parent, false);
            return new CommentPostHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.you_follow_request, parent, false);
            return new RequestFollowHolder(v);
        }
//        (viewType == VIEW_TYPE_FOLLOW_REQUEST) else{
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
//            return new ProgressViewHolder(view);
//        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public void onBindViewHolder(YouBaseViewHolder holder, final int position) {

        if (holder instanceof YouBaseViewHolder) {
            holder.bind(followerActivityList, position, context);
        }
//            else {
//                //check data completion
//                //if value is true, change the visibility of progressbar to gone to identify the user
//                if (loading) {
//                    ((ProgressViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
//                    ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
//                } else {
//                    ((ProgressViewHolder) holder).progressBar.setVisibility(View.GONE);
//                }
//            }

        Log.i("you_bind", "profile");
    }


    public static class StartFollowHolder extends YouBaseViewHolder {

        TextView post_text;
        ImageView left_image_url;
        Button follow_button;

        public StartFollowHolder(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_follower_image);
            post_text = itemView.findViewById(R.id.follower_activity);
            follow_button = itemView.findViewById(R.id.follow_button);
        }

        public void bind(ArrayList<You> followingActivityList, int position, Context context) {
            You feeditem = followingActivityList.get(position);

            if (feeditem.unknownFollower_profileImg != null) {
                Glide.with(left_image_url.getContext()).load(feeditem.unknownFollower_profileImg).into(left_image_url);
            }
            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String unknownFollowerName = feeditem.unknownFollower_displayName;
            SpannableStringBuilder textDisplay = new SpannableStringBuilder(unknownFollowerName + " started following you. " + timeDiff);
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay.setSpan(b, 0, unknownFollowerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(textDisplay);

        }
    }

    public static class LikePostHolder extends YouBaseViewHolder {
        TextView post_text;
        ImageView left_image_url;
        ImageView right_image_url;

        public LikePostHolder(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_follower_image);
            post_text = itemView.findViewById(R.id.follower_activity);
            right_image_url = itemView.findViewById(R.id.right_follower_image);
        }

        public void bind(ArrayList<You> followingActivityList, int position, Context context) {
            You feeditem = followingActivityList.get(position);

            if (feeditem.previewUser_profileImg != null) {
                Glide.with(left_image_url.getContext()).load(feeditem.previewUser_profileImg).into(left_image_url);
            }
            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String followerName = feeditem.previewUser_displayName;
            SpannableStringBuilder textDisplay = new SpannableStringBuilder(followerName + " liked your post. " + timeDiff);
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay.setSpan(b, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(textDisplay);
            if (feeditem.photoLiked_url != null) {
                Glide.with(right_image_url.getContext()).load(feeditem.photoLiked_url).into(right_image_url);
            }
        }
    }

    public static class CommentPostHolder extends YouBaseViewHolder {
        TextView post_text;
        ImageView left_image_url;
        ImageView right_image_url;

        public CommentPostHolder(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_follower_image);
            post_text = itemView.findViewById(R.id.follower_activity);
            right_image_url = itemView.findViewById(R.id.right_follower_image);
        }

        public void bind(ArrayList<You> followingActivityList, int position, Context context) {
            You feeditem = followingActivityList.get(position);

            if (feeditem.previewComment_profileImg != null) {
                Glide.with(left_image_url.getContext()).load(feeditem.previewComment_profileImg).into(left_image_url);
            }
            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String followerName = feeditem.previewComment_displayName;
            SpannableStringBuilder textDisplay = new SpannableStringBuilder(followerName + " left a comment on your post. " + timeDiff);
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay.setSpan(b, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(textDisplay);
            if (feeditem.photoLiked_url != null) {
                Glide.with(right_image_url.getContext()).load(feeditem.photoLiked_url).into(right_image_url);
            }
        }
    }

    public static class RequestFollowHolder extends YouBaseViewHolder {
        TextView post_text;
        ImageView left_image_url;
        TextView request_text;

        public RequestFollowHolder(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_follower_image);
            post_text = itemView.findViewById(R.id.follower_activity);
            request_text = itemView.findViewById(R.id.request_activity);
        }

        public void bind(ArrayList<You> followingActivityList, int position, Context context) {
            You feeditem = followingActivityList.get(position);

            if (feeditem.request_profileImg != null) {
                Glide.with(left_image_url.getContext()).load(feeditem.request_profileImg).into(left_image_url);
            }
            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String requestHead = "Follow Request";
            SpannableStringBuilder textDisplay1 = new SpannableStringBuilder(requestHead + " from ");
            StyleSpan b1 = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay1.setSpan(b1, 0, requestHead.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            String followerName = feeditem.request_displayName;
            SpannableStringBuilder textDisplay2 = new SpannableStringBuilder(followerName + "." + timeDiff);
            StyleSpan b2 = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay2.setSpan(b2, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(TextUtils.concat(textDisplay1, textDisplay2));

            String msg = "Approve or ignore requests";
            request_text.setText(msg);
        }
    }

//        class ProgressViewHolder extends YouBaseViewHolder {
//            public ProgressBar progressBar;
//
//            public ProgressViewHolder(View itemView) {
//                super(itemView);
//                progressBar = itemView.findViewById(R.id.progressbar);
//            }
//
//            @Override
//            public void bind(ArrayList<You> followingActivityList, int position, Context context) {
//
//            }
//
//        }

    public void youSetData(ArrayList<You> followerActivityList) {
        if (followerActivityList == null) {
            return;
        }
        this.followerActivityList.clear();
        this.followerActivityList.addAll(followerActivityList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (followerActivityList == null) {
            return 0;
        }
        return followerActivityList.size();
    }

    public void setLoaded() {
        loading = false;
    }


}
