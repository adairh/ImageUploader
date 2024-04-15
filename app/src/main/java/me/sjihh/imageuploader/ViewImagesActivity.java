package me.sjihh.imageuploader;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<String> imageUrls = new ArrayList<>();
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ProgressBar progressBar;
    private int loadedImageCount = 0;
    private int pageSize = 20; // Adjust as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_view_images);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Set the span count for grid layout
        adapter = new ImageAdapter(imageUrls);
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images");

        loadImagesFromFirebase();
    }

    private void loadImagesFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    int totalItems = listResult.getItems().size();
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.add(uri.toString());
                            loadedImageCount++;
                            if (loadedImageCount % pageSize == 0 || loadedImageCount == totalItems) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ViewImagesActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
                });
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private List<String> imageUrls;

        ImageAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);

            Glide.with(ViewImagesActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .override(150, 150) // Resizing to reduce memory footprint
                    .thumbnail(0.1f) // Load a low-resolution thumbnail first
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(ViewImagesActivity.this, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}