package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.subfragments.DiscoverPhotosSubFragment
import io.replicants.instaclone.subfragments.EditProfileSubFragment
import io.replicants.instaclone.subfragments.ProfileSubFragment
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import org.json.JSONObject

class DiscoverMainFragment : BaseMainFragment(){


    companion object {

        fun newInstance(): DiscoverMainFragment {
            val fragment = DiscoverMainFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }

    lateinit var manager: FragmentManager
    lateinit var toolbar:Toolbar

    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.mainfragment_discover, container, false)

        manager = childFragmentManager
        val tx = manager.beginTransaction()
        val profileFrag = DiscoverPhotosSubFragment.newInstance()
        profileFrag.clickListeners = this.clickListeners
        tx.add(R.id.fragment_overall_container, profileFrag, "profile_base")
        tx.commit()

        return layout
    }



}

