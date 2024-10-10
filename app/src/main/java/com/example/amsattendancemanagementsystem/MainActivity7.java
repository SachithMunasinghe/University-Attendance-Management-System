package com.example.amsattendancemanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity7 extends AppCompatActivity {
    private String classId;
    private ListView attendanceListView;
    private ArrayAdapter<String> attendanceAdapter;
    private ArrayList<String> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main7);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginTop(0);
        toolbar.setTitleMarginBottom(0);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceList = new ArrayList<>();
        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(attendanceAdapter);

        Intent intent = getIntent();
        classId = intent.getStringExtra("classId");

        fetchAttendanceData();
    }

    private void fetchAttendanceData() {
        if (classId != null) {
            DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("Class_Attendance")
                    .child(classId)
                    .child("attendance");

            attendanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    attendanceList.clear(); // Clear the previous list
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String studentId = snapshot.getKey();
                        Object value = snapshot.getValue();

                        if (value instanceof Boolean) {
                            if ((Boolean) value) {
                                attendanceList.add(studentId +" - Present");
                            }
                        } else if (value instanceof String) {
                            if ("true".equalsIgnoreCase((String) value)) {
                                attendanceList.add(studentId + " - Present");
                            }
                        }
                    }
                    attendanceAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
                    if (attendanceList.isEmpty()) {
                        Toast.makeText(MainActivity7.this, "No students are marked as present.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MainActivity7", "Failed to load attendance data", databaseError.toException());
                    Toast.makeText(MainActivity7.this, "Failed to load attendance data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Class ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}