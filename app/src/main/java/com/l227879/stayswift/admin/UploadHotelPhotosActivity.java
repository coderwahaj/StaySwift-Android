package com.l227879.stayswift.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;

import java.util.ArrayList;

public class UploadHotelPhotosActivity extends AppCompatActivity {

    private static final int MAX_PHOTOS = 10;
    private RecyclerView rvPhotos;
    private TextView tvPhotoCount;
    private Button btnBackPhotos, btnAddPhotos, btnNextPhotos;

    // The logic lists
    private final ArrayList<Uri> selectedPhotoUris = new ArrayList<>();
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();
    private final ArrayList<EditablePhotoItem> photoItems = new ArrayList<>();
    private EditablePhotosAdapter editableAdapter;

    // Previous data
    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress, hotelOtherAmenities;
    private double hotelLat, hotelLng;
    private ArrayList<String> hotelAmenities;
    private boolean isEditMode;
    private String hotelId;

    // The Launcher
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_hotel_photos);

        bindViews();
        readIncomingExtras();
        setupRecycler();
        setupPickerLauncher(); // Fixed selection logic here
        setupClicks();
        updateCountText();
    }

    private void bindViews() {
        rvPhotos = findViewById(R.id.rvPhotos);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        btnBackPhotos = findViewById(R.id.btnBackPhotos);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        btnNextPhotos = findViewById(R.id.btnNextPhotos);
    }

    private void readIncomingExtras() {
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
        isEditMode = i.getBooleanExtra("isEditMode", false);
        hotelId = i.getStringExtra("hotelId");
        existingPhotoUrls = i.getStringArrayListExtra("hotelPhotoUrls");

        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();
        if (hotelAmenities == null) hotelAmenities = new ArrayList<>();
    }

    private void setupRecycler() {
        // Clear and add existing URLs first (Edit Mode)
        photoItems.clear();
        for (String url : existingPhotoUrls) {
            photoItems.add(new EditablePhotoItem(url));
        }

        editableAdapter = new EditablePhotosAdapter(photoItems, position -> {
            EditablePhotoItem item = photoItems.get(position);
            if (item.type == EditablePhotoItem.Type.EXISTING_URL) {
                existingPhotoUrls.remove(item.url);
            } else {
                selectedPhotoUris.remove(item.uri);
            }
            photoItems.remove(position);
            editableAdapter.notifyDataSetChanged(); // Use simple notify for reliability
            updateCountText();
        });

        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        rvPhotos.setAdapter(editableAdapter);
    }

    private void setupPickerLauncher() {
        // FIX: Using the modern contract to avoid the "Preparing Media" freeze
        pickMultipleMedia = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        int spaceLeft = MAX_PHOTOS - photoItems.size();

                        for (int i = 0; i < Math.min(uris.size(), spaceLeft); i++) {
                            Uri uri = uris.get(i);

                            // Grant permission to read this file long-term
                            try {
                                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {}

                            if (!selectedPhotoUris.contains(uri)) {
                                selectedPhotoUris.add(uri);
                                photoItems.add(new EditablePhotoItem(uri));
                            }
                        }

                        editableAdapter.notifyDataSetChanged();
                        updateCountText();
                    }
                }
        );
    }

    private void setupClicks() {
        btnBackPhotos.setOnClickListener(v -> finish());

        btnAddPhotos.setOnClickListener(v -> {
            if (photoItems.size() >= MAX_PHOTOS) {
                Toast.makeText(this, "Limit reached!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trigger the Picker
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnNextPhotos.setOnClickListener(v -> {
            if (photoItems.isEmpty()) {
                Toast.makeText(this, "Add at least 1 photo", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent next = new Intent(this, ReviewHotelActivity.class);
            // ... (All your putExtra calls remain exactly the same)
            next.putExtra("hotelName", hotelName);
            next.putExtra("hotelDescription", hotelDescription);
            next.putExtra("hotelPhone", hotelPhone);
            next.putExtra("hotelEmail", hotelEmail);
            next.putExtra("hotelAddress", hotelAddress);
            next.putExtra("hotelLat", hotelLat);
            next.putExtra("hotelLng", hotelLng);
            next.putStringArrayListExtra("hotelAmenities", hotelAmenities);
            next.putExtra("hotelOtherAmenities", hotelOtherAmenities);
            next.putParcelableArrayListExtra("hotelPhotoUris", selectedPhotoUris);
            next.putExtra("isEditMode", isEditMode);
            next.putExtra("hotelId", hotelId);
            next.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);
            startActivity(next);
        });
    }

    private void updateCountText() {
        tvPhotoCount.setText(photoItems.size() + " / " + MAX_PHOTOS + " selected");
    }
}