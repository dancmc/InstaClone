package io.replicants.instaclone.adapters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CursorAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.SavedPhoto
import kotlinx.android.synthetic.main.adapter_gallery_header.view.*
import kotlinx.android.synthetic.main.adapter_gallery_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.toast


// Inspired by https://stackoverflow.com/questions/26517855/using-the-recyclerview-with-a-database
class BluetoothGalleryCursorAdapter(var context: Context, var cursor: Cursor, var clickListener: Listener, startingCursorPos: Int = 0) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var cursorAdapter: CursorAdapter

    init {
        // preview the first photo
        swapCursor(cursor, startingCursorPos)
    }


    fun swapCursor(cursor: Cursor, position: Int = 0) {

        this.cursor = cursor
        cursor.moveToPosition(position)

        cursorAdapter = object : CursorAdapter(context, cursor, false) {
            override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
                val v = LayoutInflater.from(context).inflate(R.layout.adapter_gallery_item, parent, false)
                v.gallery_item_image.setAspectRatio(1f)
                return v
            }

            override fun bindView(view: View, context: Context?, cursor: Cursor) {

                val path = cursor.getString(1)
                val pos = cursor.position

                Glide.with(context!!).load(path).into(view.gallery_item_image)
            }
        }

    }


    inner class PhotoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imageView = v.gallery_item_image
    }

    // These are the methods that control the ordering

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PhotoViewHolder(cursorAdapter.newView(context, cursorAdapter.cursor, parent))
    }

    override fun getItemCount(): Int {
        return cursorAdapter.count
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PhotoViewHolder -> {
                cursorAdapter.cursor.moveToPosition(position)
                cursorAdapter.bindView(holder.itemView, context, cursorAdapter.cursor)
                holder.itemView.isLongClickable = true
                holder.itemView.onLongClick {
                    clickListener.onLongClick()
                }

            }
        }

    }



    interface Listener {
        fun onLongClick()

    }



}