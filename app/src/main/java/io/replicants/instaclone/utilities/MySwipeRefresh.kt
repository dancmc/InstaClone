package io.replicants.instaclone.utilities

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MySwipeRefresh :SwipeRefreshLayout{


    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }



    var recyclerView:RecyclerView? = null

 
}