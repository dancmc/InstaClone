package io.replicants.instaclone.subfragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.UserListAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_discover_users.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat.getSystemService
import io.replicants.instaclone.adapters.DiscoverUserListAdapter


class DiscoverUsersSubFragment : BaseSubFragment() {


    var suggestUsers = ArrayList<User?>()
    var searchUsers = ArrayList<User?>()
    lateinit var suggestRecyclerView: RecyclerView
    lateinit var searchRecyclerView: RecyclerView
    lateinit var suggestAdapter: DiscoverUserListAdapter
    lateinit var searchAdapter: UserListAdapter

    var searchName = ""
    var page = 1

    companion object {

        @JvmStatic
        fun newInstance(): DiscoverUsersSubFragment {
            val myFragment = DiscoverUsersSubFragment()

            val args = Bundle()
            myFragment.arguments = args


            return myFragment
        }

    }


    lateinit var layout: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.subfragment_discover_users, container, false)

            suggestRecyclerView = layout.subfragment_discover_users_suggest_recycler
            searchRecyclerView = layout.subfragment_discover_users_search_recycler

            val suggestLayoutManager = LinearLayoutManager(context)
            suggestRecyclerView.layoutManager = suggestLayoutManager
            suggestAdapter = DiscoverUserListAdapter(activity!!, suggestUsers, suggestRecyclerView)
            suggestAdapter.clickListeners = clickListeners
            suggestRecyclerView.adapter = suggestAdapter

            layout.subfragment_discover_users_suggest_refresh.setOnRefreshListener {
                initialLoad()
            }

            val searchLayoutManager = LinearLayoutManager(context)
            searchRecyclerView.layoutManager = searchLayoutManager
            searchAdapter = UserListAdapter(activity!!, searchUsers, searchRecyclerView)
            searchAdapter.clickListeners = clickListeners
            searchRecyclerView.adapter = searchAdapter

            searchAdapter.onLoadMoreListener = object : UserListAdapter.OnLoadMoreListener {
                override fun onLoadMore() {

                    searchRecyclerView.post {
                        searchUsers.add(null)
                        searchAdapter.notifyItemInserted(searchUsers.lastIndex)

                        InstaApi.discoverSearch(searchName, page).enqueue(InstaApi.generateCallback(activity, loadMoreApiCallback()))

                    }

                }
            }

            layout.subfragment_discover_users_searchbar.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchName = s.toString()
                    searchAdapter.currentlyLoading = true
                    searchUsers.clear()
                    searchAdapter.notifyDataSetChanged()
                    if (searchName.isBlank()) {
                        searchRecyclerView.visibility = View.GONE
                    } else {
                        searchRecyclerView.visibility = View.VISIBLE
                        executeSearch()
                    }


                }
            })

            layout.subfragment_discover_users_searchbar.requestFocus()

            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            layout.subfragment_discover_users_searchbar.postDelayed({
                imm?.showSoftInput(layout.subfragment_discover_users_searchbar, InputMethodManager.SHOW_IMPLICIT)
            },100)


            layout.subfragment_discover_users_back.onClick {
                clickListeners?.popBackStack(false)
            }

            initialLoad()
        }

        return layout

    }

    private fun initialLoad() {
        if (!layout.subfragment_discover_users_suggest_refresh.isRefreshing) {
            layout.subfragment_discover_users_suggest_refresh.isRefreshing = true
        }
        suggestAdapter.currentlyLoading = true
        suggestUsers.clear()
        suggestUsers.add(null)
        suggestAdapter.notifyDataSetChanged()

        InstaApi.discoverUsers().enqueue(InstaApi.generateCallback(activity, initialApiCallback()))
    }

    private fun initialApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                val userArray = jsonResponse?.optJSONArray("users") ?: JSONArray()
                val userList: List<User> = Utils.usersFromJsonArray(userArray)

                suggestUsers.addAll(userList)
                suggestAdapter.currentlyLoading = false
                suggestAdapter.canLoadMore = true
                suggestAdapter.notifyDataSetChanged()
                layout.subfragment_discover_users_suggest_refresh.isRefreshing = false
            }

            override fun failure(context: Context?, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                layout.subfragment_discover_users_suggest_refresh.isRefreshing = false
            }

            override fun networkFailure(context: Context?) {
                super.networkFailure(context)
                layout.subfragment_discover_users_suggest_refresh.isRefreshing = false
            }
        }
    }

    private fun executeSearch() {
        page = 1
        InstaApi.discoverSearch(searchName, page).enqueue(InstaApi.generateCallback(activity, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                page++
                val userArray = jsonResponse?.optJSONArray("users") ?: JSONArray()
                val userList: List<User> = Utils.usersFromJsonArray(userArray)

                searchUsers.addAll(userList)
                searchAdapter.currentlyLoading = false
                searchAdapter.canLoadMore = true
                searchAdapter.notifyDataSetChanged()
            }

        }))
    }

    // only applies to search
    private fun loadMoreApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                page++
                val userArray = jsonResponse?.optJSONArray("users") ?: JSONArray()
                val userList: List<User> = Utils.usersFromJsonArray(userArray)

                searchUsers.removeAt(searchUsers.lastIndex)
                searchUsers.addAll(userList)
                if (userList.isEmpty()) {
                    searchAdapter.canLoadMore = false
                }
                searchAdapter.notifyDataSetChanged()
                searchAdapter.currentlyLoading = false
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                searchUsers.removeAt(searchUsers.lastIndex)
                searchAdapter.notifyDataSetChanged()
                searchAdapter.currentlyLoading = false
            }

            override fun networkFailure(context: Context) {
                super.networkFailure(context)
                searchUsers.removeAt(searchUsers.lastIndex)
                searchAdapter.notifyDataSetChanged()
                searchAdapter.currentlyLoading = false
            }
        }
    }


}