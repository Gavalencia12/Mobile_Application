package com.example.carhive

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CRUD(private val firebaseDatabase: FirebaseDatabase, private var currentToast: Toast?) {

    // Generates a random ID for the given collection
    fun generateId(collectionName: String): String? {
        return firebaseDatabase.getReference(collectionName).push().key
    }

    // Generic create function for any entity
    fun <T> create(entity: T, entityId: String, entityClass: Class<T>, context: Context) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.child(entityId).setValue(entity)
            .addOnSuccessListener {
                showToast(context, context.getString(R.string.entity_created_success, entityName))
            }
            .addOnFailureListener {
                showToast(context, context.getString(R.string.entity_create_failed, entityName))
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
                showToast(context, context.getString(R.string.entity_load_failed, entityName))
            }
        })
    }

    // Generic update function to update an entity if it exists
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
                            showToast(context, context.getString(R.string.entity_updated_success, entityName))
                            onSuccess()
                        }
                        .addOnFailureListener {
                            showToast(context, context.getString(R.string.entity_update_failed, entityName))
                        }
                } else {
                    onError()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(context, context.getString(R.string.entity_update_failed, entityName))
            }
        })
    }

    // Generic delete function
    fun <T> delete(entityClass: Class<T>, entityId: String, context: Context) {
        val entityName = entityClass.simpleName
        val reference = firebaseDatabase.getReference(entityName)

        reference.child(entityId).removeValue()
            .addOnSuccessListener {
                showToast(context, context.getString(R.string.entity_deleted_success, entityName))
            }
            .addOnFailureListener {
                showToast(context, context.getString(R.string.entity_delete_failed, entityName))
            }
    }

    // Displays Toast messages with a single active instance
    private fun showToast(context: Context, message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
