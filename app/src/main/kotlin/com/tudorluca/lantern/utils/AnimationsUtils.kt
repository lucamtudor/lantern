package com.tudorluca.lantern.utils

import com.nineoldandroids.animation.Animator.AnimatorListener
import com.nineoldandroids.animation.Animator

/**
 * Created by Tudor Luca on 13/09/14.
 */
public object AnimationsUtils {

    open class BaseAnimatorListener : AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }
        override fun onAnimationEnd(animation: Animator?) {
        }
        override fun onAnimationCancel(animation: Animator?) {
        }
        override fun onAnimationRepeat(animation: Animator?) {
        }
    }
}
