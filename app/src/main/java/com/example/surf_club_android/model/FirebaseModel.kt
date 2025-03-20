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
import com.google.firebase.firestore.SetOptions
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
            .get()
            .addOnSuccessListener { querySnapshot ->
                val posts = querySnapshot.documents.mapNotNull { document ->
                    try {
                        Post.fromJSON(document.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("FirebaseModel", "Error parsing post: ${document.id}", e)
                        null
                    }
                }
                callback(posts)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseModel", "Error getting posts", exception)
                callback(emptyList())
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
        val updates = mutableMapOf<String, Any>().apply {
            if (user.firstName.isNotBlank()) {
                put("firstName", user.firstName)
            }
            if (user.lastName.isNotBlank()) {
                put("lastName", user.lastName)
            }
            if (user.email.isNotBlank()) {
                put("email", user.email)
            }
            if (user.role.isNotBlank()) {
                put("role", user.role)
            }
            user.profileImageUrl?.takeIf { it.isNotBlank() }?.let { put("profileImageUrl", it) }
            user.aboutMe?.takeIf { it.isNotBlank() }?.let { put("aboutMe", it) }
            if (user.sessionIds.isNotEmpty()) {
                put("sessionIds", user.sessionIds)
            }
        }

        if (updates.isEmpty()) {
            callback(false)
            return
        }

        database.collection("users").document(user.id)
            .update(updates)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }



    fun addSessionToUser(userId: String, sessionId: String, callback: (Boolean) -> Unit) {
        val userRef = database.collection(Constants.COLLECTIONS.USERS).document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let {
                    val updatedSessions = it.sessionIds.toMutableList()
                    if (!updatedSessions.contains(sessionId)) {
                        updatedSessions.add(sessionId)
                        userRef.update("sessionIds", updatedSessions)
                            .addOnSuccessListener { callback(true) }
                            .addOnFailureListener { callback(false) }
                    } else {
                        callback(true)
                    }
                }
            } else {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    fun getUserSessions(userId: String, callback: (List<Post>) -> Unit) {
        val userRef = database.collection(Constants.COLLECTIONS.USERS).document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let {
                    if (it.sessionIds.isNotEmpty()) {
                        database.collection(Constants.COLLECTIONS.POSTS)
                            .whereIn("id", it.sessionIds)
                            .get()
                            .addOnSuccessListener { result ->
                                val posts = result.documents.mapNotNull { doc -> Post.fromJSON(doc.data ?: emptyMap()) }
                                callback(posts)
                            }
                            .addOnFailureListener { callback(emptyList()) }
                    } else {
                        callback(emptyList())
                    }
                }
            } else {
                callback(emptyList())
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }


    fun uploadProfileImage(image: Bitmap, userId: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageProfileRef = storageRef.child("profile_images/$userId.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageProfileRef.putBytes(data)
        uploadTask
            .addOnFailureListener { callback(null) }
            .addOnSuccessListener { taskSnapshot ->
                imageProfileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
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

    fun updatePost(post: Post, callback: (Boolean) -> Unit) {
        val postRef = database.collection("posts").document(post.id)
        postRef.set(post)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getPostById(postId: String, callback: (Post?) -> Unit) {
        database.collection("posts").document(postId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val post = task.result.toObject(Post::class.java)
                    callback(post)
                } else {
                    callback(null)
                }
            }
    }



}