package com.example.amsattendancemanagementsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity5 extends AppCompatActivity {
    private static final int PICK_CSV_FILE_REQUEST_CODE = 1;
    private Map<String, String[]> csvData = new HashMap<>();
    TextInputEditText editTextCName, editTextCDate, editTextFrom, editTextTo, editTextLecturer;
    Button buttonCSV, buttonAttendance, buttonNext, buttonSave;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextCName = findViewById(R.id.class_name);
        editTextCDate = findViewById(R.id.class_date);
        editTextFrom = findViewById(R.id.class_from);
        editTextTo = findViewById(R.id.class_to);
        editTextLecturer = findViewById(R.id.class_lecturer);
        buttonCSV = findViewById(R.id.get_csv);
        buttonAttendance = findViewById(R.id.attendance);
        buttonNext = findViewById(R.id.next);
        buttonSave = findViewById(R.id.saveChange);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginTop(0);
        toolbar.setTitleMarginBottom(0);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        classId = intent.getStringExtra("classId");
        editTextCName.setText(intent.getStringExtra("className"));
        editTextCDate.setText(intent.getStringExtra("classDate"));
        editTextFrom.setText(intent.getStringExtra("classFrom"));
        editTextTo.setText(intent.getStringExtra("classTo"));
        editTextLecturer.setText(intent.getStringExtra("classLecturer"));

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
                intent.putExtra("classId", classId);
                intent.putExtra("csvData", (HashMap<String, String[]>) csvData);
                startActivity(intent);
                finish();
            }
        });

        buttonAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attendanceintent = new Intent(getApplicationContext(), MainActivity7.class);
                attendanceintent.putExtra("classId", classId);
                startActivity(attendanceintent);
            }
        });

        buttonCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveClassChanges();
            }
        });
    }

    private void saveClassChanges() {
        String name, date, fromTime, toTime, lecturer;
        name = String.valueOf(editTextCName.getText());
        date = String.valueOf(editTextCDate.getText());
        fromTime = String.valueOf(editTextFrom.getText());
        toTime = String.valueOf(editTextTo.getText());
        lecturer = String.valueOf(editTextLecturer.getText());

        if (name.isEmpty()) {
            editTextCName.setError("Class name is required");
            editTextCName.requestFocus();
            return;
        }
        if (date.isEmpty()) {
            editTextCDate.setError("Class date is required");
            editTextCDate.requestFocus();
            return;
        }
        if (fromTime.isEmpty()) {
            editTextFrom.setError("Class starting time is required");
            editTextFrom.requestFocus();
            return;
        }
        if (toTime.isEmpty()) {
            editTextTo.setError("Class ending time is required");
            editTextTo.requestFocus();
            return;
        }
        if (lecturer.isEmpty()) {
            editTextLecturer.setError("Lecturer name is required");
            editTextLecturer.requestFocus();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Class_Attendance").child(classId);

        databaseReference.child("className").setValue(name);
        databaseReference.child("classDate").setValue(date);
        databaseReference.child("fromTime").setValue(fromTime);
        databaseReference.child("toTime").setValue(toTime);
        databaseReference.child("lecturer").setValue(lecturer);

        Toast.makeText(this, "Class details updated successfully", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void openFilePicker(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                csvData = parseCsvFile(selectedFileUri);
                if (!csvData.isEmpty()) {
                    Toast.makeText(this, "CSV file loaded successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "The CSV file is empty or invalid.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Map<String, String[]> parseCsvFile(Uri fileUri) {
        Map<String, String[]> csvData = new HashMap<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] columns = line.split(",");
                if (columns.length >= 3) {
                    csvData.put(columns[0].trim(), new String[]{columns[1].trim(), columns[2].trim()});
                }
            }

            reader.close();
            inputStream.close();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load the CSV file", Toast.LENGTH_SHORT).show();
        }
        return csvData;
    }

    private void handleCsvFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder csvContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                csvContent.append(line).append("\n");
            }

            reader.close();
            inputStream.close();

            // Log the content or do something with the CSV data
            Log.d("CSV Content", csvContent.toString());
            Toast.makeText(this, "CSV file loaded successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("CSV Error", "Error reading CSV file", e);
            Toast.makeText(this, "Failed to load the CSV file", Toast.LENGTH_SHORT).show();
        }
    }
}