

package com.argonaut.showjava.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue as VolleyRequestQueue
import com.android.volley.toolbox.Volley

/**
 * A singleton request queue to use with Volley for all network requests.
 */
class RequestQueue constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RequestQueue? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestQueue(context).also {
                    INSTANCE = it
                }
            }
    }
    private val requestQueue: VolleyRequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
