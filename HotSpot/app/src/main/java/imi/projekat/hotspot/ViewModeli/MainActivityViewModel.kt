package imi.projekat.hotspot.ViewModeli

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.Ostalo.Repository
import imi.projekat.hotspot.Ostalo.SingleLiveEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MultipartBody
import okhttp3.ResponseBody

class MainActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveEditProfileResponse=MutableLiveData<BaseResponse<ResponseBody>>()
    val liveEditProfileResponse: MutableLiveData<BaseResponse<ResponseBody>>
        get() = _liveEditProfileResponse

    private var _KreirajPostResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val KreirajPostResponse =_KreirajPostResponse.asSharedFlow()



    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "ConnectionError"
    )
        Log.d("Exception",throwable.localizedMessage.toString())
    }



    private fun onError(greska: String){
        _liveEditProfileResponse.postValue(BaseResponse.Error(greska))
    }

    override fun onCleared() {
        super.onCleared()
        handleJob?.cancel()
    }


//    fun ChangeProfilePhoto(photo: MultipartBody.Part){
//        _liveEditProfileResponse.value=BaseResponse.Loading()
//        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
//
//            val response=repository.changeProfilePhoto(photo)
//            withContext(Dispatchers.Main){
//                if(response.isSuccessful){
//                    _liveEditProfileResponse.value=BaseResponse.Success(response.body())
//
//                }
//                else{
//                    val content = response.errorBody()!!.charStream().readText()
//                    Log.d("Brzi",response.toString())
//                    _liveEditProfileResponse.postValue(BaseResponse.Error(response.toString()))
//                }
//            }
//        }
//    }

    fun KreirajPost(post:String){
        //KreirajPostResponse.value=BaseResponse.Loading()
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            val response=repository.KreirajPost(post)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    Log.d("Brzi1",response.toString())
                    _KreirajPostResponse.emit(BaseResponse.Success(response.body()))

                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    Log.d("Brzi2",response.toString())
                    _KreirajPostResponse.emit(BaseResponse.Error(response.toString()))

                }
            }
        }
    }





}