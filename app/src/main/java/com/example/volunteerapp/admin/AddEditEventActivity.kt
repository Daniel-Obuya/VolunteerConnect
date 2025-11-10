package com.example.volunteerapp.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.volunteerapp.databinding.ActivityAddEditEventBinding
import com.example.volunteerapp.model.Event
import com.example.volunteerapp.utils.FirebaseUtils

class AddEditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEventBinding
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra("eventId")
        if (eventId != null) loadEventDetails(eventId!!)

        binding.btnSave.setOnClickListener {
            saveEvent()
        }

        binding.btnDelete.setOnClickListener {
            deleteEvent()
        }
    }

    private fun loadEventDetails(id: String) {
        FirebaseUtils.eventsCollection.document(id).get()
            .addOnSuccessListener {
                val event = it.toObject(Event::class.java)
                if (event != null) {
                    binding.edtTitle.setText(event.title)
                    binding.edtDescription.setText(event.description)
                    binding.edtDate.setText(event.date)
                    binding.edtLocation.setText(event.location)
                }
            }
    }

    private fun saveEvent() {
        val event = Event(
            title = binding.edtTitle.text.toString(),
            description = binding.edtDescription.text.toString(),
            date = binding.edtDate.text.toString(),
            location = binding.edtLocation.text.toString()

        )

        if (eventId == null) {
            FirebaseUtils.eventsCollection.add(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()
                    finish()
                }
        } else {
            FirebaseUtils.eventsCollection.document(eventId!!).set(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun deleteEvent() {
        eventId?.let {
            FirebaseUtils.eventsCollection.document(it).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}
