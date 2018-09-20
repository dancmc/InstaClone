package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.User
import retrofit2.Call
import retrofit2.Callback

class UserListSubFragment : BaseSubFragment() {


    var users:ArrayList<User>? = null
    var callType : CallType? = null
    var id :String? = null

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
        // TODO remember to handle paging too
        return layout

    }

    fun setUserList(users : ArrayList<User>){
        this.users = users
    }

    fun setCall(callType:CallType, id:String){
        this.callType = callType
        this.id = id
    }

    enum class CallType{
        FOLLOWERS, FOLLOWING, FOLLOWINGWHOFOLLOW, LIKES
    }

}