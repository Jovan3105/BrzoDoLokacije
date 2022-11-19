package imi.projekat.hotspot.UI.HomePage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentHomePageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomePageFragment : Fragment() {

    private lateinit var binding: FragmentHomePageBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var listaPostova:List<singlePost>
    private lateinit var listaPostovaAdapter: ListaPostovaAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomePageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listaPostovaInit()
        val layoutManager=LinearLayoutManager(context)

        recyclerView=view.findViewById(R.id.ListaPostovaRecyclerView)
        recyclerView.setHasFixedSize(true)
        listaPostovaAdapter= ListaPostovaAdapter(listaPostova)

        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.setLayoutManager(llm)
        recyclerView.setAdapter(listaPostovaAdapter)



        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.GetPostsWithUserId.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    if(it.data!=null){
                        listaPostova=it.data
                        listaPostovaAdapter.update(listaPostova)
                    }
                }
            }
        }



        viewModel.getPostsByUserId(1)

//        binding.button6.setOnClickListener {
//            viewModel.getPostsByUserId(1)
//            //findNavController().navigate(R.id.action_homePageFragment_to_singlePostFragment)
//        }



    }

    private fun listaPostovaInit(){
        listaPostova= arrayListOf<singlePost>()
    }


}