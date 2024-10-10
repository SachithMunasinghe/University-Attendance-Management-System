package com.example.amsattendancemanagementsystem;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity4 extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private static final int PICK_CSV_FILE_REQUEST_CODE = 1;
    private Map<String, String[]> csvData = new HashMap<>();
    TextInputEditText editTextCName, editTextCDate, editTextFrom, editTextTo, editTextLecturer;
    TextView textViewId;
    Button buttonCSV, buttonCopy, buttonCreate, buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Class_Attendance");

        editTextCName = findViewById(R.id.class_name);
        editTextCDate = findViewById(R.id.class_date);
        editTextFrom = findViewById(R.id.class_from);
        editTextTo = findViewById(R.id.class_to);
        editTextLecturer = findViewById(R.id.class_lecturer);
        textViewId = findViewById(R.id.class_id);
        buttonCSV = findViewById(R.id.get_csv);
        buttonCopy = findViewById(R.id.copy_id);
        buttonCreate = findViewById(R.id.create_class);
        buttonNext = findViewById(R.id.next);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginTop(0);
        toolbar.setTitleMarginBottom(0);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveClassToFirebase();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
                intent.putExtra("csvData", (HashMap<String, String[]>) csvData);
                intent.putExtra("classId", String.valueOf(textViewId.getText()));
                startActivity(intent);
                finish();
            }
        });
        buttonCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String classId = String.valueOf(textViewId.getText());

                if (!classId.isEmpty()) {
                    // Get the ClipboardManager
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Class ID", classId);
                    clipboard.setPrimaryClip(clip);

                    // Notify the user
                    Toast.makeText(MainActivity4.this, "Class ID copied to clipboard!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity4.this, "No Class ID to copy", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            getOnBackPressedDispatcher().onBackPressed();
            Intent intent = new Intent(MainActivity4.this, MainActivity3.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity4.this, MainActivity3.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

    private void saveClassToFirebase(){
        String className, classDate, fromDate, toDate, lecturer;
        className = String.valueOf(editTextCName.getText());
        classDate = String.valueOf(editTextCDate.getText());
        fromDate = String.valueOf(editTextFrom.getText());
        toDate = String.valueOf(editTextTo.getText());
        lecturer = String.valueOf(editTextLecturer.getText());

        if (className.isEmpty()) {
            editTextCName.setError("Class name is required");
            editTextCName.requestFocus();
            return;
        }
        if (classDate.isEmpty()) {
            editTextCDate.setError("Class date is required");
            editTextCDate.requestFocus();
            return;
        }
        if (fromDate.isEmpty()) {
            editTextFrom.setError("Class starting time is required");
            editTextFrom.requestFocus();
            return;
        }
        if (toDate.isEmpty()) {
            editTextTo.setError("Class ending time is required");
            editTextTo.requestFocus();
            return;
        }
        if (lecturer.isEmpty()) {
            editTextLecturer.setError("Lecturer name is required");
            editTextLecturer.requestFocus();
            return;
        }
        String uniqueKey = generateUniqueKey();
        textViewId.setText(uniqueKey);

        ClassModel classModel = new ClassModel(className, classDate, fromDate, toDate, lecturer);

        databaseReference.child(uniqueKey).setValue(classModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Create an empty "attendance" node under the class node
                Map<String, Object> emptyAttendance = new HashMap<>();
                databaseReference.child(uniqueKey).child("attendance").child("placeholder").setValue("null").addOnCompleteListener(attendanceTask -> {
                    if (attendanceTask.isSuccessful()) {
                        Toast.makeText(MainActivity4.this, "Class created successfully!.Copy your class ID: ", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = attendanceTask.getException() != null ? attendanceTask.getException().getMessage() : "Unknown error";
                        Toast.makeText(MainActivity4.this, "Failed to initialize attendance: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(MainActivity4.this, "Failed to create class: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateUniqueKey() {
        // Create a key from A-Z and 1-10
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678910";
        StringBuilder uniqueKey = new StringBuilder();
        Random random = new Random();

        // Generate a 6-character key
        for (int i = 0; i < 14; i++) {
            uniqueKey.append(characters.charAt(random.nextInt(characters.length())));
        }
        return uniqueKey.toString();
    }

    public static class ClassModel {
        public String className;
        public String classDate;
        public String fromTime;
        public String toTime;
        public String lecturer;

        public ClassModel() {
            // Default constructor required for calls to DataSnapshot.getValue(ClassModel.class)
        }

        public ClassModel(String className, String classDate, String fromTime, String toTime, String lecturer) {
            this.className = className;
            this.classDate = classDate;
            this.fromTime = fromTime;
            this.toTime = toTime;
            this.lecturer = lecturer;
        }
    }
}