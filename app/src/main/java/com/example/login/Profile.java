package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private static  View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private Uri profileURI;
    private String profileURL = "profile_images/";
    private ImageView profilePic;
    private ImageView reloadBTN, homeBTN;
    private RecyclerView cardsRecyclerView;
    private TextView nameVT, emailVT, postsVT;
    private   CardsRecyclerView adapter = new CardsRecyclerView();
    private  static  boolean profileChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressBar = findViewById(R.id.p_progressBar);
        progressBarLayout = findViewById(R.id.p_progressLayout);
        progressBarLabel = findViewById(R.id.p_progressLabel);
        fromLayout = findViewById(R.id.profile_layout);

        profilePic = findViewById(R.id.profilePic);
        reloadBTN = findViewById(R.id.reloadButton);
        homeBTN = findViewById(R.id.homeButton);
        cardsRecyclerView = findViewById(R.id.p_cardsRecyclerView);
        nameVT = findViewById(R.id.p_name);
        emailVT = findViewById(R.id.p_email);
        postsVT = findViewById(R.id.posts);
        cardsRecyclerView.setNestedScrollingEnabled(false);

        showProgress(true);
        nameVT.setText(Server.currentUser.getProperty("name").toString());
        emailVT.setText(Server.currentUser.getEmail());
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "select Image"), 1);
            }
        });

        homeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, MainActivity.class));
            }
        });

        reloadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile.this.recreate();
            }
        });

        loadProfile();
        cardsRecyclerView.setAdapter(adapter);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadProfile() {
        showProgress(true);
        try{
            String profilePicURL = Server.currentUser.getProperty("profileImage").toString();
            Glide.with(this).load(Server.ImageUrl+ profileURL + Server.currentUser.getEmail()+".jpeg")
                    .placeholder(R.mipmap.profile_placeholder)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(profileChanged)
                    .into(profilePic);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        String whereClause = "email = " + "'" + Server.currentUser.getEmail()+"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setGroupBy("created");
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Post.class).find(queryBuilder, new AsyncCallback<List<Post>>() {
            @Override
            public void handleResponse(List<Post> response) {
                String noOfPosts = "Total Posts : " + Integer.toString(response.size());
                postsVT.setText(noOfPosts);
                adapter.setDataList((ArrayList<Post>) response);
                showProgress(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1){
            try {
                showProgress(true);
                profileURI = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileURI);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, new ByteArrayOutputStream());
                Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 50, Server.currentUser.getEmail() + ".jpeg", "profile_images", true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile response) {

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Server.currentUser.setProperty("profileImage", Server.ImageUrl+ profileURL + Server.currentUser.getEmail()+".jpeg");
                Backendless.UserService.update(Server.currentUser, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        showProgress(false);
                        profileChanged = true;
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                });
            } catch (Exception e) {
                showProgress(false);
                e.printStackTrace();
                Toast.makeText(Profile.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    //progress bar
    public static void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }
}