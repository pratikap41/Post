package com.example.login;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreatePost extends AppCompatActivity {

    static final int PICK_IMAGE = 1;
    private static boolean imageSelected = false; //to check if image is selected by user or not
    private Bitmap bitmap;
    private Button postBTN, cancelBTN;
    private Uri imageUri;
    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private TextView cardTitleTV, cardsubTitleTV;
    private EditText descriptionHeaderET, descriptionET;
    private ImageView cardImage, thumbnailImage;
    private String cardImageName;
    private final String cardImageUrl = "post_images/"; //uploaded image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
//progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.ps_createPostLayout);

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy_hhmmss");
        cardImage = findViewById(R.id.ps_cardImage);
        cardTitleTV = findViewById(R.id.ps_cardTitle);
        postBTN = findViewById(R.id.ps_postButton);
        cancelBTN = findViewById(R.id.ps_cancelButton);
        descriptionHeaderET = findViewById(R.id.ps_descriptionHeader);
        descriptionET = findViewById(R.id.ps_content);
        thumbnailImage = findViewById(R.id.ps_cardThumbnail);


        try{
            String URL = Server.currentUser.getProperty("profileImage").toString();
            Glide.with(this)
                    .load(URL)
                    .placeholder(R.mipmap.profile_placeholder)
                    .centerCrop()
                    .into(thumbnailImage);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select Image"), PICK_IMAGE);
            }
        });
        cardTitleTV.setText(Server.currentUser.getProperty("name").toString());

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatePost.this.finish();
            }
        });

        postBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descriptionHeader = descriptionHeaderET.getText().toString().trim();
                String description = descriptionET.getText().toString().trim();
                if (description.isEmpty() || descriptionHeader.isEmpty()) {
                    Toast.makeText(CreatePost.this, "Fill-Up Required Fields", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(true);
                    if (imageSelected) {
                        imageSelected = false;
                        Date currentDate = new Date();
                        cardImageName = Server.currentUser.getEmail() + "_" + formatter.format(currentDate) + ".jpeg";
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, new ByteArrayOutputStream());
                        Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 50, cardImageName, "post_images", new AsyncCallback<BackendlessFile>() {
                            @Override
                            public void handleResponse(BackendlessFile response) {
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                showProgress(false);
                                Toast.makeText(CreatePost.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Post post = new Post();
                        try{
                            post.setCardImage(Server.ImageUrl + cardImageUrl + cardImageName);
                            post.setContent(description);
                            post.setDescriptionTitle(descriptionHeader);
                            post.setName(Server.currentUser.getProperty("name").toString());
                            post.setEmail(Server.currentUser.getEmail());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        Backendless.Data.of(Post.class).save(post, new AsyncCallback<Post>() {
                            @Override
                            public void handleResponse(Post response) {
                                showProgress(false);
                                Toast.makeText(CreatePost.this, "Posted Successfully", Toast.LENGTH_SHORT).show();
                                CreatePost.this.finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                showProgress(false);
                                Toast.makeText(CreatePost.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        showProgress(false);
                        Toast.makeText(CreatePost.this, "Please Upload Image", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                cardImage.setImageBitmap(bitmap);
                imageSelected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //progress bar
    public void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }

}