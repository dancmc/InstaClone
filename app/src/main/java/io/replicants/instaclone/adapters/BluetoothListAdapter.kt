package io.replicants.instaclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.BluetoothItem
import kotlinx.android.synthetic.main.adapter_bluetooth_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick


// Adapter for the list of bluetooth devices in BluetoothSubFragment
class BluetoothListAdapter(private val context: Context?, private val dataset: MutableList<BluetoothItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener :BluetoothListAdapter.Listener?=null


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
                holder.image.setImageResource(R.drawable.icon_bluetooth_grey)
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