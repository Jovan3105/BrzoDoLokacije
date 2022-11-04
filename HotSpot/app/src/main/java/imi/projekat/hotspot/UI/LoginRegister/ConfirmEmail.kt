package imi.projekat.hotspot.UI.LoginRegister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import imi.projekat.hotspot.Dialogs.LoadingDialog
import imi.projekat.hotspot.R
import imi.projekat.hotspot.databinding.FragmentConfirmEmailBinding


class ConfirmEmail : Fragment() {

    private lateinit var binding: FragmentConfirmEmailBinding
    private val args:ConfirmEmailArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_confirm_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var token=args.confirmationToken
        Toast.makeText(requireContext(), token, Toast.LENGTH_SHORT).show()
        //val dijalog= LoadingDialog(requireActivity())

    }
}