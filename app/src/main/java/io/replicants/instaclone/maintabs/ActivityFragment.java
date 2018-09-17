package io.replicants.instaclone.maintabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.replicants.instaclone.R;
import io.replicants.instaclone.utilities.Utils;

public class ActivityFragment extends Fragment {

    public static ActivityFragment newInstance(){
        ActivityFragment fragment = new ActivityFragment();

        Bundle args  = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_activity, container, false);


        return layout;
    }

}
