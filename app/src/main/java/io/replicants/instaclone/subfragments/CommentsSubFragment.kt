package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.CommentsAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Comment
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_comments.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CommentsSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoID: String): CommentsSubFragment {
            val myFragment = CommentsSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var recycler: RecyclerView
    lateinit var adapter: CommentsAdapter
    val commentsList = ArrayList<Comment?>()
    lateinit var photoID: String
    var changeListener:Listener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }

        photoID = arguments?.getString("photoID", "") ?: ""

        layout = inflater.inflate(R.layout.subfragment_comments, container, false)
        recycler = layout.subfragment_comments_recycler
        recycler.setHasFixedSize(true)
        recycler.setItemViewCacheSize(40)

        val layoutManager = LinearLayoutManager(activity)
        recycler.layoutManager = layoutManager

        adapter = CommentsAdapter(activity!!, photoID, commentsList, recycler, changeListener)
        adapter.clickListeners = clickListeners
        recycler.adapter = adapter

        adapter.onLoadMoreListener = object : CommentsAdapter.OnLoadMoreListener {
            override fun onLoadMore() {
                recycler.post {
                    val lastCommentID = commentsList.lastOrNull()?.commentID
                    commentsList.add(null)
                    adapter.notifyItemInserted(commentsList.lastIndex)

                    InstaApi.getComments(photoID, lastCommentID).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                        override fun success(jsonResponse: JSONObject?) {

                            val result = Utils.commentsFromJsonArray(jsonResponse?.optJSONArray("comments")
                                    ?: JSONArray())

                            commentsList.removeAt(commentsList.lastIndex)
                            commentsList.addAll(result)
                            if (result.isEmpty()) {
                                adapter.canLoadMore = false
                            }
                            adapter.notifyDataSetChanged()
                            adapter.currentlyLoading = false
                            changeListener?.commentsChanged(commentsList.size)
                        }
                    }))
                }
            }
        }

        layout.subfragment_comments_send.onClick {
            val textToSend = layout.subfragment_comments_input.text.toString()
            if (textToSend.isNotBlank()) {
                InstaApi.newComment(photoID, textToSend).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                    override fun success(jsonResponse: JSONObject?) {
                        layout.subfragment_comments_input.text.clear()
                        adapter.canLoadMore = true
                        adapter.loadMore()
                    }
                }))
            }
        }

        layout.subfragment_comments_toolbar_back.onClick {
            clickListeners?.popBackStack(false)
        }

        initialLoad()



        return layout
    }

    fun initialLoad() {
        InstaApi.getComments(photoID, null).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                commentsList.clear()
                commentsList.addAll(Utils.commentsFromJsonArray(jsonResponse?.optJSONArray("comments")
                        ?: JSONArray()))
                adapter.notifyDataSetChanged()
            }
        }))
    }

    interface Listener{
        fun commentsChanged(totalNumber:Int)
    }
}