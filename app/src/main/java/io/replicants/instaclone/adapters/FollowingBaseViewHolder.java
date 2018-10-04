package io.replicants.instaclone.adapters;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import io.replicants.instaclone.pojos.Following;

public abstract class FollowingBaseViewHolder extends RecyclerView.ViewHolder {
    public FollowingBaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(ArrayList<Following> followingActivityList, int position, Context context);
}