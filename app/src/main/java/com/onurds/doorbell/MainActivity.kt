package com.onurds.doorbell

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    companion object {
        // Constant for notification permission request code
        private const val NOTIFICATION_PERMISSION_CODE = 123
    }
    private lateinit var adapter: NotificationLogAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentDateFilter: LocalDateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            setupRecyclerView()
            setupButtons()
            requestNotificationPermission()
            setupFirebaseMessaging()
            observeLogs()

        } catch (e: Exception) {

            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        // Setup search button with date picker
        findViewById<MaterialButton>(R.id.searchButton).setOnClickListener {
            showDatePicker()
        }

        // Setup clear button
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener {
            showClearConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationLogAdapter()
        findViewById<RecyclerView>(R.id.logsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun observeLogs() {
        val database = NotificationDatabase.getDatabase(applicationContext)
        scope.launch {
            // Choose which query to use based on filter
            val logsFlow = if (currentDateFilter != null) {
                database.notificationLogDao().getLogsByDate(currentDateFilter!!)
            } else {
                database.notificationLogDao().getAllLogs()
            }

            // Collect and update the UI
            logsFlow.collect { logs ->
                adapter.submitList(logs)
                if (logs.isEmpty() && currentDateFilter != null) {
                    Toast.makeText(
                        this@MainActivity,
                        "No logs found for selected date",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar)
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // Convert milliseconds to LocalDateTime
            val selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)

            currentDateFilter = selectedDate
            updateDateFilterDisplay(selectedDate)
            observeLogs()
        }

        datePicker.addOnNegativeButtonClickListener {
            // Clear the filter
            currentDateFilter = null
            updateDateFilterDisplay(null)
            observeLogs()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateDateFilterDisplay(date: LocalDateTime?) {
        val dateFilterText = findViewById<TextView>(R.id.dateFilterText)
        if (date != null) {
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            val formattedDate = date.format(formatter)
            dateFilterText.text = getString(R.string.showing_logs_for_date, formattedDate)
            dateFilterText.visibility = View.VISIBLE
        } else {
            dateFilterText.visibility = View.GONE
        }
    }

    private fun showClearConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear All Logs")
            .setMessage("Are you sure you want to delete all notification logs? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllLogs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllLogs() {
        scope.launch {
            withContext(Dispatchers.IO) {
                val database = NotificationDatabase.getDatabase(applicationContext)
                database.notificationLogDao().deleteAllLogs()
            }
            // Show confirmation
            Toast.makeText(this@MainActivity, "All logs cleared", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                // Log the token for debugging purposes
                Log.d("Firebase", "FCM Token: ${tokenTask.result}")

                // Subscribe to doorbell alerts topic
                FirebaseMessaging.getInstance().subscribeToTopic("doorbell_alerts")
                    .addOnCompleteListener { subscribeTask ->
                        if (subscribeTask.isSuccessful) {
                            Log.d("Firebase", "Successfully subscribed to doorbell alerts")
                            Toast.makeText(this, "Ready to receive doorbell notifications", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Firebase", "Failed to subscribe: ${subscribeTask.exception?.message}")
                            Toast.makeText(this, "Failed to set up notifications", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("Firebase", "Failed to get FCM token: ${tokenTask.exception?.message}")
            }
        }
    }

    private fun requestNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when {
                // Check if permission exists
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Permissions", "Notification permission already granted")
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        "Notifications are needed to alert you when the doorbell rings",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // Request the permission
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                }
            }
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Notification permission granted")
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("Permissions", "Notification permission denied")
                    Toast.makeText(
                        this,
                        "Notifications are needed for doorbell alerts",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}
