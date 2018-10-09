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
class GalleryCursorAdapter(var context: Context, var layoutManager: GridLayoutManager, var drafts: ArrayList<SavedPhoto>, var cursor: Cursor, var clickListener: Listener, startingCursorPos: Int = 0) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var cursorAdapter: CursorAdapter
    var headers = ArrayList<String>()

    val TYPE_HEADER = 0
    val TYPE_DRAFT = 1
    val TYPE_PHOTO = 2

    var inLongClickMode = false
    var longClickStartNumber = 0
    var selectedItems = HashSet<Int>()

    init {
        // preview the first photo
        swapCursor(cursor, startingCursorPos)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (getItemViewType(position)) {
                    TYPE_HEADER -> 4
                    else -> 1
                }
            }
        }
    }

    fun loadHeaders() {
        if (drafts.size > 0) {
            headers = arrayListOf("Drafts", "Gallery")
        } else {
            headers = arrayListOf("Gallery")
        }
        cancelLongClickMode()
    }

    fun reloadDrafts(drafts: ArrayList<SavedPhoto>) {
        this.drafts = drafts
        loadHeaders()
    }

    fun swapCursor(cursor: Cursor, position: Int = 0) {



        this.cursor = cursor
        val move = cursor.moveToPosition(position)
        if (move) {
            clickListener.onClick(cursor.getString(1), position)
        }

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
                view.gallery_item_image.onClick {
                    if (!inLongClickMode) {
                        clickListener.onClick(path, pos)
                    }
                }
            }
        }
        loadHeaders()

    }


    inner class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView = v.adapter_gallery_header_text
    }

    inner class PhotoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imageView = v.gallery_item_image
        val tick = v.gallery_item_tick


    }

    // These are the methods that control the ordering

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_gallery_header, parent, false))
        } else {
            PhotoViewHolder(cursorAdapter.newView(context, cursorAdapter.cursor, parent))
        }
    }

    override fun getItemCount(): Int {
        return cursorAdapter.count + drafts.size + headers.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                if (position == 0) {
                    holder.textView.text = headers[0]
                } else {
                    holder.textView.text = headers[1]
                }
            }
            is PhotoViewHolder -> {


                if (drafts.size == 0) {
                    // handle normal gallery if no drafts
                    cursorAdapter.cursor.moveToPosition(position - 1)
                    cursorAdapter.bindView(holder.itemView, context, cursorAdapter.cursor)
                    holder.itemView.isLongClickable = false
                    holder.tick.visibility = View.GONE
                } else {
                    val offset = 2 + drafts.size
                    if (position < offset) {
                        // this part handles the drafts
                        val draft = drafts[position - 1]

                        val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                        Glide.with(context).load(draft.photoFilePreview).apply(requestOptions).into(holder.imageView)

                        if (inLongClickMode) {
                            holder.tick.visibility = View.VISIBLE
                            holder.tick.isSelected = position - 1 in selectedItems
                        } else {
                            holder.tick.visibility = View.GONE
                        }

                        // handle click (in long click mode and not)
                        holder.imageView.onClick {
                            if (inLongClickMode) {
                                if (position - 1 !in selectedItems) {
                                    selectedItems.add(position - 1)
                                } else {
                                    selectedItems.remove(position - 1)
                                }
                                clickListener.onLongClickChanged()
                            } else {
                                clickListener.onClick(draft.photoFilePreview, position, draft)
                            }
                            notifyDataSetChanged()
                        }

                        // handle long click
                        holder.itemView.isLongClickable = true
                        holder.imageView.onLongClick {
                            context?.toast("Pressed")
                            if (!inLongClickMode) {
                                inLongClickMode = true
                                clickListener.onLongClick()
                                longClickStartNumber = position - 1
                                selectedItems.add(position - 1)
                            } else {
                                if (position - 1 > longClickStartNumber) {
                                    selectedItems.clear()
                                    selectedItems.addAll((longClickStartNumber until position).toList())
                                    clickListener.onLongClickChanged()
                                }
                            }
                            notifyDataSetChanged()
                        }

                    } else {
                        // this part handles gallery when there are drafts
                        cursorAdapter.cursor.moveToPosition(position - offset)
                        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.cursor)
                        holder.itemView.isLongClickable = false
                        holder.tick.visibility = View.GONE
                    }
                }
            }
        }

    }


    override fun getItemViewType(position: Int): Int {
        return if (drafts.size > 0) {
            when {
                position == 0 -> TYPE_HEADER
                position > 0 && position < drafts.size + 1 -> TYPE_DRAFT
                position == drafts.size + 1 -> TYPE_HEADER
                else -> TYPE_PHOTO
            }
        } else {
            when (position) {
                0 -> TYPE_HEADER
                else -> TYPE_PHOTO
            }
        }
    }

    interface Listener {
        fun onClick(filePath: String, position: Int, draft: SavedPhoto? = null)

        fun onLongClick()

        fun onLongClickChanged()

        fun onLongClickCancelled()
    }

    fun cancelLongClickMode() {
        longClickStartNumber = 0
        inLongClickMode = false
        selectedItems.clear()
        clickListener.onLongClickCancelled()
        notifyDataSetChanged()
    }

    fun getLongClickItems(): List<SavedPhoto> {
        return drafts.filterIndexed { index, _ -> index in selectedItems }
    }



}