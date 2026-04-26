//package com.l227879.stayswift.admin;
//
//import android.content.ClipData;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.l227879.stayswift.R;
//
//import java.util.ArrayList;
//
//public class UploadHotelPhotosActivity extends AppCompatActivity {
//
//    private static final int MAX_PHOTOS = 10;
//
//    private RecyclerView rvPhotos;
//    private TextView tvPhotoCount;
//    private Button btnBackPhotos, btnAddPhotos, btnNextPhotos;
//
//    private final ArrayList<Uri> selectedPhotoUris = new ArrayList<>();
//    private SelectedPhotosAdapter adapter;
//
//    // keep all previous data for next step
//    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress, hotelOtherAmenities;
//    private double hotelLat, hotelLng;
//    private ArrayList<String> hotelAmenities;
//
//    private ActivityResultLauncher<Intent> pickImagesLauncher;
//    private boolean isEditMode;
//    private String hotelId;
//    private ArrayList<String> existingPhotoUrls;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_upload_hotel_photos);
//
//        bindViews();
//        readIncomingExtras();
//        setupRecycler();
//        setupPickerLauncher();
//        setupClicks();
//        updateCountText();
//    }
//
//    private void bindViews() {
//        rvPhotos = findViewById(R.id.rvPhotos);
//        tvPhotoCount = findViewById(R.id.tvPhotoCount);
//        btnBackPhotos = findViewById(R.id.btnBackPhotos);
//        btnAddPhotos = findViewById(R.id.btnAddPhotos);
//        btnNextPhotos = findViewById(R.id.btnNextPhotos);
//    }
//
//    private void readIncomingExtras() {
//        Intent i = getIntent();
//        hotelName = i.getStringExtra("hotelName");
//        hotelDescription = i.getStringExtra("hotelDescription");
//        hotelPhone = i.getStringExtra("hotelPhone");
//        hotelEmail = i.getStringExtra("hotelEmail");
//        hotelAddress = i.getStringExtra("hotelAddress");
//        hotelLat = i.getDoubleExtra("hotelLat", 0.0);
//        hotelLng = i.getDoubleExtra("hotelLng", 0.0);
//        hotelAmenities = i.getStringArrayListExtra("hotelAmenities");
//        hotelOtherAmenities = i.getStringExtra("hotelOtherAmenities");
//        isEditMode = i.getBooleanExtra("isEditMode", false);
//        hotelId = i.getStringExtra("hotelId");
//        existingPhotoUrls = i.getStringArrayListExtra("hotelPhotoUrls");
//        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();
//    }
//
//    private void setupRecycler() {
//        adapter = new SelectedPhotosAdapter(selectedPhotoUris, position -> {
//            if (position >= 0 && position < selectedPhotoUris.size()) {
//                selectedPhotoUris.remove(position);
//                adapter.notifyItemRemoved(position);
//                updateCountText();
//            }
//        });
//
//        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
//        rvPhotos.setAdapter(adapter);
//    }
//
//    private void setupPickerLauncher() {
//        pickImagesLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
//
//                    Intent data = result.getData();
//                    int remaining = MAX_PHOTOS - selectedPhotoUris.size();
//                    if (remaining <= 0) {
//                        Toast.makeText(this, "You can select up to " + MAX_PHOTOS + " photos only.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (data.getClipData() != null) {
//                        ClipData clipData = data.getClipData();
//                        int toTake = Math.min(clipData.getItemCount(), remaining);
//
//                        for (int idx = 0; idx < toTake; idx++) {
//                            Uri uri = clipData.getItemAt(idx).getUri();
//                            if (uri != null && !selectedPhotoUris.contains(uri)) {
//                                selectedPhotoUris.add(uri);
//                            }
//                        }
//
//                        if (clipData.getItemCount() > remaining) {
//                            Toast.makeText(this, "Only " + remaining + " more photos allowed.", Toast.LENGTH_SHORT).show();
//                        }
//
//                    } else if (data.getData() != null) {
//                        Uri singleUri = data.getData();
//                        if (singleUri != null && !selectedPhotoUris.contains(singleUri)) {
//                            selectedPhotoUris.add(singleUri);
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged();
//                    updateCountText();
//                }
//        );
//    }
//
//    private void setupClicks() {
//        btnBackPhotos.setOnClickListener(v -> finish());
//
//        btnAddPhotos.setOnClickListener(v -> {
//            if (selectedPhotoUris.size() >= MAX_PHOTOS) {
//                Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            pickIntent.setType("image/*");
//            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
//
//            pickImagesLauncher.launch(Intent.createChooser(pickIntent, "Select hotel photos"));
//        });
//
//        btnNextPhotos.setOnClickListener(v -> {
//            if (selectedPhotoUris.isEmpty()) {
//
//                Toast.makeText(this, "Please add at least 1 photo.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//
//            Intent next = new Intent(this, ReviewHotelActivity.class);
//            next.putExtra("hotelName", hotelName);
//            next.putExtra("hotelDescription", hotelDescription);
//            next.putExtra("hotelPhone", hotelPhone);
//            next.putExtra("hotelEmail", hotelEmail);
//            next.putExtra("hotelAddress", hotelAddress);
//            next.putExtra("hotelLat", hotelLat);
//            next.putExtra("hotelLng", hotelLng);
//            next.putStringArrayListExtra("hotelAmenities", hotelAmenities);
//            next.putExtra("hotelOtherAmenities", hotelOtherAmenities);
//            next.putParcelableArrayListExtra("hotelPhotoUris", selectedPhotoUris);
//            next.putExtra("isEditMode", isEditMode);
//            next.putExtra("hotelId", hotelId);
//            next.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);
//            startActivity(next);
//        });
//    }
//
//    private void updateCountText() {
//        tvPhotoCount.setText(selectedPhotoUris.size() + " / " + MAX_PHOTOS + " selected");
//    }
//}
package com.l227879.stayswift.admin;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
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

    // New photos picked in this session
    private final ArrayList<Uri> selectedPhotoUris = new ArrayList<>();
    // Existing uploaded URLs (for edit mode)
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();

    // Unified list for UI grid
    private final ArrayList<EditablePhotoItem> photoItems = new ArrayList<>();
    private EditablePhotosAdapter editableAdapter;

    // keep all previous data for next step
    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress, hotelOtherAmenities;
    private double hotelLat, hotelLng;
    private ArrayList<String> hotelAmenities;

    private boolean isEditMode;
    private String hotelId;

    private ActivityResultLauncher<Intent> pickImagesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_hotel_photos);

        bindViews();
        readIncomingExtras();
        setupRecycler();
        setupPickerLauncher();
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
        // Prefill existing URL photos (edit mode)
        photoItems.clear();
        for (String url : existingPhotoUrls) {
            if (url != null && !url.trim().isEmpty()) {
                photoItems.add(new EditablePhotoItem(url));
            }
        }

        editableAdapter = new EditablePhotosAdapter(photoItems, position -> {
            if (position < 0 || position >= photoItems.size()) return;

            EditablePhotoItem item = photoItems.get(position);

            if (item.type == EditablePhotoItem.Type.EXISTING_URL) {
                // Remove from existing URLs too
                existingPhotoUrls.remove(item.url);
            } else if (item.type == EditablePhotoItem.Type.NEW_URI) {
                // Remove from new URIs too
                selectedPhotoUris.remove(item.uri);
            }

            photoItems.remove(position);
            editableAdapter.notifyItemRemoved(position);
            updateCountText();
        });

        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        rvPhotos.setAdapter(editableAdapter);
    }

    private void setupPickerLauncher() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                    Intent data = result.getData();
                    int remaining = MAX_PHOTOS - photoItems.size();
                    if (remaining <= 0) {
                        Toast.makeText(this, "You can select up to " + MAX_PHOTOS + " photos only.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        int toTake = Math.min(clipData.getItemCount(), remaining);

                        for (int idx = 0; idx < toTake; idx++) {
                            Uri uri = clipData.getItemAt(idx).getUri();
                            if (uri != null && !selectedPhotoUris.contains(uri)) {
                                selectedPhotoUris.add(uri);
                                photoItems.add(new EditablePhotoItem(uri));
                            }
                        }

                        if (clipData.getItemCount() > remaining) {
                            Toast.makeText(this, "Only " + remaining + " more photos allowed.", Toast.LENGTH_SHORT).show();
                        }

                    } else if (data.getData() != null) {
                        Uri singleUri = data.getData();
                        if (singleUri != null && !selectedPhotoUris.contains(singleUri)) {
                            if (photoItems.size() >= MAX_PHOTOS) {
                                Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            selectedPhotoUris.add(singleUri);
                            photoItems.add(new EditablePhotoItem(singleUri));
                        }
                    }

                    editableAdapter.notifyDataSetChanged();
                    updateCountText();
                }
        );
    }

    private void setupClicks() {
        btnBackPhotos.setOnClickListener(v -> finish());

        btnAddPhotos.setOnClickListener(v -> {
            if (photoItems.size() >= MAX_PHOTOS) {
                Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("image/*");
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);

            pickImagesLauncher.launch(Intent.createChooser(pickIntent, "Select hotel photos"));
        });

        btnNextPhotos.setOnClickListener(v -> {
            if (photoItems.isEmpty()) {
                Toast.makeText(this, "Please add at least 1 photo.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent next = new Intent(this, ReviewHotelActivity.class);
            next.putExtra("hotelName", hotelName);
            next.putExtra("hotelDescription", hotelDescription);
            next.putExtra("hotelPhone", hotelPhone);
            next.putExtra("hotelEmail", hotelEmail);
            next.putExtra("hotelAddress", hotelAddress);
            next.putExtra("hotelLat", hotelLat);
            next.putExtra("hotelLng", hotelLng);
            next.putStringArrayListExtra("hotelAmenities", hotelAmenities);
            next.putExtra("hotelOtherAmenities", hotelOtherAmenities);

            // New local images selected now
            next.putParcelableArrayListExtra("hotelPhotoUris", selectedPhotoUris);

            // Edit context + remaining existing URLs
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