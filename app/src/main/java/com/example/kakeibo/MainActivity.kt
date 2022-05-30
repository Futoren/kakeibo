package com.example.kakeibo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.util.forEach
import com.example.kakeibo.databinding.ActivityMainBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CAMERA_PERMISSION = 1
    private lateinit var currentPhotoPath: String

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
        imageReaderNew()
    }

    private fun buttonCapture(){
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    showPicture.launch(takePictureIntent)
                }
        }
    }

    private var showPicture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){}

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpeg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun imageReaderNew() {
        val filepath = File("/storage/self/primary/" +
                "Android/data/com.example.kakeibo/files/Pictures")
        val imageFile = filepath.walk().filter { it.name.endsWith(".jpeg") }.first()
        val fis = FileInputStream(imageFile)
        val bm = BitmapFactory.decodeStream(fis)
        val matrix = Matrix()
        matrix.setRotate(90F)
        val newbitmap =  Bitmap.createBitmap(
            bm,
            0,
            0,
            bm.width,
            bm.height,
            matrix,
            false
        )
        imageToText(newbitmap)
        binding.imageView.setImageBitmap(newbitmap)
    }

    private fun imageToText(image :Bitmap){
        val recognizer = TextRecognizer.Builder(this).build()
        val frame = Frame.Builder().setBitmap(image).build()
        val textBlockSparseArray = recognizer.detect(frame)
        val stringBuilder = StringBuilder()
        for (i in 0 until textBlockSparseArray.size()) {
            val textBlock = textBlockSparseArray.valueAt(i)
            stringBuilder.append(textBlock.value)
        }
        Log.d("textBlock",stringBuilder.toString())
    }
}