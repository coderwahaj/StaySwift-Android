package com.l227879.stayswift.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.l227879.stayswift.R;

public class CustomerSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        EditText name = findViewById(R.id.etSupportName);
        EditText email = findViewById(R.id.etSupportEmail);
        EditText msg = findViewById(R.id.etSupportMessage);
        Button submit = findViewById(R.id.btnSubmitSupport);

        submit.setOnClickListener(v -> {
            String body = "Name: " + name.getText().toString() + "\n"
                    + "Email: " + email.getText().toString() + "\n\n"
                    + msg.getText().toString();

            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:support@stayswift.com"));
            i.putExtra(Intent.EXTRA_SUBJECT, "StaySwift Support");
            i.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(i);
        });
    }
}