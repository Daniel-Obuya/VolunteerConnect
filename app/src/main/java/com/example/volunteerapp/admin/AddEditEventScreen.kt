package com.example.volunteerapp.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

data class Opportunity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    eventId: String? = null,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Load existing event
    LaunchedEffect(eventId) {
        if (!eventId.isNullOrEmpty()) {
            db.collection("opportunities").document(eventId).get()
                .addOnSuccessListener { doc ->
                    val opp = doc.toObject(Opportunity::class.java)
                    if (opp != null) {
                        title = opp.title
                        description = opp.description
                        date = opp.date
                        time = opp.time
                        location = opp.location
                    }
                }
        }
    }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d -> date = "$d/${m + 1}/$y" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val timePicker = TimePickerDialog(
        context,
        { _, h, min -> time = String.format(Locale.getDefault(), "%02d:%02d", h, min) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (eventId == null) "Add Event" else "Edit Event") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { datePicker.show() }) { Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date") } }
            )
            OutlinedTextField(
                value = time,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { timePicker.show() }) { Icon(Icons.Default.AccessTime, contentDescription = "Pick Time") } }
            )
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val opportunity = Opportunity(title = title, description = description, date = date, time = time, location = location)
                    if (eventId == null) {
                        db.collection("opportunities").add(opportunity).addOnSuccessListener {
                            Toast.makeText(context, "Event Saved", Toast.LENGTH_SHORT).show()
                            onSaved()
                        }
                    } else {
                        db.collection("opportunities").document(eventId).set(opportunity).addOnSuccessListener {
                            Toast.makeText(context, "Event Updated", Toast.LENGTH_SHORT).show()
                            onSaved()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Event") }

            if (eventId != null) {
                Button(
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                    onClick = {
                        db.collection("opportunities").document(eventId).delete().addOnSuccessListener {
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            onDeleted()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete Event") }
            }
        }
    }
}
