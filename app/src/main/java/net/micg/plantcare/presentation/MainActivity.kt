package net.micg.plantcare.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.micg.plantcare.receiver.alarm.AlarmReceiver
import net.micg.plantcare.R
import net.micg.plantcare.databinding.ActivityMainBinding
import net.micg.plantcare.presentation.alarms.AlarmsFragmentDirections
import net.micg.plantcare.presentation.articles.ArticlesFragmentDirections

class MainActivity : AppCompatActivity() {
    private val destinationFragmentIdToNavItemMapId = mapOf(
        R.id.alarmsFragment to R.id.alarmsFragment,
        R.id.alarmCreationFragment to R.id.alarmsFragment,
        R.id.articlesFragment to R.id.articlesFragment,
        R.id.articleFragment to R.id.articlesFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setUpActivity()
        handleIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101
            )
        }

        if (intent != null && intent.extras != null) {
            val url = intent.getStringExtra("url")

            if (url != null) {
                startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation)
    }

    private fun setUpActivity() = with(ActivityMainBinding.inflate(layoutInflater)) {
        setContentView(root)

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = (fragment as NavHostFragment).navController

        setUpNavigation(bottomNavView, navController)
        setUpBottomNavAnimation(bottomNavView, navController)
    }

    private fun setUpNavigation(
        bottomNavView: BottomNavigationView, navController: NavController,
    ) = bottomNavView.setOnItemSelectedListener { item ->
        navController.navigate(
            when (item.itemId) {
                R.id.alarmsFragment -> AlarmsFragmentDirections.actionGlobalAlarmsFragment()
                R.id.articlesFragment -> ArticlesFragmentDirections.actionGlobalArticlesFragment()
                else -> AlarmsFragmentDirections.actionGlobalAlarmsFragment()
            },
            NavOptions.Builder().setPopUpTo(item.itemId, true).build()
        )
        true
    }

    private fun setUpBottomNavAnimation(
        bottomNavView: BottomNavigationView, navController: NavController,
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavView.menu.findItem(
                destinationFragmentIdToNavItemMapId[destination.id] ?: R.id.articlesFragment
            )?.isChecked = true
        }
    }

    private fun handleIntent() {
        when (intent.getStringExtra(AlarmReceiver.FRAGMENT_TAG) ?: return) {
            AlarmReceiver.ALARMS_FRAGMENT_TAG -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                (fragment as NavHostFragment).navController.navigate(
                    ArticlesFragmentDirections.actionArticlesFragmentToAlarmsFragment()
                )
            }
        }
    }
}
