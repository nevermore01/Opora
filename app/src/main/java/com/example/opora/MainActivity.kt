package com.example.opora

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var searchField: EditText
    private lateinit var checkBoxAll: CheckBox
    private lateinit var checkBoxWebResources: CheckBox
    private lateinit var checkBoxTelegramChannels: CheckBox
    private lateinit var checkBoxCollections: CheckBox
    private lateinit var checkBoxPlaces: CheckBox
    private lateinit var checkBoxApplications: CheckBox
    private lateinit var checkBoxFavorites: CheckBox
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MaterialAdapter
    private val materials = mutableListOf<Material>()
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE)
        val instructionsShown = sharedPreferences.getBoolean("instructions_shown", false)

        if (!instructionsShown) {
            val intent = Intent(this, InstructionActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        searchField = findViewById(R.id.searchField)
        checkBoxAll = findViewById(R.id.checkBoxAll)
        checkBoxWebResources = findViewById(R.id.checkBoxWebResources)
        checkBoxTelegramChannels = findViewById(R.id.checkBoxTelegramChannels)
        checkBoxCollections = findViewById(R.id.checkBoxCollections)
        checkBoxPlaces = findViewById(R.id.checkBoxPlaces)
        checkBoxApplications = findViewById(R.id.checkBoxApplications)
        checkBoxFavorites = findViewById(R.id.checkBoxFavorites)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MaterialAdapter(this, materials)
        recyclerView.adapter = adapter

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMaterials()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        setupCheckBox(checkBoxAll)
        setupCheckBox(checkBoxWebResources)
        setupCheckBox(checkBoxTelegramChannels)
        setupCheckBox(checkBoxCollections)
        setupCheckBox(checkBoxPlaces)
        setupCheckBox(checkBoxApplications)
        setupCheckBox(checkBoxFavorites)

        val adminPanelLink: TextView = findViewById(R.id.adminPanelLink)
        adminPanelLink.setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        loadMaterials()
        adapter.refreshFavorites()
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
        val query = searchField.text?.toString()?.lowercase(Locale.getDefault()) ?: ""
        val filteredMaterials = materials.filter { material ->
            val matchesSearch = (material.name?.lowercase(Locale.getDefault())?.contains(query) ?: false) ||
                    (material.shortDescription?.lowercase(Locale.getDefault())?.contains(query) ?: false)
            Log.d("Filter", "Material: $material")
            val matchesCheckBox = when {
                checkBoxAll.isChecked -> true
                checkBoxWebResources.isChecked -> material.type == "Веб-ресурси"
                checkBoxTelegramChannels.isChecked -> material.type == "Соціальні медіа"
                checkBoxCollections.isChecked -> material.type == "Збори"
                checkBoxPlaces.isChecked -> material.type == "Місця"
                checkBoxApplications.isChecked -> material.type == "Додатки"
                checkBoxFavorites.isChecked -> adapter.isFavorite(material)
                else -> true
            }

            matchesSearch && matchesCheckBox
        }

        adapter.updateMaterials(filteredMaterials)

    }


    private fun uncheckAllExcept(except: CheckBox) {
        listOf(
            checkBoxAll, checkBoxWebResources, checkBoxTelegramChannels,
            checkBoxCollections,  checkBoxPlaces, checkBoxApplications, checkBoxFavorites
        ).forEach { checkBox ->
            if (checkBox != except) {
                checkBox.isChecked = false
            }
        }
    }

    private fun setupCheckBox(checkBox: CheckBox) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                uncheckAllExcept(checkBox)
            }
            filterMaterials()
        }
    }
}
