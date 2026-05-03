package com.l227879.stayswift.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.NotificationItem;

import java.util.ArrayList;
import java.util.Collections;

public class UserNotificationsActivity extends AppCompatActivity {

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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        progress.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("notifications")
                .child(uid)
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