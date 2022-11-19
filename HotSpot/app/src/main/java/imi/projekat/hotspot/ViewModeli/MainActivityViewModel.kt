package imi.projekat.hotspot.ViewModeli

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.*
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.Repository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Part

class MainActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveEditProfileResponse=MutableSharedFlow<BaseResponse<changeAccDataResponse>>()
    val liveEditProfileResponse=_liveEditProfileResponse.asSharedFlow()

    private var _liveProfilePhotoResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val liveProfilePhotoResponse=_liveProfilePhotoResponse.asSharedFlow()

    private var _KreirajPostResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val KreirajPostResponse =_KreirajPostResponse.asSharedFlow()

    private var _DodajPostResposne= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val DodajPostResposne=_DodajPostResposne.asSharedFlow()

    private var _liveAllFollowingByUser= MutableSharedFlow<BaseResponse<getuser>>()
    val liveAllFollowingByUser=_liveAllFollowingByUser.asSharedFlow()



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

    fun addPost(@Part photos:ArrayList<MultipartBody.Part>,@Part description:MultipartBody.Part,@Part location:MultipartBody.Part){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            val response=repository.addPost(photos,description,location)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    Log.d("Brzi1",response.toString())
                    _DodajPostResposne.emit(BaseResponse.Success(response.body()))

                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    Log.d("Brzi2",response.toString())
                    _DodajPostResposne.emit(BaseResponse.Error(response.toString()))

                }
            }
        }
    }

    fun getPhoto(){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.getProfilePhoto()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _liveProfilePhotoResponse.emit(BaseResponse.Success(response.body()))

                }
                else{

//                    val gson = Gson()
//                    val type = object : TypeToken<ResponseBody>() {}.type
//                    val errorResponse: ResponseBody = gson.fromJson(response.errorBody()!!.charStream(), type)

//                    val content = response.errorBody()!!.charStream().readText()
//                    _liveProfilePhotoResponse.emit((BaseResponse.Error(content)))
                }
            }
        }
    }

    fun GetAllFollowingByUser(){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.getAllFollowingByUSer()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _liveAllFollowingByUser.emit(BaseResponse.Success(response.body()))

                }
                else{

//                    val gson = Gson()
//                    val type = object : TypeToken<ResponseBody>() {}.type
//                    val errorResponse: ResponseBody = gson.fromJson(response.errorBody()!!.charStream(), type)

//                    val content = response.errorBody()!!.charStream().readText()
//                    _liveProfilePhotoResponse.emit((BaseResponse.Error(content)))
                }
            }
        }
    }




}