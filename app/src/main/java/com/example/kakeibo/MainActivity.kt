package com.example.kakeibo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.kakeibo.databinding.ActivityMainBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CAMERA_PERMISSION = 1
    private lateinit var currentPhotoPath: String
    private var sumNumber : Long = 0

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
        binding.addButton.setOnClickListener{addValue()}
        binding.setLimit.setOnClickListener { setLimit() }
    }

    private fun buttonCapture(){
        dispatchTakePictureIntent()
    }

    private fun addValue(){
        binding.sumValue.text=sumNumber.toString()
        if(sumNumber>binding.limitValue.text.toString().toLong()){
            binding.checkValue.text="Over"
            binding.checkValue.setTextColor(Color.RED)
        }else{
            binding.checkValue.text="Save"
            binding.checkValue.setTextColor(Color.GREEN)
        }
    }

    private fun setLimit(){
        binding.limitValue.text=binding.editLimit.text
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                try {
                    createImageFile()?.also{
                        val photoURI: Uri = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider", it)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        showPicture.launch(takePictureIntent)
                    }
                } catch (ex: IOException) {
                    null
                }
        }
    }

    private var showPicture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==0){
            val filepath = File("/storage/self/primary/" +
                    "Android/data/com.example.kakeibo/files/Pictures")
            val imageFile = filepath.walk().filter { it.name.endsWith(".jpeg") }.last()
            imageFile.delete()
        }else{
            imageReaderNew()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null) {
            Log.d("storageDirSize",storageDir.length().toString())
        }
        return if (storageDir != null&&storageDir.length()>0){
            File.createTempFile("JPEG_${timeStamp}_",".jpeg", storageDir
            ).apply { currentPhotoPath = absolutePath }
        }else{
            null
        }
    }

    private fun imageReaderNew() {
        val filepath = File("/storage/self/primary/" +
                "Android/data/com.example.kakeibo/files/Pictures")
        val imageFile = filepath.walk().filter { it.name.endsWith(".jpeg") }.last()
        val fis = FileInputStream(imageFile)
        val bm = BitmapFactory.decodeStream(fis)
        val matrix = Matrix()
        matrix.setRotate(90F)
        val newbitmap =  Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, false)
        imageToText(newbitmap)
        binding.imageView.setImageBitmap(newbitmap)
    }

    private fun imageToText(image :Bitmap){
        val recognizer = TextRecognizer.Builder(this).build()
        val frame = Frame.Builder().setBitmap(image).build()
        val textBlockSparseArray = recognizer.detect(frame)
        var maxTax:Long = 0
        var tax:Long
        for (i in 0 until textBlockSparseArray.size()) {
            val textBlock = textBlockSparseArray.valueAt(i)
            if(Regex("¥").containsMatchIn(textBlock.value.toString())) {
                try {
                    tax = textBlock.value.toString().replace("[^0-9]".toRegex(), "")
                        .toLong()
                }catch(e:NumberFormatException){
                    tax = 0
                }
                if(tax>maxTax) {maxTax=tax}
            }
        }
        binding.captureValue.text = maxTax.toString()
        sumNumber+= maxTax

        Log.d("textBlock",maxTax.toString())
    }
}