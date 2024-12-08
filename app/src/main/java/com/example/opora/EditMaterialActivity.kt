package com.example.opora

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditMaterialActivity : AppCompatActivity() {

    private lateinit var materialTypeSpinner: Spinner
    private lateinit var materialName: EditText
    private lateinit var materialShortDescription: EditText
    private lateinit var materialDetailedDescription: EditText
    private lateinit var materialLink: EditText
    private lateinit var materialCollectionAmount: EditText
    private lateinit var materialAddress: EditText
    private lateinit var imagesContainer: LinearLayout
    private lateinit var addImageButton: Button
    private lateinit var saveChangesButton: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var material: Material
    private val newImages = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_material)

        materialTypeSpinner = findViewById(R.id.materialTypeSpinner)
        materialName = findViewById(R.id.materialName)
        materialShortDescription = findViewById(R.id.materialShortDescription)
        materialDetailedDescription = findViewById(R.id.materialDetailedDescription)
        materialLink = findViewById(R.id.materialLink)
        materialCollectionAmount = findViewById(R.id.materialCollectionAmount)
        materialAddress = findViewById(R.id.materialAddress)
        imagesContainer = findViewById(R.id.imagesContainer)
        addImageButton = findViewById(R.id.addImageButton)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        material = intent.getSerializableExtra("material") as Material

        if (material.id.isEmpty()) {
            Toast.makeText(this, "Помилка: матеріал не має ідентифікатора", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        materialName.setText(material.name)
        materialShortDescription.setText(material.shortDescription)
        materialDetailedDescription.setText(material.detailedDescription)
        materialLink.setText(material.link)
        materialCollectionAmount.setText(material.collectionAmount)
        materialAddress.setText(material.address)

        material.imageUrls.forEach { imageUrl ->
            addImageView(imageUrl)
        }

        when (material.type) {
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

        materialTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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

        addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        saveChangesButton.setOnClickListener {
            val updatedMaterial = material.copy(
                name = materialName.text.toString(),
                shortDescription = materialShortDescription.text.toString(),
                detailedDescription = materialDetailedDescription.text.toString(),
                link = materialLink.text.toString(),
                collectionAmount = materialCollectionAmount.text.toString(),
                address = materialAddress.text.toString(),
                type = materialTypeSpinner.selectedItem.toString()
            )

            if (newImages.isNotEmpty()) {
                uploadImages(newImages) { imageUrls ->
                    val updatedImageUrls = (material.imageUrls + imageUrls).toMutableList()
                    db.collection("materials").document(material.id)
                        .set(updatedMaterial.copy(imageUrls = updatedImageUrls))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Зміни збережено", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Помилка при збереженні змін", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                db.collection("materials").document(material.id)
                    .set(updatedMaterial)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Зміни збережено", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Помилка при збереженні змін", Toast.LENGTH_SHORT).show()
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
                    newImages.add(imageUri)
                    addImageView(imageUri.toString())
                }
            } ?: data?.data?.let { imageUri ->
                newImages.add(imageUri)
                addImageView(imageUri.toString())
            }
        }
    }

    private fun addImageView(imageUrl: String) {
        val container = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        val imageView = ImageView(this).apply {
            Glide.with(this@EditMaterialActivity).load(imageUrl).into(this)
            layoutParams = FrameLayout.LayoutParams(300, 300)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val removeButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_close)
            layoutParams = FrameLayout.LayoutParams(50, 50).apply {
                gravity = Gravity.END or Gravity.TOP
            }
            background = null
            setOnClickListener {
                imagesContainer.removeView(container)
                material.imageUrls.remove(imageUrl)
            }
        }

        container.addView(imageView)
        container.addView(removeButton)
        imagesContainer.addView(container)
    }

    private fun uploadImages(images: List<Uri>, onComplete: (List<String>) -> Unit) {
        if (images.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val imageUrls = mutableListOf<String>()
        val storageRef = storage.reference

        for ((index, imageUri) in images.withIndex()) {
            val imageRef = storageRef.child("materials/${System.currentTimeMillis()}_$index.jpg")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        if (imageUrls.size == images.size) {
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
}
