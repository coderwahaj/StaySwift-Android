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
import com.l227879.stayswift.models.SupportRequest;

import java.util.ArrayList;
import java.util.Collections;

public class AdminSupportActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;
    private final ArrayList<SupportRequest> list = new ArrayList<>();
    private SupportRequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support);

        rv = findViewById(R.id.rvSupport);
        progress = findViewById(R.id.progressSupport);
        tvEmpty = findViewById(R.id.tvSupportEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupportRequestsAdapter(list);
        rv.setAdapter(adapter);

        loadSupportRequests();
    }

    private void loadSupportRequests() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("support_requests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        list.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            SupportRequest r = s.getValue(SupportRequest.class);
                            if (r != null) list.add(r);
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