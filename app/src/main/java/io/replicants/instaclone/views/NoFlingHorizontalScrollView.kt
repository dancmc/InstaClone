package io.replicants.instaclone.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.Nullable

class NoFlingHorizontalScrollView:HorizontalScrollView {



    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, @Nullable attrs: AttributeSet, intDefStyle: Int) : super(context, attrs, intDefStyle) {}

    override fun fling(velocityX: Int) {

    }
}