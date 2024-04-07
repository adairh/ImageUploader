package me.sjihh.imageuploader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Button uploadButton, viewButton;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_upload);

        imageView = findViewById(R.id.imageView);
        uploadButton = findViewById(R.id.uploadButton);
        viewButton = findViewById(R.id.viewButton);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        imageView.setOnClickListener(v -> openFileChooser());

        uploadButton.setOnClickListener(v -> {
            if (imageUri != null) {
                Toast.makeText(UploadActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(UploadActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        viewButton.setOnClickListener(v ->{
            Intent intent = new Intent( this, ViewImagesActivity.class);
            startActivity(intent);
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageView);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        // Declare a final copy of imageUri
        final Uri finalImageUri = imageUri;

        fileRef.putFile(finalImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(UploadActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    imageView.setImageResource(android.R.color.transparent);
                    // You can't assign null to the original imageUri variable here
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }
}