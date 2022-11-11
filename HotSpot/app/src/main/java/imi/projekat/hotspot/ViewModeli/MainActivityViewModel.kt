package imi.projekat.hotspot.ViewModeli

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.changeAccDataResponse
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
import retrofit2.http.Part

class MainActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveEditProfileResponse=MutableSharedFlow<BaseResponse<changeAccDataResponse>>()
    val liveEditProfileResponse=_liveEditProfileResponse.asSharedFlow()

    private var _KreirajPostResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val KreirajPostResponse =_KreirajPostResponse.asSharedFlow()



    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "ConnectionError"
    )
        Log.d("Exception",throwable.localizedMessage.toString())
    }



    private fun onError(greska: String){
       // _liveEditProfileResponse.emit(BaseResponse.Error(greska))
    }

    override fun onCleared() {
        super.onCleared()
        handleJob?.cancel()
    }


    fun ChangeProfilePhoto(@Part slika: MultipartBody.Part, @Part username:MultipartBody.Part, @Part email:MultipartBody.Part,
                           @Part oldPassword:MultipartBody.Part,@Part newPassword:MultipartBody.Part){
        //_liveEditProfileResponse.value=BaseResponse.Loading()
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.changeProfilePhoto(slika,username,email,oldPassword,newPassword)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _liveEditProfileResponse.emit(BaseResponse.Success(response.body()))

                }
                else{
                    val gson = Gson()
                    val type = object : TypeToken<changeAccDataResponse>() {}.type
                    var errorResponse: changeAccDataResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
                    _liveEditProfileResponse.emit((BaseResponse.Error(errorResponse.message.toString())))
                }
            }
        }
    }

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