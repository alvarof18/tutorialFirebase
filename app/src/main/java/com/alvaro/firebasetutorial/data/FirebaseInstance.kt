package com.alvaro.firebasetutorial.data

import android.content.Context
import android.util.Log
import com.alvaro.firebasetutorial.domain.Todo
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.random.Random


class FirebaseInstance(context: Context) {

    private val database = Firebase.database
    private val dbRef = database.reference

    init {
        FirebaseApp.initializeApp(context)
    }

    fun writeOnFirebase(title: String, description: String) {
        val newItem = dbRef.push()
        newItem.setValue(getGenericTodoTasksItem(title,description))

    }

    fun getData(): Flow<List<Pair<String, Todo>>> {
        return callbackFlow {
            val listener = database.reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = getCleanSnapshot(snapshot)
                    if (data != null) {
                        trySend(data)
                    }

                    //       val data: String? = snapshot.getValue<String>()
                    //       if (data != null) {
                    //           trySend(data)
                    //      }


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("Info", "Something wrong")
                    cancel()
                }
            })
            awaitClose { database.reference.removeEventListener(listener) }
        }

    }

    private fun getGenericTodoTasksItem(title: String, description: String) =
        Todo(title = title, description = description)

    fun removeFromDatabase(reference: String) {
        dbRef.child(reference).removeValue()
    }

    fun updateFromDatabase(reference: String) {
        dbRef.child(reference).child("done").setValue(true)
    }

    private fun getCleanSnapshot(snapshot: DataSnapshot): List<Pair<String, Todo>> {
        return snapshot.children.map { item ->
            Pair(item.key!!, item.getValue(Todo::class.java)!!)
        }
    }

}

