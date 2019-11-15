package cuzhy.com.mlkit

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE: Int = 1000
    private val IMAGE_CAPTURE_CODE: Int = 1001
    private var imageView: ImageView? = null
    var image_uri: Uri? = null
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        main()
    }

    private fun main() {
        var btnCamera = findViewById(R.id.buttonCamera) as Button
        imageView = findViewById(R.id.imageView) as ImageView

        btnCamera.setOnClickListener { view ->
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                // permission was not enable
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                // permission already granted
                openCamera()
            }
        }

    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        Toast.makeText(this, "Open Camera", Toast.LENGTH_SHORT).show()
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun detectTextOnDevice(uri: Uri?) {
        if (uri == null) {
            Log.d(TAG, "detectTextOnDevice() result is null")
        } else {
            // 1
            try {
                val image = FirebaseVisionImage.fromFilePath(this, uri)
                val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
                val result = detector.processImage(image)
                    .addOnSuccessListener { firebaseVisionText ->
                        // Task completed successfully
                        Log.d(TAG, "Task completed successfully ${firebaseVisionText}")
//                        resultText(firebaseVisionText)
                        Log.d(TAG, "show text => ${firebaseVisionText.text}")
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d(TAG, "Task failed with an exception ${e}")
                    }
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            imageView?.setImageURI(image_uri)
            detectTextOnDevice(image_uri)
        }
    }
}
