package com.robkonarski.workflow

sealed class ActivityResult<T> {

    /**
     * Successful activity execution status.
     *
     * @param data Indicates the data passed to the next activity.
     */
    data class Success<T> @JvmOverloads constructor(val data: T? = null) : ActivityResult<T>()

    /**
     * Error activity execution status.
     *
     * @param exception Indicates what went wrong.
     * @param retry Indicates whether the activity should be retried or not.
     *              If false, the workflow will be marked as failed.
     */
    data class Error<T>(val exception: Exception, val retry: Boolean) : ActivityResult<T>()
}
