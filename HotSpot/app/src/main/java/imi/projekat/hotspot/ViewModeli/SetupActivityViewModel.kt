package imi.projekat.hotspot.ViewModeli

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.*
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.Ostalo.Repository
import imi.projekat.hotspot.Ostalo.SingleLiveEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MultipartBody
import okhttp3.ResponseBody

class SetupActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveRefreshTokenResponse=MutableLiveData<BaseResponse<refreshTokenResponse>>()
    val liveRefreshTokenResponse: MutableLiveData<BaseResponse<refreshTokenResponse>>
        get() = _liveRefreshTokenResponse




    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "ConnectionError"
    )
        Log.d("Exception",throwable.localizedMessage.toString())
    }



    private fun onError(greska: String){
        _liveRefreshTokenResponse.postValue(BaseResponse.Error(greska))
    }

    override fun onCleared() {
        super.onCleared()
        handleJob?.cancel()
    }


    fun refreshToken(zahtev: refreshTokenDTS){
        _liveRefreshTokenResponse.value=BaseResponse.Loading()
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.refresujToken(zahtev)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _liveRefreshTokenResponse.value=BaseResponse.Success(response.body())

                }
                else{
                    val gson = Gson()
                    val type = object : TypeToken<refreshTokenResponse>() {}.type
                    var errorResponse: refreshTokenResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
                    _liveRefreshTokenResponse.postValue(BaseResponse.Error(errorResponse.message.toString()))
                }
            }
        }
    }






}