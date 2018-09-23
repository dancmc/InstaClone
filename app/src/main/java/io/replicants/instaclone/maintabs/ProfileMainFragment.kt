package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.ProfileSubFragment
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.subfragment_feed.*

class ProfileMainFragment : BaseMainFragment() {


    companion object {

        fun newInstance(): ProfileMainFragment {
            val fragment = ProfileMainFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }

    lateinit var manager: FragmentManager


    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.mainfragment_profile, container, false)

        val displayName = Prefs.DISPLAY_NAME
//        val displayName = arguments?.getString("displayName") ?: ""

        // stand alone toolbar for profile
        val toolbar = LayoutInflater.from(context).inflate(R.layout.profile_toolbar, null, false) as androidx.appcompat.widget.Toolbar
        toolbar.title = displayName

        manager = childFragmentManager
        val tx = manager.beginTransaction()
        val profileFrag = ProfileSubFragment.newInstance(displayName)

        profileFrag.changeToolbar(toolbar)
        profileFrag.clickListeners = this.clickListeners

        tx.add(R.id.fragment_overall_container, profileFrag, null)
        tx.commit()

        return layout
    }

}

