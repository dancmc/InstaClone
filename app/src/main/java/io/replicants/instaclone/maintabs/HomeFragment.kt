package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.FeedFragment
import org.jetbrains.anko.toast


class HomeFragment : Fragment(), FeedFragment.FeedFragmentInterface {

    companion object {

        fun newInstance(): HomeFragment {
            val myFragment = HomeFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var manager :FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_home, container, false)

        manager = childFragmentManager
        val tx = manager.beginTransaction()
        val feedFrag = FeedFragment.newInstance()
        tx.add(R.id.fragment_home_container, feedFrag, null)
        tx.commit()


        return layout
    }

    override fun moveToSettings() {
        val tx = manager.beginTransaction()
        val settingsFrag = SettingsFragment.newInstance()
        tx.replace(R.id.fragment_home_container, settingsFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun moveToAdhoc() {
        activity?.toast("Will go to adhoc")
        // TODO not implemented
    }


}