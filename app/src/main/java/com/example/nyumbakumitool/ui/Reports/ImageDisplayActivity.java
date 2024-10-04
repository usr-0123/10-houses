package com.example.nyumbakumitool.ui.Reports;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nyumbakumitool.R;

public class ImageDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = findViewById(R.id.imageView);

        // Get the passed image URL from the intent
        String imageUrl = getIntent().getStringExtra("image_url");

        // Load the image into the ImageView using Glide
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);
        }
    }

}
