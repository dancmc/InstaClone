package io.replicants.instaclone.adapters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import kotlinx.android.synthetic.main.adapter_feed_item_grid.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


// Inspired by https://stackoverflow.com/questions/26517855/using-the-recyclerview-with-a-database
class GalleryCursorAdapter(var context:Context, var cursor:Cursor, var clickListener:Listener):RecyclerView.Adapter<GalleryCursorAdapter.ViewHolder>() {

    lateinit var adapter:CursorAdapter

    init {
        // preview the first photo

        swapCursor(cursor)
    }

    fun swapCursor(cursor: Cursor){

        val move = cursor.moveToFirst()
        if(move){
            clickListener.onClick(cursor.getString(1))
        }

        adapter = object : CursorAdapter(context, cursor, false){
            override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
                val v = LayoutInflater.from(context).inflate(R.layout.adapter_feed_item_grid, parent, false)
                v.feed_item_grid_image.setAspectRatio(1f)
                return v
            }

            override fun bindView(view: View, context: Context?, cursor: Cursor) {

                val path = cursor.getString(1)

                Glide.with(context!!).load(path).into(view.feed_item_grid_image)
                view.feed_item_grid_image.onClick {
                    clickListener.onClick(path)
                }
            }
        }
        notifyDataSetChanged()
    }


    inner class ViewHolder(v:View) : RecyclerView.ViewHolder(v){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = adapter.newView(context, adapter.cursor, parent)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return adapter.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapter.cursor.moveToPosition(position)
        adapter.bindView(holder.itemView, context, adapter.cursor)
    }

    interface Listener{
        fun onClick(filePath:String)
    }
}