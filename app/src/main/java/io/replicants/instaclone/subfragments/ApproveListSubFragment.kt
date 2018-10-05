package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.ApproveListAdapter
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.adapters.UserListAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_approve_list.view.*
import retrofit2.Call
import retrofit2.Callback
import kotlinx.android.synthetic.main.subfragment_user_list.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject

class ApproveListSubFragment : BaseSubFragment() {


    var users = ArrayList<User?>()
    lateinit var recyclerView: RecyclerView
    lateinit var adapter :ApproveListAdapter

    companion object {

        @JvmStatic
        fun newInstance(userList:ArrayList<User?>): ApproveListSubFragment {
            val myFragment = ApproveListSubFragment()
            myFragment.setUserList(userList)

            val args = Bundle()
            myFragment.arguments = args


            return myFragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_approve_list, container, false)

        layout.subfragment_approve_list_approveall.onClick {
            users.forEach { user->
                InstaApi.approveUser(user?.displayName).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                    override fun success(jsonResponse: JSONObject) {
                        users.find { it?.displayName == user?.displayName}?.followStatusToMe = User.STATUS_FOLLOWING
                        adapter.notifyDataSetChanged()
                    }
                }))
            }
        }

        recyclerView = layout.subfragment_userlist_recycler
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager =layoutManager
        adapter = ApproveListAdapter(activity!!, users, recyclerView)
        adapter.clickListeners = clickListeners
        recyclerView.adapter = adapter


        return layout

    }


    fun setUserList(users : ArrayList<User?>){
        this.users = users
    }


}