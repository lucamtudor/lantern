package com.tudorluca.lantern.widget

import android.graphics.drawable.Drawable
import android.graphics.Paint
import android.graphics.ColorFilter
import android.graphics.Canvas
import android.graphics
import kotlin.properties.Delegates
import java.util.ArrayList
import com.tudorluca.lantern.utils.dpToPx
import android.graphics.Color
import android.graphics.PixelFormat

public class DonationSeekBarLegendDrawable() : Drawable() {

    public var startOffsetX: Float = 0f
    public var endOffsetX: Float = 0f

    public var legendValues: List<String> by Delegates.observable(initial = ArrayList()) {(meta, old, new) -> invalidateSelf() }


    private val mTextPaint: Paint

    {
        mTextPaint = graphics.Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        mTextPaint.setTextSize(17f.dpToPx())
        mTextPaint.setColor(Color.WHITE)
        mTextPaint.setTextAlign(Paint.Align.CENTER)
    }

    override fun setAlpha(alpha: Int) {
        mTextPaint.setAlpha(alpha)
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mTextPaint.setColorFilter(cf)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun draw(canvas: Canvas) {
        val step = (canvas.getWidth() - startOffsetX - endOffsetX) / (legendValues.size - 1).toFloat()
        var x = startOffsetX
        val y = (canvas.getHeight() - mTextPaint.descent() - mTextPaint.ascent()) / 2

        for (index in legendValues.indices) {
            val text = legendValues[index]
            canvas.drawText(text, x, y, mTextPaint)
            x += step
        }
    }
}