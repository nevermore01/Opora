package com.example.opora
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Random

class AddMaterialActivity : AppCompatActivity() {

    private lateinit var materialTypeSpinner: Spinner
    private lateinit var materialName: EditText
    private lateinit var materialShortDescription: EditText
    private lateinit var materialDetailedDescription: EditText
    private lateinit var materialLink: EditText
    private lateinit var materialCollectionAmount: EditText
    private lateinit var materialAddress: EditText
    private lateinit var selectImagesButton: Button
    private lateinit var imagesContainer: LinearLayout
    private lateinit var addMaterialButton: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val selectedImages = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_material)

        materialTypeSpinner = findViewById(R.id.materialTypeSpinner)
        materialName = findViewById(R.id.materialName)
        materialShortDescription = findViewById(R.id.materialShortDescription)
        materialDetailedDescription = findViewById(R.id.materialDetailedDescription)
        materialLink = findViewById(R.id.materialLink)
        materialCollectionAmount = findViewById(R.id.materialCollectionAmount)
        materialAddress = findViewById(R.id.materialAddress)
        selectImagesButton = findViewById(R.id.selectImagesButton)
        imagesContainer = findViewById(R.id.imagesContainer)
        addMaterialButton = findViewById(R.id.addMaterialButton)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        materialTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (parent.getItemAtPosition(position).toString()) {
                    "Збори" -> {
                        materialCollectionAmount.visibility = View.VISIBLE
                        materialAddress.visibility = View.GONE
                    }
                    "Місця" -> {
                        materialCollectionAmount.visibility = View.GONE
                        materialAddress.visibility = View.VISIBLE
                    }
                    else -> {
                        materialCollectionAmount.visibility = View.GONE
                        materialAddress.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        selectImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        addMaterialButton.setOnClickListener {
            val id = generateRandomId() // Генерація випадкового ID
            val name = materialName.text.toString()
            val shortDescription = materialShortDescription.text.toString()
            val detailedDescription = materialDetailedDescription.text.toString()
            val link = materialLink.text.toString()
            val collectionAmount = materialCollectionAmount.text.toString()
            val address = materialAddress.text.toString()
            val type = materialTypeSpinner.selectedItem.toString()

            if (name.isEmpty() || shortDescription.isEmpty() || detailedDescription.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val material = hashMapOf<String, Any>(
                "id" to id, // Додавання ID до матеріалу
                "name" to name,
                "shortDescription" to shortDescription,
                "detailedDescription" to detailedDescription,
                "link" to link,
                "type" to type
            )

            when (type) {
                "Збори" -> material["collectionAmount"] = collectionAmount
                "Місця" -> material["address"] = address
            }

            db.collection("materials")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        uploadImages { imageUrls ->
                            if (imageUrls.isNotEmpty()) {
                                material["imageUrls"] = imageUrls
                            }
                            db.collection("materials")
                                .document(id) // Використання ID як документа
                                .set(material)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Матеріал додано успішно", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Помилка при додаванні матеріалу", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Матеріал з такою назвою вже існує", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    selectedImages.add(imageUri)
                    addImageView(imageUri)
                }
            } ?: data?.data?.let { imageUri ->
                selectedImages.add(imageUri)
                addImageView(imageUri)
            }
        }
    }

    private fun addImageView(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            setImageURI(imageUri)
            layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                setMargins(8, 8, 8, 8)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val removeButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_close)
            layoutParams = FrameLayout.LayoutParams(50, 50).apply {
                gravity = Gravity.END or Gravity.TOP
            }
            background = null
            setOnClickListener {
                imagesContainer.removeView(imageView.parent as View)
                selectedImages.remove(imageUri)
            }
        }

        val container = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(imageView)
            addView(removeButton)
        }

        imagesContainer.addView(container)
    }

    private fun uploadImages(onComplete: (List<String>) -> Unit) {
        if (selectedImages.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val imageUrls = mutableListOf<String>()
        val storageRef = storage.reference

        for ((index, imageUri) in selectedImages.withIndex()) {
            val imageRef = storageRef.child("materials/${System.currentTimeMillis()}_$index.jpg")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        if (imageUrls.size == selectedImages.size) {
                            onComplete(imageUrls)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Помилка при завантаженні фото", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    private fun generateRandomId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val random = Random()
        val idBuilder = StringBuilder()

        repeat(7) {
            idBuilder.append(digits[random.nextInt(digits.length)])
        }

        repeat(3) {
            idBuilder.append(chars[random.nextInt(chars.length)])
        }

        return idBuilder.toString()
    }
}
