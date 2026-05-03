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
import com.l227879.stayswift.models.NotificationItem;
import com.l227879.stayswift.user.NotificationsAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class AdminNotificationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;
    private final ArrayList<NotificationItem> list = new ArrayList<>();
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notifications);

        rv = findViewById(R.id.rvUserNotifs);
        progress = findViewById(R.id.progressUserNotifs);
        tvEmpty = findViewById(R.id.tvEmptyUserNotifs);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(list);
        rv.setAdapter(adapter);

        loadNotifs();
    }

    private void loadNotifs() {
        progress.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("admin_notifications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        list.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            NotificationItem n = s.getValue(NotificationItem.class);
                            if (n != null) list.add(n);
                        }
                        Collections.sort(list, (a, b) -> Long.compare(b.createdAt, a.createdAt));
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                    }
                });
    }
}