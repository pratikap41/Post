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

public class ProfileSecondary extends AppCompatActivity {

    private static  View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar

    private BackendlessUser profileUser;
    private Uri profileURI;
    private String profileURL = "profile_images/";
    private ImageView profilePic;
    private ImageView reloadBTN, homeBTN;
    private RecyclerView cardsRecyclerView;
    private TextView nameVT, emailVT, postsVT, youtPostTV;
    private   CardsRecyclerView adapter = new CardsRecyclerView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_secondary);

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
        youtPostTV = findViewById(R.id.yourPostTxt);
        cardsRecyclerView.setNestedScrollingEnabled(false);


        showProgress(true);
        youtPostTV.setText("Posts");
        String userEmail = getIntent().getStringExtra("userID");
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("email = "+"'" + userEmail+"'");
        Backendless.Data.of(BackendlessUser.class).find(queryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                if(!response.isEmpty()){
                    profileUser = response.get(0);
                    nameVT.setText(profileUser.getProperty("name").toString());
                    emailVT.setText(userEmail);
                    reloadBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadProfile();
                        }
                    });

                    loadProfile();
                    cardsRecyclerView.setAdapter(adapter);
                    cardsRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileSecondary.this));
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ProfileSecondary.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void loadProfile() {
        showProgress(true);
        try{
            String profilePicURL = profileUser.getProperty("profileImage").toString();
            Glide.with(this).load(Server.ImageUrl+ profileURL + profileUser.getEmail()+".jpeg")
                    .placeholder(R.mipmap.profile_placeholder)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profilePic);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        String whereClause = "email = " + "'" + profileUser.getEmail()+"'";
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
                Toast.makeText(ProfileSecondary.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });

    }


    //progress bar
    public static void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }
}