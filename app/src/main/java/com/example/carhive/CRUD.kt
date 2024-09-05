package com.example.carhive

import android.widget.Toast
import android.content.Context
import com.google.firebase.database.*

class CRUD(private val firebaseDatabase: FirebaseDatabase) {
    private lateinit var databaseRef: DatabaseReference

    // Método genérico para crear una entidad
    fun <T : Any> create(clazz: Class<T>, entity: T, context: Context) {
        // Se asume que entity tiene un campo "id" accesible. Para asegurar esto,
        // se requiere una interfaz o clase base con la propiedad "id".
        val entityId = entity::class.java.getMethod("getId").invoke(entity) as String
        val databaseRef = firebaseDatabase.getReference(clazz.simpleName)
        databaseRef.child(entityId).setValue(entity).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "${clazz.simpleName} creado con éxito :)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al crear ${clazz.simpleName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método genérico para leer entidades
    // Método genérico para leer entidades con un callback
    fun <T : Any> read(clazz: Class<T>, onDataLoaded: (List<T>) -> Unit, context: Context) {
        databaseRef = firebaseDatabase.getReference(clazz.simpleName)
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entityList = mutableListOf<T>()
                snapshot.children.forEach { snap ->
                    val entity = snap.getValue(clazz)
                    entity?.let { entityList.add(it) }
                }
                onDataLoaded(entityList) // Retorna los datos a través del callback
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al leer datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Método genérico para actualizar una entidad
    fun <T : Any> update(clazz: Class<T>, id: String, updates: Map<String, Any>, context: Context) {
        databaseRef = firebaseDatabase.getReference(clazz.simpleName)
        databaseRef.child(id).updateChildren(updates).addOnCompleteListener {
            Toast.makeText(context, "${clazz.simpleName} actualizado con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    // Método genérico para eliminar una entidad
    fun <T : Any> delete(clazz: Class<T>, id: String, context: Context) {
        databaseRef = firebaseDatabase.getReference(clazz.simpleName)
        databaseRef.child(id).removeValue().addOnCompleteListener {
            Toast.makeText(context, "${clazz.simpleName} eliminado con éxito", Toast.LENGTH_SHORT).show()
        }
    }
}
