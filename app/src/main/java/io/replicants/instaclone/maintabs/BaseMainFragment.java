package io.replicants.instaclone.maintabs;

import android.view.LayoutInflater;


import java.util.ArrayList;
import java.util.zip.Inflater;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.replicants.instaclone.R;
import io.replicants.instaclone.pojos.User;
import io.replicants.instaclone.subfragments.BaseSubFragment;
import io.replicants.instaclone.subfragments.CommentsSubFragment;
import io.replicants.instaclone.subfragments.EditProfileSubFragment;
import io.replicants.instaclone.subfragments.MapSubFragment;
import io.replicants.instaclone.subfragments.PhotoSpecificSubFragment;
import io.replicants.instaclone.subfragments.ProfileSubFragment;
import io.replicants.instaclone.subfragments.UserListSubFragment;
import retrofit2.Call;
import retrofit2.Callback;

public class BaseMainFragment extends Fragment {

    ClickListeners clickListeners = new ClickListeners() {

        @Override
        public void moveToUserListSubFragmentWithList(ArrayList<User> users) {
            UserListSubFragment frag = UserListSubFragment.newInstance();
            frag.setUserList(users);
            changeFragment(frag);
        }


        @Override
        public void moveToUserListSubFragmentWithCall(UserListSubFragment.CallType callType, String id) {
            UserListSubFragment frag = UserListSubFragment.newInstance();
            frag.setCall(callType, id, null);
            changeFragment(frag);
        }

        @Override
        public void moveToCommentsSubFragment(String photoID) {
            changeFragment(CommentsSubFragment.newInstance(photoID));
        }

        @Override
        public void moveToPhotoSpecificSubFragment(ArrayList<String> photoIDs) {
            changeFragment(PhotoSpecificSubFragment.newInstance(photoIDs));
        }

        @Override
        public void moveToMapSubFragment() {
            changeFragment(MapSubFragment.newInstance());
        }

        @Override
        public void moveToProfileSubFragment(String displayName) {
            ProfileSubFragment fragment = ProfileSubFragment.newInstance(displayName);

            Toolbar toolbar = (Toolbar)LayoutInflater.from(getContext()).inflate(R.layout.toolbar_basic, null, false);
            toolbar.setTitle(displayName);
            fragment.setToolbar(toolbar);

            changeFragment(fragment);
        }

        @Override
        public void moveToEditProfileSubFragment(String displayName) {
            changeFragment(EditProfileSubFragment.Companion.newInstance(displayName));
        }


    };

    private void changeFragment(BaseSubFragment fragment) {
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction tx = manager.beginTransaction();
        fragment.setClickListeners(clickListeners);
        tx.add(R.id.fragment_overall_container, fragment, null);
        tx.addToBackStack(null);
        tx.commit();
    }

    public void clearBackStack(){
        int num = getChildFragmentManager().getBackStackEntryCount();
        if(num>0){

            getChildFragmentManager().popBackStack(getChildFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    public interface ClickListeners {
        public void moveToUserListSubFragmentWithList(ArrayList<User> users);

        public void moveToUserListSubFragmentWithCall(UserListSubFragment.CallType callType, String id);

        public void moveToCommentsSubFragment(String photoID);

        public void moveToPhotoSpecificSubFragment(ArrayList<String> photoIDs);

        public void moveToMapSubFragment();

        public void moveToProfileSubFragment(String displayName);

        public void moveToEditProfileSubFragment(String displayName);

    }
}
