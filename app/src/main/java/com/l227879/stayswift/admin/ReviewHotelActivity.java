package com.l227879.stayswift.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.l227879.stayswift.AdminDashboardActivity;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReviewHotelActivity extends AppCompatActivity {

    private TextView tvPreviewBasic, tvPreviewLocation, tvPreviewAmenities;
    private RecyclerView rvPreviewPhotos;
    private MaterialButton btnBackPreview, btnConfirmSave;

    // Themed Buffer View
    private RelativeLayout layoutLoadingOverlay;

    // Data fields
    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress, hotelOtherAmenities;
    private double hotelLat, hotelLng;
    private ArrayList<String> hotelAmenities;
    private ArrayList<Uri> hotelPhotoUris;

    // Edit mode support
    private boolean isEditMode;
    private String hotelId;
    private ArrayList<String> existingPhotoUrls;

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

        // Bind the new themed overlay
        layoutLoadingOverlay = findViewById(R.id.layoutLoadingOverlay);
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

        isEditMode = i.getBooleanExtra("isEditMode", false);
        hotelId = i.getStringExtra("hotelId");
        existingPhotoUrls = i.getStringArrayListExtra("hotelPhotoUrls");

        if (hotelAmenities == null) hotelAmenities = new ArrayList<>();
        if (hotelPhotoUris == null) hotelPhotoUris = new ArrayList<>();
        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();
    }
    private CharSequence boldLabel(String label, String value) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int start = ssb.length();
        ssb.append(label);
        ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(" ").append(value == null ? "-" : value);
        return ssb;
    }
    private void showData() {
        SpannableStringBuilder basic = new SpannableStringBuilder();
        basic.append(boldLabel("Name:", value(hotelName))).append("\n");
        basic.append(boldLabel("Description:", value(hotelDescription))).append("\n");
        basic.append(boldLabel("Phone:", value(hotelPhone))).append("\n");
        basic.append(boldLabel("Email:", value(hotelEmail)));

        tvPreviewBasic.setText(basic);

        SpannableStringBuilder location = new SpannableStringBuilder();
        location.append(boldLabel("Address:", value(hotelAddress))).append("\n");
        location.append(boldLabel("Coordinates:", hotelLat + ", " + hotelLng));

        tvPreviewLocation.setText(location);

        String amenitiesText = hotelAmenities.isEmpty() ? "None selected" : TextUtils.join(", ", hotelAmenities);
        if (!TextUtils.isEmpty(hotelOtherAmenities)) {
            amenitiesText += "\nOther: " + hotelOtherAmenities;
        }

        SpannableStringBuilder amen = new SpannableStringBuilder();
        amen.append(boldLabel("Amenities:", amenitiesText));
        tvPreviewAmenities.setText(amen);
    }
