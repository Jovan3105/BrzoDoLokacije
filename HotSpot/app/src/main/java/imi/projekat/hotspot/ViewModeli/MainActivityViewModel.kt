package imi.projekat.hotspot.ViewModeli

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
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



    private var _DodajPostResposne= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val DodajPostResposne=_DodajPostResposne.asSharedFlow()

    private var _GetPostWithIdResponse=MutableLiveData<BaseResponse<singlePost>>()
    val GetPostWithIdResponse: MutableLiveData<BaseResponse<singlePost>>
        get() = _GetPostWithIdResponse
    private var _liveAllFollowingByUser= MutableSharedFlow<BaseResponse<getuser>>()
    val liveAllFollowingByUser=_liveAllFollowingByUser.asSharedFlow()


    private var _PostCommentResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val PostCommentResponse=_PostCommentResponse.asSharedFlow()

    private var _GetPostsWithUserId= MutableSharedFlow<BaseResponse<List<singlePost>>>()
    val GetPostsWithUserId=_GetPostsWithUserId.asSharedFlow()

    private var _GreskaHendler= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val GreskaHendler=_GreskaHendler.asSharedFlow()

    private var _GetCommentsResponseHandler= MutableSharedFlow<BaseResponse<List<singleComment>>>()
    val GetCommentsResponseHandler=_GetCommentsResponseHandler.asSharedFlow()

    private var _LikePostResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val LikePostResponse=_LikePostResponse.asSharedFlow()

    private var _DislikePostResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val DislikePostResponse=_DislikePostResponse.asSharedFlow()

    private var _UnfollowUserResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val UnfollowUserResponse=_UnfollowUserResponse.asSharedFlow()

    private var _FollowUserResponse= MutableSharedFlow<BaseResponse<ResponseBody>>()
    val FollowUserResponse=_FollowUserResponse.asSharedFlow()

    private var _UserInfoResponsee= MutableSharedFlow<BaseResponse<UserInfoResponse>>()
    val UserInfoResponsee=_UserInfoResponsee.asSharedFlow()

    var handleJob: Job?=null

    val exceptionHandler=CoroutineExceptionHandler{_,throwable->onError(
        "ConnectionError"
    )
        Log.d("ExceptionInMainView",throwable.localizedMessage.toString())
    }

    val exceptionHandlerZaSlike=CoroutineExceptionHandler{_,throwable->onError2(
        throwable.localizedMessage?.toString() ?: "Nisam uspeo da procitam gresku onError2"
    )

    }


    private fun onError(greska: String){
        runBlocking{
            launch {
                _GreskaHendler.emit(BaseResponse.Error(greska))
            }
        }
    }

    private fun onError2(greska: String){
        runBlocking{
            launch {
                Log.d("PreuzimanjeSlike",greska)
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

    fun addPost(@Part photos:ArrayList<MultipartBody.Part>, @Part description:MultipartBody.Part, @Part longitude:MultipartBody.Part, @Part latitude:MultipartBody.Part, @Part shortDescription:MultipartBody.Part){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            _DodajPostResposne.emit(BaseResponse.Loading())
            val response=repository.addPost(photos,description,longitude,latitude,shortDescription)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _DodajPostResposne.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _DodajPostResposne.emit(BaseResponse.Error(content))
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

                   val content = response.errorBody()!!.charStream().readText()
                    Log.d("GRES",response.toString())
                    _liveAllFollowingByUser.emit(BaseResponse.Error(content))
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
    fun getCommentsById(postid: Int){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {

            val response=repository.getCommentsWithID(postid)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _GetCommentsResponseHandler.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _GetCommentsResponseHandler.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun getPostsByUserId(userid: Int){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            _GetPostsWithUserId.emit(BaseResponse.Loading())
            val response=repository.getPostsByUserId(userid)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _GetPostsWithUserId.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    Log.d("GRES",response.toString())
                    _GetPostsWithUserId.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun likePost(like:likeDTS){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            _LikePostResponse.emit(BaseResponse.Loading())
            val response=repository.likePost(like)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _LikePostResponse.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _LikePostResponse.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun dislikePost(dislike:likeDTS){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            _DislikePostResponse.emit(BaseResponse.Loading())
            val response=repository.dislikePost(dislike)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _DislikePostResponse.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _DislikePostResponse.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun followUser(UserId:Int){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            val response=repository.FollowUser(UserId)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _FollowUserResponse.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _FollowUserResponse.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun unfollowUser(UserId: Int){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            val response=repository.UnfollowUser(UserId)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _UnfollowUserResponse.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _UnfollowUserResponse.emit(BaseResponse.Error(content))
                }
            }
        }
    }

    fun dajSliku(imageView: ImageView, slikaPath:String,context: Context){
        repository.dajSliku(imageView,slikaPath,context)
    }

    fun getUserWithID(idusera :Int){
        handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
            val response=repository.getUserWithID(idusera)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    _UserInfoResponsee.emit(BaseResponse.Success(response.body()))
                }
                else{
                    val content = response.errorBody()!!.charStream().readText()
                    _UserInfoResponsee.emit(BaseResponse.Error(content))
                }
            }
        }
    }
}