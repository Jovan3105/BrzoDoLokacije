package imi.projekat.hotspot.UI.LoginRegister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputLayout
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import imi.projekat.hotspot.R
import imi.projekat.hotspot.databinding.FragmentLoginAndRegisterBinding
import kotlinx.android.synthetic.main.fragment_login_and_register.*
import kotlinx.android.synthetic.main.fragment_login_and_register.view.*
import kotlinx.android.synthetic.main.login_tab_fragment.view.*
import kotlinx.android.synthetic.main.register_tab_fragment.view.*


class LoginAndRegister : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_login_and_register, container, false)
    }

    private lateinit var binding: FragmentLoginAndRegisterBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentLoginAndRegisterBinding.bind(view)
        val ltrAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.left_to_right)
        val rtlAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.rigth_to_left)
        val btpAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.bot_to_top)



        binding.logInLayout.ForgotPassword.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.forgotPassword)

        }


        binding.logInLayout.Password.doAfterTextChanged{
            binding.logInLayout.passwordWrapper.endIconMode= TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }

        binding.signUpLayout.Password.doAfterTextChanged {
            binding.signUpLayout.passwordWrapper.endIconMode=TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }

        binding.singUp.setOnClickListener{
            binding.singUp.startAnimation(ltrAnimacija)
            //Toast.makeText(this@LoginActivity, "You clicked on TextView1 'Click Me'.", Toast.LENGTH_SHORT).show()
            binding.singUp.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            binding.singUp.setTextColor(resources.getColor(R.color.white,null))
            binding.logIn.background=null
            binding.logIn.setTextColor(resources.getColor(R.color.pink1,null))
            binding.logInLayout.root.visibility=View.GONE
            binding.signUpLayout.root.visibility=View.VISIBLE

        }

        binding.logIn.setOnClickListener{
            //Toast.makeText(this@LoginActivity, "You clicked on TextView 2'Click Me'.", Toast.LENGTH_SHORT).show()
            binding.logIn.startAnimation(rtlAnimacija)
            binding.logIn.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            binding.logIn.setTextColor(resources.getColor(R.color.white,null))
            binding.singUp.background=null
            binding.singUp.setTextColor(resources.getColor(R.color.pink1,null))
            binding.logInLayout.root.visibility=View.VISIBLE
            binding.signUpLayout.root.visibility=View.GONE

        }

        binding.logInLayout.loginButton.setOnClickListener {
            login()
        }

        binding.signUpLayout.signUpDugme.setOnClickListener {
            signUp()
        }


    }

    private fun login(){
        val Username:String=binding.logInLayout.Email.text.toString().trim()

        if(Username.isBlank()){
            binding.logInLayout.Email.setError(getString(R.string.InsertYourEmailOrUsername))
            return
        }

        val Password:String=binding.logInLayout.Password.text.toString().trim()
        if(Password.isBlank()){
            binding.logInLayout.passwordWrapper.endIconMode=TextInputLayout.END_ICON_NONE
            binding.logInLayout.Password.setError(getString(R.string.InsertYourPassword))
            return
        }
        val obj= loginDTS(Password,Username)
        (activity as LoginActivity?)?.getMyData(obj)
    }

    private fun signUp(){
        var Username:String=binding.signUpLayout.Username.text.toString().trim()
        if(Username.isBlank())
        {
            binding.signUpLayout.Username.setError(getString(R.string.InsertYourUsername))
            return
        }

        var Email:String=binding.signUpLayout.Email.text.toString().trim()
        if(Email.isBlank())
        {
            binding.signUpLayout.Email.setError(getString(R.string.InsertYourEmail))
            return
        }

        var Password:String=binding.signUpLayout.Password.text.toString().trim()
        if(Password.isBlank())
        {
            binding.signUpLayout.passwordWrapper.endIconMode=TextInputLayout.END_ICON_NONE
            binding.signUpLayout.Password.setError(getString(R.string.InsertYourPassword))
            return
        }

        if(!Password.equals(binding.signUpLayout.ConfirmPassword.text.toString()))
        {
            binding.signUpLayout.ConfirmPassword.setError(getString(R.string.PasswordsAreNotTheSame))
            return
        }
        val obj= signUpDTS(Email,Password,Username)
        (activity as LoginActivity?)?.signUp(obj)
    }


}