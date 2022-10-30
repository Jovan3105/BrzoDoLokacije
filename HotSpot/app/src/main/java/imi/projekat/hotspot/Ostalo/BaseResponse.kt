package imi.projekat.hotspot.Ostalo

sealed class BaseResponse<out T> {
    data class Success<out T>(val data: T? = null) : BaseResponse<T>()
    data class Loading(val nothing: Nothing?=null) : BaseResponse<Nothing>()
    data class Error(val poruka: String?) : BaseResponse<Nothing>()
}