package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.*
import io.replicants.instaclone.subfragments.upload.pickphoto.CameraPagerFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.GalleryPagerFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.PickPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.post.PostPhotoSubFragment
import kotlinx.android.synthetic.main.mainfragment_activity.view.*

class ActivityMainFragment : BaseMainFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): ActivityMainFragment {
            val myFragment = ActivityMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    var comingFromRequestList = false
    lateinit var layout: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.mainfragment_activity, container, false)

        val manager = childFragmentManager
        val tx = manager.beginTransaction()
        val activityFrag = ActivitySubFragment.newInstance()
        activityFrag.clickListeners = this.clickListeners
        tx.add(R.id.fragment_overall_container, activityFrag, null)
        tx.commit()


        return layout
    }

    override fun beforeFragmentPopped() {
        for (frag in childFragmentManager.fragments) {
            if (frag.isVisible && frag is ApproveListSubFragment) {
                comingFromRequestList = true
            }
        }
    }

    override fun afterFragmentPopped() {
        for (frag in childFragmentManager.fragments) {
            if (frag.isVisible && frag is ActivitySubFragment && comingFromRequestList) {
                frag.refreshRequests()
                comingFromRequestList = false
            }
        }
    }
}