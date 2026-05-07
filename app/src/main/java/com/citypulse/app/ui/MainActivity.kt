package com.citypulse.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.citypulse.app.databinding.ActivityMainBinding
import com.citypulse.app.util.PermissionManager
class MainActivity:AppCompatActivity(){
    private lateinit var binding:ActivityMainBinding
//Instancier ici—registerForActivityResultdoitêtreappeléavantonCreate()
    private lateinit var permissionManager:PermissionManager
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager=PermissionManager(this)
//Demanderlalocalisationaudémarrage
        requestLocationPermissions()
    }
//AppelabledepuislesFragments:(requireActivity()asMainActivity).requestLocationPermissions()
    fun requestLocationPermissions(onGranted:()->Unit={},onDenied:()->Unit={}){
        permissionManager.requestLocationPermissions(
            onGranted={onGranted()},
            onDenied={
                Toast.makeText(this,"Mode dégradé:carte désactivée.",Toast.LENGTH_LONG).show()
                onDenied()
            }
        )
    }
    fun requestNotificationPermission(onGranted:()->Unit={},onDenied:()->Unit={}){
        permissionManager.requestNotificationPermission(onGranted,onDenied)
    }
}