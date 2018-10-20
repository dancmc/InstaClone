package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import kotlinx.android.synthetic.main.subfragment_activity_pager.view.*

class ActivitySubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): ActivitySubFragment {
            val myFragment = ActivitySubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var adapter: ActivitySubFragment.ActivityVPAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_activity_pager, container, false)

        adapter = ActivityVPAdapter(childFragmentManager, clickListeners)
        layout.subfragment_activity_viewpager.adapter = adapter
        (layout.subfragment_activity_tabs as TabLayout).setupWithViewPager(layout.subfragment_activity_viewpager)
        layout.subfragment_activity_viewpager.currentItem = 1


        return layout
    }

    class ActivityVPAdapter(fm: FragmentManager, private var listeners: BaseMainFragment.ClickListeners?) : FragmentStatePagerAdapter(fm) {

        var followingFrag:ActivityFollowingSubFragment? = null
        var selfFrag : ActivitySelfSubFragment? = null

        override fun getItem(position: Int): Fragment {

            return if (position == 0) {
                followingFrag ?: ActivityFollowingSubFragment().apply {
                    followingFrag = this
                    clickListeners = listeners
                }
            } else {
                selfFrag ?: ActivitySelfSubFragment().apply {
                    selfFrag = this
                    clickListeners = listeners
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0) {
                "Following"
            } else {
                "You"
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun refreshRequests() {
        (adapter.getItem(1) as? ActivitySelfSubFragment)?.refresh()
    }
}