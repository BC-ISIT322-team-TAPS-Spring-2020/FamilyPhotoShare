package android.example.com.familyphotoshare

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home_page.*

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File

class HomePage : AppCompatActivity() {
    lateinit var storageReference:StorageReference

    companion object {
        private val PERMISSION_CODE = 1000;
        private val IMAGE_CAPTURE_CODE = 1001;
        private val IMAGE_PICK_CODE = 1002;
        private val PICK_IMAGE_CODE = 1003;
        var image_uri: Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //        var file = "1592346486601.jpg"
        var filepath = "/storage/emulated/0/Pictures/1592346486601.jpg"

        storageReference = FirebaseStorage.getInstance().getReference(filepath);

        //family-photo-share-41b26
        //AIzaSyBG0SNhEX7bTfKcekJ6ffbEM5a8dX7uprQ

        //Button click
        viewPictures.setOnClickListener {
            //this checks runtime permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {

                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
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

        takePicture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                    //permission was not enabled
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

        uploadPictures.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE)
            //uploadImage()
        }
    }

    private fun uploadImage() {


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
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
//            imageView.setImageURI(data?.data)
//        }
        if (requestCode == IMAGE_PICK_CODE) {
            imageView.setImageURI(data?.data)
        }
        else if (requestCode == PICK_IMAGE_CODE) {
            Toast.makeText(this, "PICK_IMAGE_CODE", Toast.LENGTH_SHORT).show()
            val uploadTask = storageReference!!.putFile(data!!.data!!)
            val task = uploadTask.continueWithTask{
                task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@HomePage, "Failed", Toast.LENGTH_SHORT).show()
                }
                storageReference!!.downloadUrl
            }.addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    val downloadUri = task.result
                    val url = downloadUri!!.toString()
                    Log.d("DIRECTLINK", url)
                }
            }
        }
//        else if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
//            imageView.setImageURI(data?.data)
//        }
    }

}
