package imi.projekat.hotspot.ViewModeli

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.Repository
import kotlinx.coroutines.*
import okhttp3.ResponseBody

class LoginActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveLoginResponse=MutableLiveData<BaseResponse<LoginResponse>>()
    val liveLoginResponse: MutableLiveData<BaseResponse<LoginResponse>>
        get() = _liveLoginResponse


    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "Exception:${throwable.localizedMessage}"
    )
    }



    private fun onError(greska: String){
        _liveLoginResponse.postValue(BaseResponse.Error(greska))
    }

    override fun onCleared() {
        super.onCleared()
        handleJob?.cancel()
    }


    fun login(loginData:loginDTS){
        _liveLoginResponse.value=BaseResponse.Loading()
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.login(loginData)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _liveLoginResponse.value=BaseResponse.Success(response.body())

                }
                else{
                    val gson = Gson()
                    val type = object : TypeToken<LoginResponse>() {}.type
                    var errorResponse: LoginResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
                    onError(errorResponse.message.toString())
                }
            }
        }
    }
}