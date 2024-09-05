package com.example.carhive

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.models.Car
import com.google.firebase.database.*

class CarActivity : AppCompatActivity() {

    private lateinit var crud: CRUD
    private lateinit var etCarName: EditText
    private lateinit var btnCreate: Button
    private lateinit var rvCars: RecyclerView
    private lateinit var carAdapter: CarAdapter
    private val carList = mutableListOf<Car>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        // Inicializar Firebase
        val firebaseDatabase = FirebaseDatabase.getInstance()
        crud = CRUD(firebaseDatabase)

        etCarName = findViewById(R.id.etCarName)
        btnCreate = findViewById(R.id.btnCreate)
        rvCars = findViewById(R.id.rvCars)

        rvCars.layoutManager = LinearLayoutManager(this)
        carAdapter = CarAdapter(this, carList, ::onUpdateClick, ::onDeleteClick)
        rvCars.adapter = carAdapter

        btnCreate.setOnClickListener {
            createCar()
        }

        loadCars()
    }

    private fun createCar() {
        val carName = etCarName.text.toString().trim()

        if (carName.isNotEmpty()) {
            val generatedId = FirebaseDatabase.getInstance().getReference("Car").push().key ?: return
            val car = Car(id = generatedId, name = carName)
            crud.create(Car::class.java, car, this)
            loadCars()
        } else {
            Toast.makeText(this, "Por favor, ingresa el nombre del carro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCars() {
        crud.read(Car::class.java, { cars ->
            val previousSize = carList.size
            carList.clear()
            carList.addAll(cars)
            carAdapter.notifyItemRangeRemoved(0, previousSize)
            carAdapter.notifyItemRangeInserted(0, cars.size)
        }, this)
    }



    private fun onUpdateClick(car: Car) {
        etCarName.setText(car.name)
        btnCreate.text = getString(R.string.actualizar_carro)

        // Set up the button to update the car
        btnCreate.setOnClickListener {
            crud.update(Car::class.java, car.id, mapOf("name" to etCarName.text.toString().trim()), this)

            // Clear the EditText after updating
            etCarName.text.clear()

            // Reset the button text back to "Crear Carro"
            btnCreate.text = getString(R.string.crear_carro)

            // Reset the button's click listener to the original create logic
            btnCreate.setOnClickListener {
                createCar()
            }

            // Reload the car list
            loadCars()
        }
    }


    private fun onDeleteClick(car: Car) {
        crud.delete(Car::class.java, car.id, this)
        loadCars()
    }
}
