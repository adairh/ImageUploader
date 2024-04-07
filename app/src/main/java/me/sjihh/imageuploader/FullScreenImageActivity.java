package me.sjihh.imageuploader;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        Glide.with(this)
            .load(imageUrl)
            .into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadPopup(imageUrl);
            }
        });
    }

    private void showDownloadPopup(String imageUrl) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_download_image);
        dialog.setCancelable(true);

        Button downloadButton = dialog.findViewById(R.id.downloadButton);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(FullScreenImageActivity.this, ImageDownloadService.class);
                serviceIntent.putExtra("imageUrl", imageUrl);
                startService(serviceIntent);

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}