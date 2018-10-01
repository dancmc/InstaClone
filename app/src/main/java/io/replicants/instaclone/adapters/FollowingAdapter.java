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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.replicants.instaclone.R;
import io.replicants.instaclone.pojos.Following;
import io.replicants.instaclone.utilities.FollowingBaseViewHolder;
import io.replicants.instaclone.utilities.GlideHeader;
import io.replicants.instaclone.utilities.Utils;


public class FollowingAdapter extends RecyclerView.Adapter<FollowingBaseViewHolder> {
    private ArrayList<Following> followingActivityList;
    RecyclerView recyclerView;
    Context context;
    private int visibleThreshold = 6;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private final int VIEW_TYPE_LIKE_PHOTOS6 = 0;
    private final int VIEW_TYPE_LIKE_PHOTOS10 = 1;
    private final int VIEW_TYPE_LIKE_A_PHOTO = 2;
    private final int VIEW_TYPE_START_FOLLOW = 3;
    private final int VIEW_TYPE_TOTAL = 4;
    private FeedAdapter.OnLoadMoreListener onLoadMoreListener;

    public FollowingAdapter(ArrayList<Following> followingActivityList, Context context, RecyclerView recyclerView) {
        this.followingActivityList = followingActivityList;
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
        if (followingActivityList.get(position).typeId == 1 && followingActivityList.get(position).total_liked <= 6) {
            return VIEW_TYPE_LIKE_PHOTOS6;
        } else if (followingActivityList.get(position).typeId == 1 && followingActivityList.get(position).total_liked <= 10) {
            return VIEW_TYPE_LIKE_PHOTOS10;
        } else if (followingActivityList.get(position).typeId == 2) {
            return VIEW_TYPE_LIKE_A_PHOTO;
        } else if (followingActivityList.get(position).typeId == 3) {
            return VIEW_TYPE_START_FOLLOW;
        } else {
            return VIEW_TYPE_TOTAL;
        }
    }

    public FollowingBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LIKE_PHOTOS6) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_like_follow, parent, false);
            return new ViewHolderType1Following(v);

        } else if (viewType == VIEW_TYPE_LIKE_PHOTOS10) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_like_follow_more, parent, false);
            return new ViewHolderType1Following(v);

        } else if (viewType == VIEW_TYPE_LIKE_A_PHOTO) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_like_one, parent, false);
            return new ViewHolderType2Following(v);
        } else  {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_like_one, parent, false);
            return new ViewHolderType3Following(v);
        }
//        if (viewType == VIEW_TYPE_START_FOLLOW)else {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
//            return new ProgressViewHolder(view);
//        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FollowingBaseViewHolder holder, int position) {

        if (holder instanceof FollowingBaseViewHolder) {
            holder.bind(followingActivityList, position, context);
        }
