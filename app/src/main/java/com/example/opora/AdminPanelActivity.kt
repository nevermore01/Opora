package com.example.opora

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AdminPanelActivity : AppCompatActivity() {
    private lateinit var searchField: EditText
    private lateinit var checkBoxAll: CheckBox
    private lateinit var checkBoxWebResources: CheckBox
    private lateinit var checkBoxTelegramChannels: CheckBox
    private lateinit var checkBoxCollections: CheckBox
    private lateinit var checkBoxPlaces: CheckBox
    private lateinit var checkBoxApplications: CheckBox
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminMaterialAdapter

    private val materials = mutableListOf<Material>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        searchField = findViewById(R.id.searchField)
        checkBoxAll = findViewById(R.id.checkBoxAll)
        checkBoxWebResources = findViewById(R.id.checkBoxWebResources)
        checkBoxTelegramChannels = findViewById(R.id.checkBoxTelegramChannels)
        checkBoxCollections = findViewById(R.id.checkBoxCollections)
        checkBoxPlaces = findViewById(R.id.checkBoxPlaces)
        checkBoxApplications = findViewById(R.id.checkBoxApplications)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminMaterialAdapter(this, materials)
        recyclerView.adapter = adapter

        loadMaterials()

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMaterials()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val checkBoxes = listOf(
            checkBoxAll, checkBoxWebResources, checkBoxTelegramChannels,
            checkBoxCollections, checkBoxPlaces, checkBoxApplications
        )

        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) uncheckAllExcept(checkBox)
                filterMaterials()
            }
        }

        val addMaterialButton: Button = findViewById(R.id.addMaterialButton)
        addMaterialButton.setOnClickListener {
            val intent = Intent(this, AddMaterialActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMaterials()  // Завантаження матеріалів при відкритті активності
    }

    private fun loadMaterials() {
        db.collection("materials")
            .get()
            .addOnSuccessListener { documents ->
                materials.clear()
                for (document in documents) {
                    val material = document.toObject(Material::class.java)
                    materials.add(material)
                }
                filterMaterials()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterMaterials() {
        val query = searchField.text.toString().lowercase(Locale.getDefault())
        val filteredMaterials = materials.filter { material ->
            val matchesSearch = material.name.lowercase(Locale.getDefault()).contains(query) ||
                    material.shortDescription.lowercase(Locale.getDefault()).contains(query)

            val matchesCheckBox = when {
                checkBoxAll.isChecked -> true
                checkBoxWebResources.isChecked -> material.type == "Веб-Ресурси"
                checkBoxTelegramChannels.isChecked -> material.type == "Соціальні медіа"
                checkBoxCollections.isChecked -> material.type == "Збори"
                checkBoxPlaces.isChecked -> material.type == "Місця"
                checkBoxApplications.isChecked -> material.type == "Додатки"
                else -> true
            }

            matchesSearch && matchesCheckBox
        }

        adapter.updateMaterials(filteredMaterials)
    }

    private fun uncheckAllExcept(except: CheckBox) {
        val checkBoxes = listOf(
            checkBoxAll, checkBoxWebResources, checkBoxTelegramChannels,
            checkBoxCollections, checkBoxPlaces, checkBoxApplications
        )

        checkBoxes.forEach { checkBox ->
            if (checkBox != except) {
                checkBox.isChecked = false
            }
        }
    }
}
