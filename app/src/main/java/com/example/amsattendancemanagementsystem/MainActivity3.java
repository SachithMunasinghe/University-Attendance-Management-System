package com.example.amsattendancemanagementsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity3 extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    TextView textViewMail, textViewName;
    TextInputEditText textViewId;
    Button buttonCreate, buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        textViewMail = findViewById(R.id.Email);
        textViewName = findViewById(R.id.Name);
        textViewId = findViewById(R.id.class_id);
        buttonCreate = findViewById(R.id.btnCreate);
        buttonJoin = findViewById(R.id.btnJoin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginTop(0);
        toolbar.setTitleMarginBottom(0);


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            textViewMail.setText(user.getEmail());

            String displayName = user.getDisplayName(); // Get the display name from Firebase
            if (displayName != null && !displayName.isEmpty()) {
                textViewName.setText(displayName);
            } else {
                textViewName.setText("Welcome User");
            }
        }
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity4.class);
                startActivity(intent);
                finish();
            }
        });
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String class_ID;
                class_ID = String.valueOf(textViewId.getText());
                if (class_ID.isEmpty()) {
                    textViewId.setError("Class ID is required");
                    textViewId.requestFocus();
                    return;
                }
                checkClassIdInDatabase(class_ID);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.item1){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkClassIdInDatabase(String classId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Class_Attendance");

        databaseReference.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Class ID exists, retrieve class data
                    Intent intent = new Intent(MainActivity3.this, MainActivity5.class);
                    intent.putExtra("classId", classId);
                    intent.putExtra("className", snapshot.child("className").getValue(String.class));
                    intent.putExtra("classDate", snapshot.child("classDate").getValue(String.class));
                    intent.putExtra("classFrom", snapshot.child("fromTime").getValue(String.class));
                    intent.putExtra("classTo", snapshot.child("toTime").getValue(String.class));
                    intent.putExtra("classLecturer", snapshot.child("lecturer").getValue(String.class));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity3.this, "Class ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", error.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity3.this);
        alertDialog.setTitle("Exit");
        alertDialog.setMessage("Do you want to exit?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}