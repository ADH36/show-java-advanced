

package com.argonaut.showjava

/**
 * Constants used throughout the application
 */
object Constants {

    const val STORAGE_PERMISSION_REQUEST = 1009

    interface EVENTS {
        companion object {
            const val CLEAR_SOURCE_HISTORY = "clear_source_history"
            const val CHANGE_FONT = "change_font"
            const val TOGGLE_DARK_MODE = "toggle_dark_mode"
            const val SELECT_DECOMPILER = "select_decompiler"
            const val DECOMPILE_APP = "decompile_app"
            const val REPORT_APP_LOW_MEMORY = "report_app_low_memory"
        }
    }

    /**
     * Workers related constants
     */
    interface WORKER {

        companion object {
            const val STATUS_TYPE = "com.njlabs.showjava.worker.STATUS_TYPE"
            const val STATUS_TITLE = "com.njlabs.showjava.worker.STATUS_TITLE"
            const val STATUS_MESSAGE = "com.njlabs.showjava.worker.STATUS_MESSAGE"

            const val PROGRESS_NOTIFICATION_CHANNEL = "com.njlabs.showjava.worker.notification.progress"
            const val COMPLETION_NOTIFICATION_CHANNEL = "com.njlabs.showjava.worker.notification.completion"
            const val PROGRESS_NOTIFICATION_ID = 1094
            const val COMPLETED_NOTIFICATION_ID = 1095
        }

        /**
         * Actions used for interacting with workers
         */
        interface ACTION {
            companion object {
                // Action to broadcast status to receivers
                const val BROADCAST = "com.njlabs.showjava.worker.action.BROADCAST"

                // Action to instruct the receiver to stop the worker
                const val STOP = "com.njlabs.showjava.worker.action.STOP"
            }
        }
    }
}