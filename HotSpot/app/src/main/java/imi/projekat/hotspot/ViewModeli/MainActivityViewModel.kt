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
import retrofit2.http.Part

class MainActivityViewModel(private val repository:Repository=Repository()) :ViewModel(){
    private var _liveEditProfileResponse=MutableSharedFlow<BaseResponse<changeAccDataResponse>>()
    val liveEditProfileResponse=_liveEditProfileResponse.asSharedFlow()

    private var _liveProfilePhotoResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val liveProfilePhotoResponse=_liveProfilePhotoResponse.asSharedFlow()

    private var _DodajPostResposne= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val DodajPostResposne=_DodajPostResposne.asSharedFlow()

    private var _GetPostWithIdResponse=MutableLiveData<BaseResponse<getpostResponse>>()
    val GetPostWithIdResponse: MutableLiveData<BaseResponse<getpostResponse>>
        get() = _GetPostWithIdResponse

    private var _PostCommentResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val PostCommentResponse=_PostCommentResponse.asSharedFlow()

    private var _GreskaHendler= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val GreskaHendler=_GreskaHendler.asSharedFlow()



    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "ConnectionError"
    )
        Log.d("ExceptionInMainView",throwable.localizedMessage.toString())
    }



    private fun onError(greska: String){
        runBlocking{
            launch {
                _GreskaHendler.emit(BaseResponse.Error(greska))
            }
        }
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

    fun addPost(@Part photos:ArrayList<MultipartBody.Part>,@Part description:MultipartBody.Part,@Part location:MultipartBody.Part,@Part shortDescription:MultipartBody.Part){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            _DodajPostResposne.emit(BaseResponse.Loading())
            val response=repository.addPost(photos,description,location,shortDescription)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    Log.d("Brzi1",response.toString())
                    _DodajPostResposne.emit(BaseResponse.Success(response.body()))

                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _DodajPostResposne.emit(BaseResponse.Error(content))

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

                    val content = response.errorBody()!!.charStream().readText()
                    _liveProfilePhotoResponse.emit((BaseResponse.Error(content)))
                }
            }
        }
    }


    fun getPostWithID(postid:Int){
        _GetPostWithIdResponse.value=BaseResponse.Loading()
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.getPostWithId(postid)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _GetPostWithIdResponse.value=BaseResponse.Success(response.body())
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _GetPostWithIdResponse.postValue(BaseResponse.Error(content))
                }
            }
        }
    }


    fun PostComment(comment:commentDTS){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.comment(comment)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _PostCommentResponse.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _PostCommentResponse.emit(BaseResponse.Error(content))
                }
            }
        }
    }


}