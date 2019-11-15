package cuzhy.com.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import cuzhy.com.mlkit.model.ExtraKey
import kotlinx.android.synthetic.main.activity_main.*
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
        showImage(imageUri)
        detectTextOnDevice(imageUri)
    }

    private fun showImage(uri:Uri) {
        val imagePath = getRealPathFromUri(this, uri)
        var exifInterface = ExifInterface(imagePath)
        var orientation: Int = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver , uri)

        var rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> rotateImage(bitmap, 0f)
        }
        Log.d(TAG, orientation.toString())
        imageView.setImageBitmap(rotatedBitmap)
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0,0, source.width, source.height, matrix, true)
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String {
        var proj = Array<String>(1){MediaStore.Images.Media.DATA}
        var loader = CursorLoader(context, uri, proj, null, null,null)
        var cursor = loader.loadInBackground()
        var columnsIndex: Int = cursor?.getColumnIndex(MediaStore.Images.Media.DATA) as Int
        cursor?.moveToFirst()
        var result = cursor?.getString(columnsIndex)
        cursor?.close()
        return result
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