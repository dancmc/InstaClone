package io.replicants.instaclone.adapters

import android.app.Activity
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.BluetoothItem
import io.replicants.instaclone.pojos.Comment
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.*
import kotlinx.android.synthetic.main.adapter_bluetooth_item.view.*
import kotlinx.android.synthetic.main.adapter_comment_item.view.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.adapter_userlist_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import org.json.JSONObject

class BTListAdapter(private val context: Context?, private val dataset: MutableList<BluetoothItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener :BTListAdapter.Listener?=null


    inner class BTHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image = v.adapter_bluetooth_item_image
        val text = v.adapter_bluetooth_item_text

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BTHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bluetooth_item, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = dataset[position]
        holder as BTHolder

        holder.itemView.onClick {
            listener?.onClick(item)
        }

        holder.text.text = if(item.name.isNotBlank())item.name else item.address

        when{
            item.connecting->{
                holder.image.setImageResource(R.drawable.icon_bluetooth_orange)
                context?.let {
                    holder.text.setTextColor(ContextCompat.getColor(context,R.color.orange700))
                }
            }
            item.connected->{
                holder.image.setImageResource(R.drawable.icon_bluetooth_cyan)
                context?.let {
                    holder.text.setTextColor(ContextCompat.getColor(context,R.color.lightblue700))
                }
            }
            else->{
                holder.image.setImageResource(R.drawable.icon_bluetooth)
                context?.let {
                    holder.text.setTextColor(ContextCompat.getColor(context,R.color.grey500))
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    interface Listener{
        fun onClick(bluetoothItem: BluetoothItem)
    }

}