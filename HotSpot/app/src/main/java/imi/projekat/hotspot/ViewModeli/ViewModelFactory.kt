package imi.projekat.hotspot.ViewModeli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import imi.projekat.hotspot.Ostalo.Repository

class ViewModelFactory(val repository: Repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LoginActivityViewModel::class.java)){
            return LoginActivityViewModel(this.repository) as T
        }
        throw IllegalArgumentException("ViewModel nije pronadjen")

    }
}