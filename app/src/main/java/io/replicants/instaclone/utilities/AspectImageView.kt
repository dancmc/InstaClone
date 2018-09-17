package io.replicants.instaclone.utilities

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView


class AspectImageView : ImageView {
    private var aspectRatio = 1.0f

    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    // where aspect ratio is height/width
    fun setAspectRatio(ratio: Float) {
        aspectRatio = ratio
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val hms = MeasureSpec.makeMeasureSpec((width * aspectRatio).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, hms)
    }
}