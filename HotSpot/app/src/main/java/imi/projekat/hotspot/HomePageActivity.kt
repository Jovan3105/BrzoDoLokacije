package imi.projekat.hotspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import imi.projekat.hotspot.databinding.ActivityHomePageBinding
import imi.projekat.hotspot.ui.FragmentAddPost
import imi.projekat.hotspot.ui.FragmentHome
import imi.projekat.hotspot.ui.FragmentProfilePage
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_main.*

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val firstFragment = FragmentHome()
        val secondFragment = FragmentAddPost()
        val thirdFragment = FragmentProfilePage()

        setCurrentFragment(firstFragment)

        bottomNavigationView2.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(firstFragment)
                R.id.add -> setCurrentFragment(secondFragment)
                R.id.profile -> setCurrentFragment(thirdFragment)

            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            commit()
        }
}