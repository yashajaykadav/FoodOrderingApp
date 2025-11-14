package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.UserAction
import com.foodordering.krishnafoods.admin.adapter.UserManagementAdapter
import com.foodordering.krishnafoods.admin.model.User
import com.foodordering.krishnafoods.user.activity.LoginActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var rvUserManagement: RecyclerView
    private lateinit var btnLogout: MaterialButton
    private lateinit var adapter: UserManagementAdapter
    private lateinit var searchBar: TextInputEditText // Made this a class property

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val fullUserlist = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        initViews()
        // searchBar is now initialized in initViews()

        searchBar.addTextChangedListener { text ->
            filterUsers(text.toString())
        }
        setupRecyclerView()
        loadUsers()
    }

    private fun initViews() {
        rvUserManagement = findViewById(R.id.rvUserManagement)
        btnLogout = findViewById(R.id.btnLogout)
        searchBar = findViewById(R.id.etSearch) // Initialize the class property

        supportActionBar?.hide()
        window.statusBarColor = getColor(R.color.colorAccent)

        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }

        btnLogout.setOnClickListener { logoutAdmin() }
    }

    private fun setupRecyclerView() {
        adapter = UserManagementAdapter { user, action ->
            when (action) {
                UserAction.DELETE -> confirmDeleteUser(user)
                UserAction.MAKE_ADMIN -> updateUserRole(user, "admin")
                UserAction.MAKE_USER -> updateUserRole(user, "user")
            }
        }
        rvUserManagement.layoutManager = LinearLayoutManager(this)
        rvUserManagement.adapter = adapter
    }

    private fun filterUsers(query: String) {
        val searchText = query.lowercase().trim()
        val filteredList = if (searchText.isEmpty()) {
            fullUserlist
        } else {
            fullUserlist.filter { user ->
                user.name.lowercase().contains(searchText) ||
                        user.email.lowercase().contains(searchText) ||
                        user.role.lowercase().contains(searchText)
            }
        }
        adapter.submitList(filteredList.toList()) // immutable copy
    }

    private fun loadUsers() {
        db.collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                fullUserlist.clear()

                snapshot?.documents?.forEach { document ->
                    val user = document.toObject(User::class.java)?.apply {
                        id = document.id
                        if (photoUrl.isNullOrBlank() && document.contains("photoUrl")) {
                            photoUrl = document.getString("photoUrl")
                        }
                    }
                    user?.let { fullUserlist.add(it) }
                }

                // Re-apply the filter instead of just submitting the list
                // This fixes the UI bug where the list "jumps" after a delete
                filterUsers(searchBar.text.toString())
            }
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.name}? This will remove all related data permanently.")
            .setPositiveButton("Delete") { _, _ -> deleteUserAndData(user) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * --- THIS IS THE CRASH FIX ---
     * This function now fetches all documents FIRST, and only then
     * runs the batch delete. This prevents the crash.
     */
    private fun deleteUserAndData(user: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == user.id) {
            Toast.makeText(this, "You cannot delete your own account.", Toast.LENGTH_SHORT).show()
            return // Prevent self-delete
        }

        val userRef = db.collection("users").document(user.id)

        // 1. Create all the query tasks first
        val ordersTask = db.collection("orders").whereEqualTo("userId", user.id).get()
        val feedbackTask = db.collection("feedback").whereEqualTo("userId", user.id).get()
        val enquiryTask = db.collection("enquiry").whereEqualTo("userId", user.id).get()
        // Note: The "users" collection query you had was removed as it was redundant.

        // 2. Wait for all tasks to successfully complete
        Tasks.whenAllSuccess<QuerySnapshot>(ordersTask, feedbackTask, enquiryTask)
            .addOnSuccessListener { results ->
                // results is a List<QuerySnapshot> containing all snapshots
                val ordersSnapshot = results[0]
                val feedbackSnapshot = results[1]
                val enquirySnapshot = results[2]

                // 3. Now, run the batch synchronously
                db.runBatch { batch ->
                    // Add the main user to the batch
                    batch.delete(userRef)

                    // Add all related documents to the batch
                    for (doc in ordersSnapshot) {
                        batch.delete(doc.reference)
                    }
                    for (doc in feedbackSnapshot) {
                        batch.delete(doc.reference)
                    }
                    for (doc in enquirySnapshot) {
                        batch.delete(doc.reference)
                    }
                }.addOnSuccessListener {
                    // Success! No need to call loadUsers().
                    // The snapshot listener will automatically update the UI.
                    Toast.makeText(this, "${user.name} deleted.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    // Handle batch failure
                    Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure in fetching data
                Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserRole(user: User, newRole: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == user.id) {
            Toast.makeText(this, "You cannot change your own role.", Toast.LENGTH_SHORT).show()
            return // Prevent self-role update
        }

        db.collection("users").document(user.id)
            .update("role", newRole)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutAdmin() {
        auth.signOut()
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finishAffinity()
    }
}