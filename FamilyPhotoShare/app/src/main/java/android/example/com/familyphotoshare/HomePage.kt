package android.example.com.familyphotoshare

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home_page.*
import java.io.IOException
import com.android.volley.Response
import com.android.volley.toolbox.Volley

class HomePage : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageButton: Button
    //private lateinit var sendButton: Button
    private var imageData: ByteArray? = null
    //private val postURL: String = "https://photos.google.com/albums"
    private val postURL: String = "https://ptsv2.com/t/54odo-1576291398/post" // remember to use your own api

    companion object {
        private const val IMAGE_PICK_CODE = 998
        private const val UPLOAD_IMAGE_PICK_CODE = 999
        private const val PERMISSION_CODE = 1000;
        private const val IMAGE_CAPTURE_CODE = 1001;
        var image_uri: Uri? = null
    }

//    private val IMAGE_PICK_CODE = 1002;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //Button click Photos
        viewPictures.setOnClickListener {
            //this checks runtime permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {

                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    //permission has been granted
                    pickImageFromGallery();

                }
            } else {
                //for systems running on Marshmallow and above
                pickImageFromGallery();
            }
        }

        //Button click Camera
        takePicture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                    //permission was not enabled
                    val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else {
                    openCamera()
                }
            }
            else {
                openCamera()
            }
        }

        //imageView = findViewById(R.id.imageView)

//        imageButton = findViewById(R.id.imageButton)
//        imageButton.setOnClickListener {
//            launchGallery()
//        }
//        sendButton = findViewById(R.id.sendButton)
//        sendButton.setOnClickListener {
//            uploadImage()
//        }

        sendButton.setOnClickListener {
            Toast.makeText(this, "sendButton clicked", Toast.LENGTH_SHORT).show()
            uploadImage()
        }
    }

//    private fun launchGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, IMAGE_PICK_CODE)
//    }

    private fun uploadImage() {
        imageData?: return
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                println("response is: $it")
            },
            Response.ErrorListener {
                println("error is: $it")
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && permissions[0] == "android.permission.READ_EXTERNAL_STORAGE") {
                    pickImageFromGallery()
                }
                else if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && permissions[0] == "android.permission.CAMERA") {
                    openCamera()
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == UPLOAD_IMAGE_PICK_CODE) {
            val uri = data?.data
            if (uri != null) {
                imageView.setImageURI(uri)
                createImageData(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView.setImageURI(data?.data)
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            imageView.setImageURI(data?.data)
        }
    }

}
