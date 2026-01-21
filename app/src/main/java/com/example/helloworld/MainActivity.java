package com.example.helloworld;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = findViewById(R.id.btnHello);
        b.setOnClickListener(v -> Toast.makeText(this, "Hello from APK Builder!", Toast.LENGTH_SHORT).show());
    }
}
