package com.l227879.stayswift.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.l227879.stayswift.AdminDashboardActivity;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.UUID;

public class ReviewHotelActivity extends AppCompatActivity {

    private TextView tvPreviewBasic, tvPreviewLocation, tvPreviewAmenities;
    private RecyclerView rvPreviewPhotos;
    private Button btnBackPreview, btnConfirmSave;
    private ProgressBar progressSaveHotel;

    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress, hotelOtherAmenities;
    private double hotelLat, hotelLng;
    private ArrayList<String> hotelAmenities;
    private ArrayList<Uri> hotelPhotoUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_hotel);

        bindViews();
        readData();
        showData();
        setupPhotos();
        setupClicks();
    }

    private void bindViews() {
        tvPreviewBasic = findViewById(R.id.tvPreviewBasic);
        tvPreviewLocation = findViewById(R.id.tvPreviewLocation);
        tvPreviewAmenities = findViewById(R.id.tvPreviewAmenities);
        rvPreviewPhotos = findViewById(R.id.rvPreviewPhotos);
        btnBackPreview = findViewById(R.id.btnBackPreview);
        btnConfirmSave = findViewById(R.id.btnConfirmSave);
        progressSaveHotel = findViewById(R.id.progressSaveHotel);
    }

    private void readData() {
        Intent i = getIntent();
        hotelName = i.getStringExtra("hotelName");
        hotelDescription = i.getStringExtra("hotelDescription");
        hotelPhone = i.getStringExtra("hotelPhone");
        hotelEmail = i.getStringExtra("hotelEmail");
        hotelAddress = i.getStringExtra("hotelAddress");
        hotelLat = i.getDoubleExtra("hotelLat", 0.0);
        hotelLng = i.getDoubleExtra("hotelLng", 0.0);
        hotelAmenities = i.getStringArrayListExtra("hotelAmenities");
        hotelOtherAmenities = i.getStringExtra("hotelOtherAmenities");
        hotelPhotoUris = i.getParcelableArrayListExtra("hotelPhotoUris");

        if (hotelAmenities == null) hotelAmenities = new ArrayList<>();
        if (hotelPhotoUris == null) hotelPhotoUris = new ArrayList<>();
    }

    private void showData() {
        String basic = "Basic Information\n\n"
                + "Name: " + value(hotelName) + "\n"
                + "Description: " + value(hotelDescription) + "\n"
                + "Phone: " + value(hotelPhone) + "\n"
                + "Email: " + value(hotelEmail);

        String location = "Location\n\n"
                + "Address: " + value(hotelAddress) + "\n"
                + "Latitude: " + hotelLat + "\n"
                + "Longitude: " + hotelLng;

        String amenitiesText = "Amenities\n\n";
        amenitiesText += hotelAmenities.isEmpty() ? "None selected" : TextUtils.join(", ", hotelAmenities);
        if (!TextUtils.isEmpty(hotelOtherAmenities)) {
            amenitiesText += "\nOther: " + hotelOtherAmenities;
        }

        tvPreviewBasic.setText(basic);
        tvPreviewLocation.setText(location);
        tvPreviewAmenities.setText(amenitiesText);
    }

    private void setupPhotos() {
        rvPreviewPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        rvPreviewPhotos.setAdapter(new SelectedPhotosAdapter(hotelPhotoUris, position -> {
            // No delete in review
        }));
    }

    private void setupClicks() {
        btnBackPreview.setOnClickListener(v -> finish());

        btnConfirmSave.setOnClickListener(v -> {
            if (hotelPhotoUris.isEmpty()) {
                Toast.makeText(this, "Please add at least one photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveHotelToFirebase();
        });
    }

    private void saveHotelToFirebase() {
        setLoading(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            setLoading(false);
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        String ownerUid = user.getUid();
        String hotelId = FirebaseDatabase.getInstance().getReference("hotels").push().getKey();
        if (hotelId == null) {
            setLoading(false);
            Toast.makeText(this, "Unable to create hotel ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> uploadedUrls = new ArrayList<>();
        uploadPhotosSequentially(0, hotelId, uploadedUrls, ownerUid);
    }

    private void uploadPhotosSequentially(int index, String hotelId, ArrayList<String> uploadedUrls, String ownerUid) {
        if (index >= hotelPhotoUris.size()) {
            Hotel hotel = new Hotel(
                    hotelId,
                    ownerUid,
                    safe(hotelName),
                    safe(hotelDescription),
                    safe(hotelPhone),
                    safe(hotelEmail),
                    safe(hotelAddress),
                    hotelLat,
                    hotelLng,
                    hotelAmenities,
                    safe(hotelOtherAmenities),
                    uploadedUrls,
                    System.currentTimeMillis()
            );

            FirebaseDatabase.getInstance().getReference("hotels")
                    .child(hotelId)
                    .setValue(hotel)
                    .addOnSuccessListener(unused -> {
                        setLoading(false);
                        Toast.makeText(this, "Hotel added successfully!", Toast.LENGTH_LONG).show();
                        Intent dash = new Intent(this, AdminDashboardActivity.class);
                        dash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(dash);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Failed to save hotel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            return;
        }

        Uri uri = hotelPhotoUris.get(index);
        String fileName = System.currentTimeMillis() + "_" + index + ".jpg";

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("hotel_photos")
                .child(hotelId)
                .child(fileName);

        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new Exception("Upload failed");
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    uploadedUrls.add(downloadUri.toString());
                    uploadPhotosSequentially(index + 1, hotelId, uploadedUrls, ownerUid);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void setLoading(boolean loading) {
        progressSaveHotel.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnBackPreview.setEnabled(!loading);
        btnConfirmSave.setEnabled(!loading);
        btnConfirmSave.setText(loading ? "Saving..." : "Confirm & Save");
    }

    private String value(String s) {
        return TextUtils.isEmpty(s) ? "-" : s;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}