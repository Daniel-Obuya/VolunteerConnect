package com.example.volunteerapp.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volunteerapp.databinding.ActivityAdminDashboardBinding
import com.example.volunteerapp.model.Event
import com.example.volunteerapp.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var eventAdapter: EventAdapter
    private var eventList = mutableListOf<Event>()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadEvents()

        binding.btnAddEvent.setOnClickListener {
            startActivity(Intent(this, AddEditEventActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(eventList) { event ->
            val intent = Intent(this, AddEditEventActivity::class.java)
            intent.putExtra("eventId", event.id)
            startActivity(intent)
        }
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewEvents.adapter = eventAdapter
    }

    private fun loadEvents() {
        listener = FirebaseUtils.eventsCollection.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                eventList.clear()
                for (doc in snapshot.documents) {
                    val event = doc.toObject(Event::class.java)
                    if (event != null) {
                        event.id = doc.id
                        eventList.add(event)
                    }
                }
                eventAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