//    private void showData() {
//        tvPreviewBasic.setText("Name: " + value(hotelName) + "\n"
//                + "Description: " + value(hotelDescription) + "\n"
//                + "Phone: " + value(hotelPhone) + "\n"
//                + "Email: " + value(hotelEmail));
//
//        tvPreviewLocation.setText("Address: " + value(hotelAddress) + "\n"
//                + "Coordinates: " + hotelLat + ", " + hotelLng);
//
//        String amenitiesText = hotelAmenities.isEmpty() ? "None selected" : TextUtils.join(", ", hotelAmenities);
//        if (!TextUtils.isEmpty(hotelOtherAmenities)) {
//            amenitiesText += "\nOther: " + hotelOtherAmenities;
//        }
//        tvPreviewAmenities.setText(amenitiesText);
//    }

    private void setupPhotos() {
        rvPreviewPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        ArrayList<EditablePhotoItem> previewItems = new ArrayList<>();

        if (existingPhotoUrls != null) {
            for (String url : existingPhotoUrls) {
                if (!TextUtils.isEmpty(url)) previewItems.add(new EditablePhotoItem(url));
            }
        }
        if (hotelPhotoUris != null) {
            for (Uri uri : hotelPhotoUris) {
                if (uri != null) previewItems.add(new EditablePhotoItem(uri));
            }
        }

        EditablePhotosAdapter previewAdapter = new EditablePhotosAdapter(previewItems, position -> { });
        rvPreviewPhotos.setAdapter(previewAdapter);
    }

    private void setupClicks() {
        btnBackPreview.setOnClickListener(v -> finish());

        btnConfirmSave.setOnClickListener(v -> {
            int totalPhotos = existingPhotoUrls.size() + hotelPhotoUris.size();
            if (totalPhotos == 0) {
                Toast.makeText(this, "Please add at least one photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveHotelToFirebase();
        });
    }

    private void saveHotelToFirebase() {
        setLoading(true); // Shows the themed overlay

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            setLoading(false);
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        String ownerUid = user.getUid();
        String finalHotelId;

        if (isEditMode) {
            if (TextUtils.isEmpty(hotelId)) {
                setLoading(false);
                Toast.makeText(this, "Invalid hotel ID.", Toast.LENGTH_SHORT).show();
                return;
            }
            finalHotelId = hotelId;
        } else {
            finalHotelId = FirebaseDatabase.getInstance().getReference("hotels").push().getKey();
            if (finalHotelId == null) {
                setLoading(false);
                Toast.makeText(this, "Database error.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ArrayList<String> mergedPhotoUrls = new ArrayList<>(existingPhotoUrls);
        uploadNewPhotosSequentially(0, finalHotelId, mergedPhotoUrls, ownerUid);
    }

    private void uploadNewPhotosSequentially(int index, String finalHotelId, ArrayList<String> mergedPhotoUrls, String ownerUid) {
        if (index >= hotelPhotoUris.size()) {
            writeHotelToDb(finalHotelId, ownerUid, mergedPhotoUrls);
            return;
        }

        Uri uri = hotelPhotoUris.get(index);
        String fileName = UUID.randomUUID().toString() + ".jpg";

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("hotel_photos")
                .child(finalHotelId)
                .child(fileName);

        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    mergedPhotoUrls.add(downloadUri.toString());
                    uploadNewPhotosSequentially(index + 1, finalHotelId, mergedPhotoUrls, ownerUid);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void writeHotelToDb(String finalHotelId, String ownerUid, ArrayList<String> mergedPhotoUrls) {
        long now = System.currentTimeMillis();

        if (!isEditMode) {
            Hotel hotel = new Hotel(finalHotelId, ownerUid, safe(hotelName), safe(hotelDescription),
                    safe(hotelPhone), safe(hotelEmail), safe(hotelAddress), hotelLat, hotelLng,
                    hotelAmenities, safe(hotelOtherAmenities), mergedPhotoUrls, now);

            FirebaseDatabase.getInstance().getReference("hotels").child(finalHotelId).setValue(hotel)
                    .addOnSuccessListener(unused -> onSaveSuccess("Hotel added successfully!"))
                    .addOnFailureListener(e -> onSaveFailure(e.getMessage()));
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", safe(hotelName));
            updates.put("description", safe(hotelDescription));
            updates.put("phone", safe(hotelPhone));
            updates.put("email", safe(hotelEmail));
            updates.put("address", safe(hotelAddress));
            updates.put("lat", hotelLat);
            updates.put("lng", hotelLng);
            updates.put("amenities", hotelAmenities);
            updates.put("otherAmenities", safe(hotelOtherAmenities));
            updates.put("photoUrls", mergedPhotoUrls);
            updates.put("updatedAt", now);

            FirebaseDatabase.getInstance().getReference("hotels").child(finalHotelId).updateChildren(updates)
                    .addOnSuccessListener(unused -> onSaveSuccess("Hotel updated successfully!"))
                    .addOnFailureListener(e -> onSaveFailure(e.getMessage()));
        }
    }

    private void onSaveSuccess(String msg) {
        setLoading(false);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent dash = new Intent(this, AdminDashboardActivity.class);
        dash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dash);
        finish();
    }

    private void onSaveFailure(String error) {
        setLoading(false);
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        if (layoutLoadingOverlay != null) {
            layoutLoadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        btnBackPreview.setEnabled(!loading);
        btnConfirmSave.setEnabled(!loading);
    }

    private String value(String s) {
        return TextUtils.isEmpty(s) ? "-" : s;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}