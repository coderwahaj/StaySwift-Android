package com.l227879.stayswift.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class AdminNotificationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;

    private final ArrayList<Map<String, Object>> data = new ArrayList<>();
    private AdminNotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        rv = findViewById(R.id.rvAdminNotifs);
        progress = findViewById(R.id.progressAdminNotifs);
        tvEmpty = findViewById(R.id.tvEmptyAdminNotifs);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminNotificationsAdapter(data);
        rv.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("admin_notifications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        data.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Object val = s.getValue();
                            if (val instanceof Map) {
                                //noinspection unchecked
                                data.add((Map<String, Object>) val);
                            }
                        }

                        Collections.sort(data, (a, b) -> {
                            long ta = a.get("createdAt") == null ? 0 : (long) a.get("createdAt");
                            long tb = b.get("createdAt") == null ? 0 : (long) b.get("createdAt");
                            return Long.compare(tb, ta);
                        });

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(error.getMessage());
                    }
                });
    }
}