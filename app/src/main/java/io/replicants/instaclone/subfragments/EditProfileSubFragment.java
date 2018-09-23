package io.replicants.instaclone.subfragments;

import android.os.Bundle;

public class EditProfileSubFragment extends BaseSubFragment {

    public static EditProfileSubFragment newInstance(String displayName){
        EditProfileSubFragment myFragment = new EditProfileSubFragment();

        Bundle args = new Bundle();
        myFragment.setArguments(args);
        args.putString("displayName", displayName);

        return myFragment;
    }

}
