package com.tudorluca.lantern.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import android.view.ViewTreeObserver
import android.view.LayoutInflater
import com.tudorluca.lantern.R
import android.widget.SeekBar.OnSeekBarChangeListener
import com.nineoldandroids.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import com.tudorluca.lantern.utils.AnimationsUtils
import com.nineoldandroids.animation.Animator
import com.tudorluca.lantern.utils.dpToPx

public class DonationSeekBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    public var legendValues: List<String>
        get() = legendDrawable.legendValues
        set(values) {
            legendDrawable.legendValues = values

            // Set donation bar to smallest value.
            seekBar.setProgress(0)
        }

    public var selectedLegendIndex: Int
        get() = getLegendPoints(legendDrawable.legendValues).indexOf(seekBar.getProgress())
        set(index){
            seekBar.setProgress(getLegendPoints(legendValues)[index])
        }

    public var onDonationChanged: (seekBar: DonationSeekBar, valueIndex: Int) -> Unit = {(seekBar, index) -> }

    private val legendDrawable: DonationSeekBarLegendDrawable
    private val seekBar: SeekBar

    {
        setOrientation(LinearLayout.VERTICAL)
        LayoutInflater.from(context).inflate(R.layout.donation_seekbar, this, true)

        seekBar = findViewById(R.id.seekbar) as SeekBar
        legendDrawable = DonationSeekBarLegendDrawable()

        setupView()
    }

    private fun setupView() {
        findViewById(R.id.seekbar_legend)?.setBackground(legendDrawable)

        seekBar.getViewTreeObserver()?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                seekBar.getViewTreeObserver()?.removeOnPreDrawListener(this)
                val thumbWidth = seekBar.getThumb()?.getBounds()?.width() as Int
                val startOffset = thumbWidth / 2f
                val endOffset = startOffset

                legendDrawable.startOffsetX = startOffset + 17.dpToPx()
                legendDrawable.endOffsetX = endOffset + 17.dpToPx()
                return true
            }
        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val valueToSnapTo = getNearestLegendPoint(currentPoint = seekBar.getProgress(), points = getLegendPoints(legendDrawable.legendValues))
                val animator = ObjectAnimator.ofInt(seekBar, "progress", valueToSnapTo) as ObjectAnimator
                animator.setInterpolator(DecelerateInterpolator())
                animator.setDuration(150)
                animator.addListener(object : AnimationsUtils.BaseAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        val valueIndex = getLegendPoints(legendDrawable.legendValues).indexOf(valueToSnapTo)
                        onDonationChanged(this@DonationSeekBar, valueIndex)
                    }
                })
                animator.start()
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun getLegendPoints(legendValues: List<String>): Array<Int> {
        val step = 100 / (legendValues.size - 1)
        var points = Array(legendValues.size, { it * step })
        points[points.lastIndex] = 100
        return points
    }

    fun getNearestLegendPoint(currentPoint: Int, points: Array<Int>): Int {
        val distances = points.map { Math.abs(it - currentPoint) }
        return points[distances.indexOf(distances.min())]
    }
}