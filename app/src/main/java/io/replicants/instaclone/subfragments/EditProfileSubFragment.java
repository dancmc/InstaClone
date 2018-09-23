package io.replicants.instaclone.subfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.replicants.instaclone.R;

public class EditProfileSubFragment extends BaseSubFragment {

    public static EditProfileSubFragment newInstance(String displayName){
        EditProfileSubFragment myFragment = new EditProfileSubFragment();

        Bundle args = new Bundle();
        myFragment.setArguments(args);
        args.putString("displayName", displayName);

        return myFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.subfragment_editprofile, container, false);
        return layout;
    }
}
