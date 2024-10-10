package com.example.amsattendancemanagementsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity6 extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Map<String, String[]> csvData = new HashMap<>();
    private TextView textViewMessage1, textViewMessage2;
    private PreviewView previewView;
    private BarcodeScanner barcodeScanner;
    private String scannedId = null;
    private Handler handler = new Handler();
    private Runnable resetMessagesRunnable;
    private DatabaseReference attendanceReference;
    Button buttonMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main6);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewMessage1 = findViewById(R.id.message1);
        textViewMessage2 = findViewById(R.id.message2);
        buttonMark = findViewById(R.id.mark_attendance);
        previewView = findViewById(R.id.preview_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginTop(0);
        toolbar.setTitleMarginBottom(0);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String classId = intent.getStringExtra("classId"); // Retrieve the class ID from the intent
        csvData = (HashMap<String, String[]>) intent.getSerializableExtra("csvData");

        attendanceReference = FirebaseDatabase.getInstance().getReference("Class_Attendance").child(classId).child("attendance");

        // Request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            setupScanner();  // Start the scanner if permission is granted
        }

        buttonMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scannedId != null) {
                    markAttendance(scannedId);
                }else {
                    Toast.makeText(MainActivity6.this, "No valid ID scanned to mark attendance", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        Intent intent = new Intent(MainActivity6.this, MainActivity3.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void setupScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_CODABAR
                        )
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
        startCamera();
    }

    @SuppressLint({"UnsafeExperimentalUsageError", "NewApi"})
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Check available camera lenses
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    Log.d("MainActivity6", "Front camera is available.");
                } else {
                    Log.e("MainActivity6", "Front camera is not available.");
                    return;  // Exit if the front camera is not available
                }

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(getMainExecutor(), this::processImageProxy);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity6", "Camera initialization failed", e);
            } catch (IllegalArgumentException e) {
                Log.e("MainActivity6", "Front camera not found or not accessible", e);
            } catch (CameraInfoUnavailableException e) {
                throw new RuntimeException(e);
            }
        }, getMainExecutor());
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        InputImage image = InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String scannedValue = barcode.getRawValue();
                        handleScanResult(scannedValue);
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity6", "Barcode scan failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleScanResult(String scannedValue) {
        scannedId = scannedValue;

        if (csvData != null && csvData.containsKey(scannedValue)) {
            String[] messages = csvData.get(scannedValue);
            textViewMessage1.setText(messages[0]);
            textViewMessage2.setText(messages[1]);

            resetMessagesRunnable = () -> {
                scannedId = null;  // Reset scannedId after 5 seconds
                textViewMessage1.setText("");
                textViewMessage2.setText("");
            };

            handler.postDelayed(resetMessagesRunnable, 5000);

        } else {
            textViewMessage1.setText("No message found for this ID.");
            textViewMessage2.setText("");

            resetMessagesRunnable = () -> {
                scannedId = null;
                textViewMessage1.setText("");
                textViewMessage2.setText("");
            };

            handler.postDelayed(resetMessagesRunnable, 5000);
        }
    }

    private void markAttendance(String id) {
        if (id != null) {
            // Save the scanned ID to Firebase under the "attendance" node
            attendanceReference.child(id).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MainActivity6", "Attendance marked successfully");
                        Toast.makeText(MainActivity6.this, "Your attendance marked successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity6", "Failed to mark attendance", e);
                        Toast.makeText(MainActivity6.this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    });
        }else{
            Toast.makeText(MainActivity6.this, "No valid ID to mark attendance", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner();  // Start the scanner if permission is granted
            } else {
                Log.e("MainActivity6", "Camera permission was not granted.");
            }
        }
    }
}