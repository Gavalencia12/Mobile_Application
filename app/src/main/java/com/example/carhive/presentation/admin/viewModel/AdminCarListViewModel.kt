package com.example.carhive.presentation.admin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carhive.data.model.CarEntity
import com.google.firebase.database.*

class AdminCarListViewModel : ViewModel() {

    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> = _carList

    fun getCars() {
        val db = FirebaseDatabase.getInstance().reference.child("Car")


        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cars = mutableListOf<CarEntity>()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        for (carSnapshot in userSnapshot.children) {
                            val car = carSnapshot.getValue(CarEntity::class.java)
                            car?.let {
                                if (!it.approved) {
                                    cars.add(it)
                                }
                            }
                        }
                    }
                    _carList.value = cars
                } else {
                    _carList.value = emptyList()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}