//        else {
//            if (loading) {
//                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
//                ((ProgressViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
//            }
//        }
        Log.i("following_bind", "profile");
    }


    public static class ViewHolderType1Following extends FollowingBaseViewHolder {

        TextView post_text;
        ImageView left_image_url;
        ImageView postImage1, postImage2, postImage3, postImage4, postImage5, postImage6, postImage7, postImage8, postImage9, postImage10;

        public ViewHolderType1Following(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_following_image);
            post_text = itemView.findViewById(R.id.following_activity);
            postImage1 = itemView.findViewById(R.id.post_image1);
            postImage2 = itemView.findViewById(R.id.post_image2);
            postImage3 = itemView.findViewById(R.id.post_image3);
            postImage4 = itemView.findViewById(R.id.post_image4);
            postImage5 = itemView.findViewById(R.id.post_image5);
            postImage6 = itemView.findViewById(R.id.post_image6);
            postImage7 = itemView.findViewById(R.id.post_image7);
            postImage8 = itemView.findViewById(R.id.post_image8);
            postImage9 = itemView.findViewById(R.id.post_image9);
            postImage10 = itemView.findViewById(R.id.post_image10);

        }

        public void bind(ArrayList<Following> followingActivityList, int position, Context context) {
            Following feeditem = followingActivityList.get(position);
            Glide.with(left_image_url.getContext()).load(GlideHeader.getUrlWithHeaders(feeditem.profile_image)).into(left_image_url);

            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String followerName = feeditem.display_name;
            String numPhotosLiked = new Integer(feeditem.total_liked).toString();
            SpannableStringBuilder textDisplay = new SpannableStringBuilder(followerName + " liked " + numPhotosLiked + " posts. " + timeDiff);
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay.setSpan(b, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(textDisplay);

            int numPhoto = 1;
            for (Map.Entry<String, String> photo : feeditem.somePhotosLiked_url.entrySet()) {
                switch (numPhoto) {
                    case 1:
                        Glide.with(postImage1.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage1);
                        numPhoto++;
                        break;
                    case 2:
                        Glide.with(postImage2.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage2);
                        numPhoto++;
                        break;
                    case 3:
                        Glide.with(postImage4.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage4);
                        numPhoto++;
                        break;
                    case 4:
                        Glide.with(postImage4.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage4);
                        numPhoto++;
                        break;
                    case 5:
                        Glide.with(postImage5.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage5);
                        numPhoto++;
                        break;
                    case 6:
                        Glide.with(postImage6.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage6);
                        numPhoto++;
                        break;
                    case 7:
                        Glide.with(postImage7.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage7);
                        numPhoto++;
                        break;
                    case 8:
                        Glide.with(postImage8.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage8);
                        numPhoto++;
                        break;
                    case 9:
                        Glide.with(postImage9.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage9);
                        numPhoto++;
                        break;
                    case 10:
                        Glide.with(postImage10.getContext()).load(GlideHeader.getUrlWithHeaders(photo.getValue())).into(postImage10);
                        numPhoto++;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static class ViewHolderType2Following extends FollowingBaseViewHolder {

        TextView post_text;
        ImageView left_image_url;
        ImageView right_image_url;

        public ViewHolderType2Following(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_following_image);
            post_text = itemView.findViewById(R.id.following_activity);
            right_image_url = itemView.findViewById(R.id.right_following_image);

        }

        public void bind(ArrayList<Following> followingActivityList, int position, Context context) {
            Following feeditem = followingActivityList.get(position);

            Glide.with(left_image_url.getContext()).load(GlideHeader.getUrlWithHeaders(feeditem.previewUser_profileImg)).into(left_image_url);

            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String followerName = feeditem.previewUser_displayName;
            SpannableStringBuilder textDisplay = new SpannableStringBuilder(followerName + " liked a post. " + timeDiff);
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay.setSpan(b, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(textDisplay);

            Glide.with(right_image_url.getContext()).load(GlideHeader.getUrlWithHeaders(feeditem.photoLiked_url)).into(right_image_url);

        }
    }

    public static class ViewHolderType3Following extends FollowingBaseViewHolder {

        TextView post_text;
        ImageView left_image_url;
        ImageView right_image_url;

        public ViewHolderType3Following(View itemView) {
            super(itemView);
            // initialize the View objects
            left_image_url = itemView.findViewById(R.id.left_following_image);
            post_text = itemView.findViewById(R.id.following_activity);
            right_image_url = itemView.findViewById(R.id.right_following_image);

        }

        public void bind(ArrayList<Following> followingActivityList, int position, Context context) {
            Following feeditem = followingActivityList.get(position);

            Glide.with(left_image_url.getContext()).load(GlideHeader.getUrlWithHeaders(feeditem.profile_image)).into(left_image_url);

            String timeDiff = Utils.formatDate(feeditem.timestamp);
            String followerName = feeditem.display_name;
            String followingName = feeditem.usersFollowed_displayName;
            SpannableStringBuilder textDisplay1 = new SpannableStringBuilder(followerName + " started following ");
            SpannableStringBuilder textDisplay2 = new SpannableStringBuilder(followingName + ". " + timeDiff);
            StyleSpan b1 = new StyleSpan(android.graphics.Typeface.BOLD);
            StyleSpan b2 = new StyleSpan(android.graphics.Typeface.BOLD);
            textDisplay1.setSpan(b1, 0, followerName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            textDisplay2.setSpan(b2, 0, followingName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            post_text.setText(TextUtils.concat(textDisplay1, textDisplay2));

            Glide.with(right_image_url.getContext()).load(GlideHeader.getUrlWithHeaders(feeditem.usersFollowed_profileImg)).into(right_image_url);


        }

    }


//    class ProgressViewHolder extends FollowingBaseViewHolder {
//        public ProgressBar progressBar;
//
//        public ProgressViewHolder(View itemView) {
//            super(itemView);
//            progressBar = itemView.findViewById(R.id.progressbar);
//        }
//
//        public void bind(ArrayList<Following> followingActivityList, int position, Context context) {
//
//        }
//
//    }

    @Override
    public int getItemCount() {
        if (followingActivityList == null) {
            return 0;
        }
        return followingActivityList.size();
    }

    public void followingSetData(ArrayList<Following> followingActivityList) {
        if (followingActivityList == null) {
            return;
        }
        this.followingActivityList.clear();
        this.followingActivityList.addAll(followingActivityList);
        notifyDataSetChanged();
        loading=false;
    }

    public void setLoaded() {
        loading = false;
    }


}
