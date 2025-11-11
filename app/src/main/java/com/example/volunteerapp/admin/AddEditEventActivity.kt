package com.example.volunteerapp.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.volunteerapp.databinding.ActivityAddEditEventBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

// Data class for volunteer opportunities
data class Opportunity(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = ""
)

class AddEditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEventBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- DATE PICKER ---
        binding.edtDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                binding.edtDate.setText(formattedDate)
            }, year, month, day)

            dpd.show()
        }

        // --- TIME PICKER ---
        binding.edtTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            val tpd = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                binding.edtTime.setText(formattedTime)
            }, hour, minute, true) // true = 24-hour format

            tpd.show()
        }

        // --- SAVE BUTTON ---
        binding.btnSave.setOnClickListener {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()
            val date = binding.edtDate.text.toString().trim()
            val time = binding.edtTime.text.toString().trim()
            val location = binding.edtLocation.text.toString().trim()

            if (title.isBlank() || description.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val opportunity = Opportunity(title, description, date, time, location)
            saveOpportunityToFirestore(opportunity)
        }

        // Optional: handle delete if implemented
        // binding.btnDelete.setOnClickListener { ... }
    }

    private fun saveOpportunityToFirestore(opportunity: Opportunity) {
        db.collection("opportunities")
            .add(opportunity)
            .addOnSuccessListener {
                Toast.makeText(this, "Opportunity posted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error posting opportunity: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
