package com.example.carhive

import android.content.Context
import android.widget.Toast
import com.example.carhive.models.Car
import com.google.firebase.database.*

class CRUD(private val firebaseDatabase: FirebaseDatabase, private var currentToast: Toast?) {

    // Generate a random ID for a given type
    fun generateId(collectionName: String): String? {
        return firebaseDatabase.getReference(collectionName).push().key
    }

    fun create(entityClass: Class<Car>, entity: Car, context: Context) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.child(entity.id).setValue(entity)
            .addOnSuccessListener {
                showToast(context, "$entityName created successfully")
            }
            .addOnFailureListener {
                showToast(context, "Failed to create $entityName")
            }
    }

    // Generic read function to retrieve all entities of a certain type
    fun <T> read(entityClass: Class<T>, callback: (List<T>) -> Unit, context: Context) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entities = mutableListOf<T>()
                for (entitySnapshot in snapshot.children) {
                    val entity = entitySnapshot.getValue(entityClass)
                    entity?.let { entities.add(it) }
                }
                callback(entities)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(context, "Failed to load $entityName")
            }
        })
    }

    // Generic read all function to retrieve all instances of Car
    fun readAll(entityClass: Class<Car>, context: Context, callback: (List<Car>) -> Unit) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cars = mutableListOf<Car>()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(entityClass)
                    car?.let { cars.add(it) }
                }
                callback(cars)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(context, "Failed to load $entityName")
            }
        })
    }

    // Generic update function to update an entity only if it exists
    fun <T> updateEntityIfExists(
        entityClass: Class<T>,
        entityId: String,
        updates: Map<String, Any>,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.child(entityId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    reference.child(entityId).updateChildren(updates)
                        .addOnSuccessListener {
                            showToast(context, "$entityName updated successfully")
                            onSuccess()
                        }
                        .addOnFailureListener {
                            showToast(context, "Failed to update $entityName")
                        }
                } else {
                    onError()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(context, "Failed to update $entityName")
            }
        })
    }

    // Generic delete function
    fun <T> delete(entityClass: Class<T>, entityId: String, context: Context) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.child(entityId).removeValue()
            .addOnSuccessListener {
                showToast(context, "$entityName deleted successfully")
            }
            .addOnFailureListener {
                showToast(context, "Failed to delete $entityName")
            }
    }

    // Helper method to display Toast messages
    private fun showToast(context: Context, message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
