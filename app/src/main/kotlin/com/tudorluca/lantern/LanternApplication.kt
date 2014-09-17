package com.tudorluca.lantern

import android.app.Application
import android.content.Context
import com.tudorluca.lantern.utils.*
import kotlin.properties.Delegates

/**
 * Created by Tudor Luca on 13/09/14.
 */
public class LanternApplication() : Application() {

    class object {
        public val key: String by Delegates.lazy { constructKey() }

        private var sInstance: Context? = null

        public fun getContext(): Context? {
            return sInstance
        }

        private fun constructKey(): String {
            // Do some encryption with the public key :)
            return publicKey
        }
    }

    {
        sInstance = this
    }
}