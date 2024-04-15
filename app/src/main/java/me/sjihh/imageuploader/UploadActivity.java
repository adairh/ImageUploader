package me.sjihh.imageuploader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Button uploadButton, viewButton;
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_upload);

        uploadButton = findViewById(R.id.uploadButton);
        viewButton = findViewById(R.id.viewButton);
        gridView = findViewById(R.id.gridView);

        imageAdapter = new ImageAdapter(this, imageUriList);
        gridView.setAdapter(imageAdapter);

        Button selectImagesButton = findViewById(R.id.selectImagesButton);
        selectImagesButton.setOnClickListener(v -> openFileChooser());
        // imageView.setOnClickListener(v -> openFileChooser());

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        uploadButton.setOnClickListener(v -> {
            if (!imageUriList.isEmpty()) {
                Toast.makeText(UploadActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
                for (Uri imageUri : imageUriList) {
                    uploadImageToFirebase(imageUri);
                }
            } else {
                Toast.makeText(UploadActivity.this, "Please select images", Toast.LENGTH_SHORT).show();
            }
        });

        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewImagesActivity.class);
            startActivity(intent);
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow selecting multiple images
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUriList.add(imageUri);
                }
            } else {
                // Single image selected
                Uri imageUri = data.getData();
                imageUriList.add(imageUri);
            }

            imageAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
        }
    }


    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(UploadActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    imageUriList.clear(); // Clear the imageUriList
                    imageAdapter.notifyDataSetChanged(); // Notify the adapter of the data change
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }
}