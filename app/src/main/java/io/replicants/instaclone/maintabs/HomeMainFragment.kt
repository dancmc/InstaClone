package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.FeedSubFragment
import io.replicants.instaclone.subfragments.TemporarySubFragment
import org.jetbrains.anko.toast


class HomeMainFragment : BaseMainFragment(), FeedSubFragment.FeedFragmentInterface {

    companion object {

        fun newInstance(): HomeMainFragment {
            val myFragment = HomeMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var manager :FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.mainfragment_home, container, false)

        manager = childFragmentManager
        val tx = manager.beginTransaction()
        val feedFrag = FeedSubFragment.newInstance()
        feedFrag.clickListeners = this.clickListeners
        tx.add(R.id.fragment_overall_container, feedFrag, null)
        tx.commit()


        return layout
    }

    override fun moveToSettings() {
        val tx = manager.beginTransaction()
        val settingsFrag = TemporarySubFragment.newInstance()
        settingsFrag.clickListeners = this.clickListeners
        tx.add(R.id.fragment_overall_container, settingsFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun moveToAdhoc() {
        activity?.toast("Will go to adhoc")
        // TODO not implemented
    }


}