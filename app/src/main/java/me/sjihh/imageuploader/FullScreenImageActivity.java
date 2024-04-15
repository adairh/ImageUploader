package me.sjihh.imageuploader;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;
    private TextView imgName, imgSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        imgName = findViewById(R.id.imgName);
        imgSize = findViewById(R.id.imgSize);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        String imageName = extractImageNameFromUrl(imageUrl);

        Glide.with(this)
            .load(imageUrl)
            .into(fullScreenImageView);

        imgName.setText(imageName);

        getImageSizeFromFirebaseStorage(imageUrl);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadPopup(imageUrl);
            }
        });
    }

    private void getImageSizeFromFirebaseStorage(String imageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long sizeInBytes = storageMetadata.getSizeBytes();
                String formattedSize = formatFileSize(sizeInBytes);
                imgSize.setText("Size: " + formattedSize);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("AAAAAAAAAAAAAAAA");
            }
        });
    }

    private String formatFileSize(long sizeInBytes) {
        // Helper method to format the file size into a readable format
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = sizeInBytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    private String extractImageNameFromUrl(String imageUrl) {
        String[] urlParts = imageUrl.split("/");
        String lastPart = urlParts[urlParts.length - 1];

        String[] lastPartSplit = lastPart.split("\\?");
        String imageName = lastPartSplit[0];

        imageName = imageName.replace("%2F", "/").replace("images/", "").replace("%20", " ");

        return imageName;
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