package io.replicants.instaclone.maintabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.replicants.instaclone.R;

public class ActivityMainFragment extends BaseMainFragment {

    public static ActivityMainFragment newInstance(){
        ActivityMainFragment fragment = new ActivityMainFragment();

        Bundle args  = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.mainfragment_activity, container, false);


        return layout;
    }

}
