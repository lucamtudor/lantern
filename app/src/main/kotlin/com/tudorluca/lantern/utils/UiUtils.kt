package com.tudorluca.lantern.utils

import android.util.DisplayMetrics
import android.content.Context
import com.tudorluca.lantern.LanternApplication
import android.view.WindowManager
import android.view.View
import android.app.Activity
import android.view.ViewGroup

/**
 * Created by Tudor Luca on 13/09/14.
 */
fun getDensityRatio(): Float {
    val metrics = getDisplayMetrics()
    return metrics.densityDpi / Constant.STANDARD_DPI
}

fun getDisplayMetrics(): DisplayMetrics {
    val metrics = DisplayMetrics()
    val windowManager = LanternApplication.getContext()?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    windowManager?.getDefaultDisplay()?.getMetrics(metrics)
    return metrics
}

fun Int.dpToPx(): Float {
    return this * getDensityRatio()
}

fun Float.dpToPx(): Float {
    val px = this * getDensityRatio()
    return px
}

fun ViewGroup.hideChildren() {
    for (index in 0..(getChildCount() - 1)) {
        getChildAt(index)?.setVisibility(View.GONE)
    }
}

fun ViewGroup.showAllChildren() {
    for (index in 0..(getChildCount() - 1)) {
        getChildAt(index)?.setVisibility(View.VISIBLE)
    }
}

/**
 * Look for a child view with the given id.  If this view has the given
 * id, return this view.
 *
 * @param id The id to search for.
 * @return The view that has the given id in the hierarchy.
 *
 * @throws IllegalArgumentException If there isn't a child view with the specified id.
 */
fun <T> View.findView(id: Int): T {
    val view = findViewById(id)
    if (view == null) throw IllegalArgumentException("Given ID $id could not be found in $this!")
    return view as T
}

/**
 * Finds a view that was identified by the id attribute from the XML that
 * was processed in {@link Activity#onCreate}.
 *
 * @param id The id to search for.
 * @return The view that has the given id.
 *
 * @throws IllegalArgumentException If there isn't a view with the specified id.
 */
fun <T> Activity.findView(id: Int): T {
    val view = findViewById(id)
    if (view == null) throw IllegalArgumentException("Given ID $id could not be found in $this!")
    return view as T
}
