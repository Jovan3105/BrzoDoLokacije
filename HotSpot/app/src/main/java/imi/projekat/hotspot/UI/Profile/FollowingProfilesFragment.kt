package imi.projekat.hotspot.UI.Profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentFollowingProfilesBinding
import kotlinx.android.synthetic.main.fragment_following_profiles.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowingProfilesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowingProfilesFragment : Fragment(),FollowersImages,AdapterFollowingProfiles.OnItemClickListener{
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding:FragmentFollowingProfilesBinding
    private lateinit var korisnici:ArrayList<FollowingUserAdapter>
    private var layoutManager:RecyclerView.LayoutManager?=null
    private var adapter:RecyclerView.Adapter<AdapterFollowingProfiles.ViewHolder>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        korisnici=ArrayList()



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel.GetAllFollowingByUser()

        binding=FragmentFollowingProfilesBinding.inflate(inflater)
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.liveAllFollowingByUser.collectLatest{
                if(it is BaseResponse.Error){
                    Log.d("greska","Nije dobar zahtev")
                }
                if(it is BaseResponse.Success){
                    if(it.data!!.followers==null)
                    {
                        Toast.makeText(requireContext(), "Nema followinga", Toast.LENGTH_SHORT).show()
                        Log.d("sve je dobro","Dobro je")
                    }
                    else
                    {
                        for (i in 0 until  it.data!!.followers.size){
                            var korisnik:FollowingUserAdapter
                            if(it.data!!.followers[i].userPhoto.isNullOrEmpty())
                            {
                                korisnik=FollowingUserAdapter(it.data!!.followers[i].id,it.data!!.followers[i].username,"","Unfollow")
                            }
                            else
                            {
                                val pom=it.data!!.followers[i].userPhoto.split("\\")
                                 korisnik=FollowingUserAdapter(it.data!!.followers[i].id,it.data!!.followers[i].username,pom[2],"Unfollow")
                            }

                            korisnici.add(korisnik)
                        }

                        layoutManager=LinearLayoutManager(requireContext())
                        adapter=AdapterFollowingProfiles(this@FollowingProfilesFragment,korisnici,this@FollowingProfilesFragment)
                        recycler.layoutManager=layoutManager
                        recycler.adapter=adapter


                    }

                }
            }
        }



        return inflater.inflate(R.layout.fragment_following_profiles,container,false)
    }

//    override fun onItemClick(position: Int) {
//        Log.d("adapter",adapter.toString())
//       val clickedItem=korisnici[position]
//        Log.d("kliknuto",clickedItem.toString())
//        if(clickedItem.buttonName=="Unfollow")
//        {
//            //anfollow zahtev
//            Log.d("Anfollow uslov","Usao sam")
//            viewLifecycleOwner.lifecycleScope.launch{
//                viewModel.UnfollowUserResponse.collectLatest{
//                    if(it is BaseResponse.Error){
//                        Log.d("greska","Nije dobar zahtev za follow korisnika")
//                    }
//                    if(it is BaseResponse.Success){
//                        Log.d("Zahtev za foolow","Uspesan")
//                        clickedItem.buttonName="Follow"
//                        adapter!!.notifyItemChanged(position)
//                    }
//                }
//            }
//            viewModel.unfollowUser(clickedItem.id)
//
//
//        }
//        else if(clickedItem.buttonName=="Follow")
//        {
//            viewModel.followUser(clickedItem.id)
//            viewLifecycleOwner.lifecycleScope.launch{
//                viewModel.FollowUserResponse.collectLatest{
//                    if(it is BaseResponse.Error){
//                        Log.d("greska","Nije dobar zahtev za follow korisnika")
//                    }
//                    if(it is BaseResponse.Success){
//                        Log.d("Zahtev za foolow","Uspesan")
//                        clickedItem.buttonName="Unfollow"
//                        adapter!!.notifyItemChanged(position)
//                    }
//                }
//            }
//
//        }
//        else
//        {
//            findNavController().navigate(R.id.action_followingProfilesFragment_to_drugi_korisnik)
//        }
//        Log.d("usernamekliknutog",clickedItem.username)
//
//    }

    override fun getPicture(imageView: ImageView, slika: String) {
        Glide.with(this)
            .load("http://10.0.2.2:5140/Storage/ProfileImages/$slika")
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.image_holder)
            .into(imageView)
    }

    override fun onItemClickFollow(position: Int) {
       val clickedItem=korisnici[position]
        if(clickedItem.buttonName=="Unfollow")
        {
            viewLifecycleOwner.lifecycleScope.launch{
                viewModel.UnfollowUserResponse.collectLatest{
                    if(it is BaseResponse.Error){
                        Log.d("greska","Nije dobar zahtev za follow korisnika")
                    }
                    if(it is BaseResponse.Success){
                        clickedItem.buttonName="Follow"
                        adapter!!.notifyItemChanged(position)
                    }
                }
            }
            viewModel.unfollowUser(clickedItem.id)


        }
        else
        {
            viewModel.followUser(clickedItem.id)
            viewLifecycleOwner.lifecycleScope.launch{
                viewModel.FollowUserResponse.collectLatest{
                    if(it is BaseResponse.Error){
                        Log.d("greska","Nije dobar zahtev za follow korisnika")
                    }
                    if(it is BaseResponse.Success){
                        clickedItem.buttonName="Unfollow"
                        adapter!!.notifyItemChanged(position)
                    }
                }
            }

        }

    }

    override fun onItemClickProfile(position: Int) {
        val clickedItem=korisnici[position]
        Log.d("profile Funkcija",korisnici[position].toString())
        val action: NavDirections =FollowingProfilesFragmentDirections.actionFollowingProfilesFragmentToDrugiKorisnik(clickedItem.id)
        findNavController().navigate(action)
    }


}