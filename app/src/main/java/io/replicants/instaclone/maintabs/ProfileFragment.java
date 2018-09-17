package io.replicants.instaclone.maintabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.replicants.instaclone.R;
import io.replicants.instaclone.network.InstaRetrofit;

public class ProfileFragment extends Fragment {

    public static ProfileFragment newInstance(){
        ProfileFragment fragment = new ProfileFragment();

        Bundle args  = new Bundle();
        fragment.setArguments(args);


        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_profile, container, false);


        return layout;
    }

}
