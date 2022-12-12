package imi.projekat.hotspot.UI.Profile

import android.R
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.ActivityMainBinding
import imi.projekat.hotspot.databinding.FragmentFollowingProfilesBinding
import kotlinx.android.synthetic.main.fragment_following_profiles.*
import kotlinx.android.synthetic.main.fragment_single_post.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class FollowingProfilesFragment : Fragment(),FollowersImages,AdapterFollowingProfiles.OnItemClickListener{
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding:FragmentFollowingProfilesBinding
    private lateinit var korisnici:ArrayList<FollowingUserAdapter>
    private var layoutManager:RecyclerView.LayoutManager?=null
    private var adapter:RecyclerView.Adapter<AdapterFollowingProfiles.ViewHolder>?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentFollowingProfilesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.liveAllFollowingByUser.collectLatest{
                if(it is BaseResponse.Error){

                }
                if(it is BaseResponse.Success){
                    if(it.data!!.followers.size==0)
                    {
                        recycler.visibility=View.GONE
                        PostsOnMapButton.visibility=View.VISIBLE
                        tekstView.visibility=View.VISIBLE

                        PostsOnMapButton.setOnClickListener{

                            (activity as MainActivity).bottomMenu.getItem(0).setChecked(true)
                            (activity as MainActivity).bottomMenu.performIdentifierAction((activity as MainActivity).bottomMenu.getItem(0).itemId,0)
                        }
                    }
                    else
                    {
                        korisnici=ArrayList()
                        for (i in 0 until  it.data!!.followers.size){
                            var korisnik:FollowingUserAdapter
                            if(it.data!!.followers[i].userPhoto.isNullOrEmpty())
                            {
                                korisnik=FollowingUserAdapter(it.data!!.followers[i].id,it.data!!.followers[i].username,"","Unfollow")
                            }
                            else
                            {

                                korisnik=FollowingUserAdapter(it.data!!.followers[i].id,it.data!!.followers[i].username,"ProfileImages/"+it.data!!.followers[i].userPhoto,"Unfollow")
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

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.UnfollowUserResponse.collectLatest{
                if(it is BaseResponse.Error){
                }
                if(it is BaseResponse.Success){
                    val content = it.data!!.charStream().readText().toInt()
                    val pom=korisnici.filter { s->s.id==content }.first()
                    pom.buttonName="Follow"
                    adapter!!.notifyItemChanged(korisnici.indexOf(pom))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.FollowUserResponse.collectLatest{
                if(it is BaseResponse.Error){
                }
                if(it is BaseResponse.Success){
                    val content = it.data!!.charStream().readText().toInt()
                    val pom=korisnici.filter { s->s.id==content }.first()
                    pom.buttonName="Unfollow"
                    adapter!!.notifyItemChanged(korisnici.indexOf(pom))
                }
            }
        }
        viewModel.GetAllFollowingByUser()
    }


    override fun onItemClickFollow(position: Int) {
       val clickedItem=korisnici[position]
        if(clickedItem.buttonName=="Unfollow")
        {
            viewModel.unfollowUser(clickedItem.id)
        }
        else
        {
            viewModel.followUser(clickedItem.id)
        }

    }

    override fun onItemClickProfile(position: Int) {
        val clickedItem=korisnici[position]
        val action: NavDirections =FollowingProfilesFragmentDirections.actionFollowingProfilesFragmentToDrugiKorisnik(clickedItem.id)
        findNavController().navigate(action)
    }

    override fun getPicture(imageView: ImageView, slika: String) {
        viewModel.dajSliku(imageView, slika,requireContext())
    }


}