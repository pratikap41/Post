package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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


    private BackendlessUser profileUser;
    private Uri profileURI;
    private String profileURL = "profile_images/";
    private ImageView profilePic;
    private ImageView  homeBTN, optionsBTN;
    private RecyclerView cardsRecyclerView;
    private TextView nameVT, emailVT, postsVT, youtPostTV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private   CardsRecyclerView adapter = new CardsRecyclerView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profilePic = findViewById(R.id.profilePic);
        homeBTN = findViewById(R.id.homeButton);
        cardsRecyclerView = findViewById(R.id.p_cardsRecyclerView);
        nameVT = findViewById(R.id.p_name);
        emailVT = findViewById(R.id.p_email);
        postsVT = findViewById(R.id.posts);
        youtPostTV = findViewById(R.id.yourPostTxt);
        optionsBTN = findViewById(R.id.optionsBTN);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        optionsBTN.setVisibility(View.INVISIBLE);
        homeBTN.setVisibility(View.INVISIBLE);
        youtPostTV.setText("Posts");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                loadUser();
            }
        });
        loadUser();
    }

    private void loadUser(){
        swipeRefreshLayout.setRefreshing(true);
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
                    loadProfile();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ProfileSecondary.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
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
        DataQueryBuilder queryBuilder2 = DataQueryBuilder.create();
        queryBuilder2.setSortBy("created DESC");
        queryBuilder2.setWhereClause(whereClause);
        Backendless.Data.of(Post.class).find(queryBuilder2, new AsyncCallback<List<Post>>() {
            @Override
            public void handleResponse(List<Post> response) {
                String noOfPosts = "Total Posts : " + Integer.toString(response.size());
                postsVT.setText(noOfPosts);
                adapter.setDataList((ArrayList<Post>) response);
                cardsRecyclerView.setAdapter(adapter);
                cardsRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileSecondary.this));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ProfileSecondary.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }



}