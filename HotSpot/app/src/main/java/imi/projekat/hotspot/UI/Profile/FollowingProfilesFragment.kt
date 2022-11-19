package imi.projekat.hotspot.UI.Profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentFollowingProfilesBinding
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
class FollowingProfilesFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding:FragmentFollowingProfilesBinding
    private lateinit var korisnici:ArrayList<FollowingUserAdapter>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        korisnici=ArrayList()
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
                            val content = it.data!!.followers[i].userPhoto
                            Log.d("slika",content)
                            Log.d("username",it.data!!.followers[i].username)
                            val imageBytes = Base64.decode(content, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            val korisnik=FollowingUserAdapter(it.data!!.followers[i].username,decodedImage)
                            korisnici.add(korisnik)
                        }
                        Log.d("korisnici",korisnici[0].username)
                        binding.listview.adapter=AdapterFollowingProfiles(requireActivity(),korisnici)

                    }
                    //Log.d("SES",it.data!!.users.size.toString())
//                    for (i in 0 until  it.data!!.size){
//                        Log.d("username",it.data[i].username)
//                    }

                }
            }
        }

        return inflater.inflate(R.layout.fragment_following_profiles,container,false)
    }


}