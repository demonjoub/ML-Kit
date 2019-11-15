package cuzhy.com.mlkit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import cuzhy.com.mlkit.model.ExtraKey
import kotlinx.android.synthetic.main.activity_results.view.*
import java.io.IOException

class ResultsActivity: AppCompatActivity() {

    private var textView: TextView? = null

    companion object {
        const val TAG = "ResultsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "${TAG}.onCreate")
        setContentView(R.layout.activity_results)
        val imagePath = intent.getStringExtra(ExtraKey.EXTRA_SESSION_ID)
        val imageView = findViewById(R.id.imageView) as ImageView
        textView = findViewById(R.id.textView) as TextView
        val imageUri = Uri.parse(imagePath)
        imageView.setImageURI(imageUri)
        detectTextOnDevice(imageUri)
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
                        textView?.text = firebaseVisionText.text
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d(TAG, "Task failed with an exception ${e}")
                        textView?.text = e.toString()
                    }
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}