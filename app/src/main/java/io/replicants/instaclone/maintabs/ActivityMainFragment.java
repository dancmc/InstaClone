package io.replicants.instaclone.maintabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.fotoapparat.preview.Frame;
import io.replicants.instaclone.R;
import io.replicants.instaclone.subfragments.FeedSubFragment;
import io.replicants.instaclone.subfragments.FollowingSubFragment;
import io.replicants.instaclone.subfragments.YouSubFragment;

public class ActivityMainFragment extends BaseMainFragment {
    FragmentManager manager;


    public static ActivityMainFragment newInstance() {
        ActivityMainFragment fragment = new ActivityMainFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.mainfragment_activity, container, false);
        final ToggleButton followingToggle = layout.findViewById(R.id.following_toggle);
        final ToggleButton youToggle = layout.findViewById(R.id.you_toggle);

        followingToggle.setChecked(false);
        youToggle.setChecked(true);

        manager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        Fragment youFrag = YouSubFragment.newInstance();

        fragmentTransaction.replace(R.id.fragment_activity_container, youFrag, null);
        fragmentTransaction.commit();


        followingToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                youToggle.setChecked(false);
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                Fragment followingFrag = FollowingSubFragment.newInstance();
                fragmentTransaction.replace(R.id.fragment_activity_container, followingFrag, null);
                fragmentTransaction.commit();

            }
        });

        youToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your click actions go here
                followingToggle.setChecked(false);
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                Fragment youFrag = YouSubFragment.newInstance();
                fragmentTransaction.replace(R.id.fragment_activity_container, youFrag, null);
                fragmentTransaction.commit();
            }
        });


        return layout;
    }

}
