package io.replicants.instaclone.subfragments.upload.edit

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_adjust.view.*
import kotlinx.android.synthetic.main.subfragment_edit_photo_cancel_done.view.*
import kotlinx.android.synthetic.main.subfragment_seekbar.view.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onScrollChange
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.sdk27.coroutines.onTouch

class ContrastSubFragment:BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(seekValue:Int): ContrastSubFragment {
            val myFragment = ContrastSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putInt("seekValue",seekValue )

            return myFragment
        }
    }

    lateinit var layout :View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_seekbar, container, false)

        val seekValue = arguments?.getInt("seekValue") ?: 0
        layout.subfragment_seekbar_number.text = seekValue.toString()
        layout.subfragment_seekbar_seekbar.max = 200
        layout.subfragment_seekbar_seekbar.progress = seekValue+100
        layout.subfragment_seekbar_seekbar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    val adjustedSeekBar = progress-100
                    val contrastApply = when{
                        adjustedSeekBar<=0->(progress+100)/200f
                        else->1f+adjustedSeekBar/160f
                    }
                    layout.subfragment_seekbar_number.text = adjustedSeekBar.toString()
                    (parentFragment as? ImageContrast)?.adjustContrast(contrastApply, adjustedSeekBar)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })


        layout.subfragment_edit_photo_canceledit.onClick { (parentFragment as? ImageContrast)?.cancelCurrentEdit(true)}
        layout.subfragment_edit_photo_doneedit.onClick { (parentFragment as? ImageContrast)?.done()}

        return layout
    }

    interface ImageContrast {
        fun adjustContrast(newContrastMatrixValue:Float, contrastSetting:Int)

        fun cancelCurrentEdit(withSaveDialog:Boolean):Boolean

        fun done()
    }


}