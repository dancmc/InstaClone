package io.replicants.instaclone.maintabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.Photo
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import org.json.JSONArray
import org.json.JSONObject

class SettingsFragment : Fragment() {

    companion object {

        fun newInstance(): SettingsFragment {
            val myFragment = SettingsFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false)

        layout.fragment_settings_button_logout.setOnClickListener {
            (activity as MainActivity).logout()
        }


        return layout
    }
}