package io.replicants.instaclone.utilities;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import io.replicants.instaclone.pojos.Following;
import io.replicants.instaclone.pojos.You;

public abstract class YouBaseViewHolder extends RecyclerView.ViewHolder {
    public YouBaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(ArrayList<You> followingActivityList, int position, Context context);
}