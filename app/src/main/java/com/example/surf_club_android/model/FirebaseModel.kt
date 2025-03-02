package com.example.surf_club_android.model


import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.storage.storage
import com.example.surf_club_android.base.Constants
import com.example.surf_club_android.base.EmptyCallback
import com.example.surf_club_android.base.PostsCallback
import com.example.surf_club_android.base.SuccessCallback
import com.example.surf_club_android.base.UsersCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Query
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val database = Firebase.firestore
    private val storage = Firebase.storage
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private var isFirestoreConfigured = false
    }

    init {
        if (!isFirestoreConfigured) {
            val setting = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { })
            }
            database.firestoreSettings = setting
            isFirestoreConfigured = true
        }
    }


    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun searchPosts(query: String, callback: PostsCallback) {
        // Prepare the query range for prefix search.
        val endQuery = query + "\uf8ff"
        // Use a mutable map to avoid duplicate posts (keyed by post id).
        val postsMap = mutableMapOf<String, Post>()
        // List the fields to search.
        val fields = listOf("name", "description", "instructions", "ingredients")
        // Use an atomic counter to know when all queries are complete.
        val counter = java.util.concurrent.atomic.AtomicInteger(fields.size)

        for (field in fields) {
            database.collection(Constants.COLLECTIONS.POSTS)
                .whereGreaterThanOrEqualTo(field, query)
                .whereLessThanOrEqualTo(field, endQuery)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val post = Post.fromJSON(document.data)
                            postsMap[post.id] = post
                        }
                    } else {
                        Log.e("FirebaseModel", "Error searching field '$field'", task.exception)
                    }
                    // When all field queries have completed, return the aggregated results.
                    if (counter.decrementAndGet() == 0) {
                        callback(postsMap.values.toList())
                    }
                }
        }
    }


    fun getLastFourPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(4)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts: MutableList<Post> = mutableListOf()
                    for (document in task.result) {
                        posts.add(Post.fromJSON(document.data))
                    }
                    callback(posts)
                } else {
                    Log.e("FirebaseModel", "Error fetching last four posts", task.exception)
                    callback(listOf())
                }
            }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo("author", id)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun addPost(post: Post, callback: SuccessCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id)
            .set(post.json)
            .addOnCompleteListener {
                callback(it.isSuccessful)
            }
    }

    fun deletePost(id: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(id).delete()
            .addOnCompleteListener {
                callback()
            }
    }

    fun signUp(
        email: String,
        password: String,
        callback: (FirebaseUser?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    callback(user, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }



    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(auth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    // --------------------
    // User Data Functions
    // --------------------
    fun saveUser(    user: FirebaseUser,
                     firstName: String,
                     lastName: String,
                     email: String,
                     role: String,
                     image: String?,
                     callback: (Boolean, String?) -> Unit) {
        // Define the user data you want to store.
        val userData = hashMapOf(
            "id" to user.uid,
            "image" to image,
            "role" to role,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )

        // Save the data in the "users" collection with the document id as the user's uid.
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.uid)
            .set(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(json.toObject(User::class.java))
                        }
                        callback(users)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        database.collection("users").document(user.id).set(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateUserProfileImage(userId: String, imageUrl: String, callback: (Boolean) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS).document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}