package com.example.kakeibo

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kakeibo.databinding.ActivityMainBinding
import android.content.Intent;
import android.graphics.Bitmap
import android.provider.MediaStore;
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        binding.buttonCapture.setOnClickListener { buttonCapture() }
        Log.d("check1","onCreate")
    }

    private fun buttonCapture(){
        Log.d("check2","buttonCapture")
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Log.d("check4","dispatch")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            Log.d("check5","dispatch")
            picture.launch(takePictureIntent)
        }
    }

    private var picture = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        Log.d("check3","picture")
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageView.setImageBitmap(imageBitmap)
        }
    }
}