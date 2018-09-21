package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.adapters.UserListAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import kotlinx.android.synthetic.main.subfragment_user_list.view.*
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject

class UserListSubFragment : BaseSubFragment() {


    var users = ArrayList<User?>()
    var callType : CallType? = null
    var id :String? = null
    var recent:Int? = null
    lateinit var recyclerView: RecyclerView
    lateinit var adapter :UserListAdapter

    companion object {

        @JvmStatic
        fun newInstance(): UserListSubFragment {
            val myFragment = UserListSubFragment()

            val args = Bundle()
            myFragment.arguments = args


            return myFragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_user_list, container, false)
        recyclerView = layout.subfragment_userlist_recycler
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager =layoutManager
        adapter = UserListAdapter(activity!!, users, recyclerView)
        adapter.clickListeners = clickListeners
        recyclerView.adapter = adapter

        if(callType!=null){
            users.clear()
            when(callType){
                CallType.FOLLOWERS->{
                    InstaApi.getFollowers(id, null).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject?) {
                            users.addAll(Utils.usersFromJsonArray(jsonResponse?.optJSONArray("followers")?: JSONArray()))
                            adapter.notifyDataSetChanged()
                        }
                    }))
                }
                CallType.FOLLOWING->{
                    InstaApi.getFollowing(id, null).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject?) {
                            users.addAll(Utils.usersFromJsonArray(jsonResponse?.optJSONArray("following")?: JSONArray()))
                            adapter.notifyDataSetChanged()
                        }
                    }))
                }
                CallType.FOLLOWINGWHOFOLLOW->{
                    InstaApi.getFollowingWhoFollow(id, null).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject?) {
                            users.addAll(Utils.usersFromJsonArray(jsonResponse?.optJSONArray("users")?: JSONArray()))
                            adapter.notifyDataSetChanged()
                        }
                    }))
                }
                CallType.LIKES->{
                    InstaApi.getLikes(id, recent, null).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject?) {
                            users.addAll(Utils.usersFromJsonArray(jsonResponse?.optJSONArray("likes")?: JSONArray()))
                            adapter.notifyDataSetChanged()
                        }
                    }))
                }
            }
        }

        adapter.onLoadMoreListener = object : FeedAdapter.OnLoadMoreListener {
            override fun onLoadMore() {

                if(callType==null){
                    adapter.currentlyLoading = false
                    adapter.canLoadMore = false
                } else{
                    recyclerView.post {

                        val lastName = if (users.size > 0) users.last()?.displayName else null
                        users.add(null)
                        adapter.notifyItemInserted(users.lastIndex)

                        when(callType){
                            CallType.FOLLOWERS->{
                                InstaApi.getFollowers(id, lastName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                                    override fun success(jsonResponse: JSONObject?) {
                                        val result = Utils.usersFromJsonArray(jsonResponse?.optJSONArray("followers")?:JSONArray())
                                        processLoadMore(result)
                                    }
                                }))
                            }
                            CallType.FOLLOWING->{
                                InstaApi.getFollowing(id, lastName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                                    override fun success(jsonResponse: JSONObject?) {
                                        val result = Utils.usersFromJsonArray(jsonResponse?.optJSONArray("following")?:JSONArray())
                                        processLoadMore(result)
                                    }
                                }))
                            }
                            CallType.FOLLOWINGWHOFOLLOW->{
                                InstaApi.getFollowingWhoFollow(id, lastName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                                    override fun success(jsonResponse: JSONObject?) {
                                        val result = Utils.usersFromJsonArray(jsonResponse?.optJSONArray("users")?:JSONArray())
                                        processLoadMore(result)
                                    }
                                }))
                            }
                            CallType.LIKES->{
                                InstaApi.getLikes(id, recent, lastName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                                    override fun success(jsonResponse: JSONObject?) {
                                        val result = Utils.usersFromJsonArray(jsonResponse?.optJSONArray("likes")?:JSONArray())
                                        processLoadMore(result)
                                    }
                                }))
                            }
                        }
                    }
                }
            }
        }


        return layout

    }

    fun processLoadMore(result:ArrayList<User>){
        users.removeAt(users.lastIndex)
        users.addAll(result)
        if(result.isEmpty()){
            adapter.canLoadMore = false
        }
        adapter.notifyDataSetChanged()
        adapter.currentlyLoading = false
    }

    fun setUserList(users : ArrayList<User?>){
        this.users = users
    }

    fun setCall(callType:CallType, id:String, recent:Int?=null){
        this.callType = callType
        this.id = id
        this.recent  = recent
    }

    enum class CallType{
        FOLLOWERS, FOLLOWING, FOLLOWINGWHOFOLLOW, LIKES
    }

}