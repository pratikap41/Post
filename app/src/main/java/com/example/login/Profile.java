package com.example.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

public class Profile extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private Uri profileURI;
    private String profileURL = "profile_images/";
    private ImageView profilePic;
    private ImageView  homeBTN, optionBTN;
    private RecyclerView cardsRecyclerView;
    private TextView nameVT, emailVT, postsVT;
    private   CardsRecyclerView adapter = new CardsRecyclerView();
    private  static  boolean profileChanged = false;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        optionBTN = findViewById(R.id.optionsBTN);

        nameVT.setText(Server.currentUser.getProperty("name").toString());
        emailVT.setText(Server.currentUser.getEmail());


        optionBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(Profile.this, v);
                popup.inflate(R.menu.main_activity_menu);
                popup.setOnMenuItemClickListener(Profile.this);
                popup.show();
            }
        });

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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                loadProfile();
            }
        });

        loadProfile();
    }

    private void loadProfile() {
        swipeRefreshLayout.setRefreshing(true);
        try{
            String profilePicURL = Server.currentUser.getProperty("profileImage").toString();
            Glide.with(this).load(Server.ImageUrl+ profileURL + Server.currentUser.getEmail()+".jpeg")
                    .placeholder(R.mipmap.profile_placeholder)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(profileChanged)
                    .into(profilePic);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        String whereClause = "email = " + "'" + Server.currentUser.getEmail()+"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Post.class).find(queryBuilder, new AsyncCallback<List<Post>>() {
            @Override
            public void handleResponse(List<Post> response) {
                String noOfPosts = "Total Posts : " + Integer.toString(response.size());
                postsVT.setText(noOfPosts);
                adapter.setDataList((ArrayList<Post>) response);
                cardsRecyclerView.setAdapter(adapter);
                cardsRecyclerView.invalidate();
                cardsRecyclerView.setLayoutManager(new LinearLayoutManager(Profile.this));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1){
            try {
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
                        swipeRefreshLayout.setRefreshing(false);
                        profileChanged = true;
                        Profile.this.recreate();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            } catch (Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                e.printStackTrace();
                Toast.makeText(Profile.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoutOP :
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(Profile.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Profile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Profile.this.finish();
                Intent intent = new Intent(Profile.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAffinity();
                break;

            case R.id.readLaterOP:
                startActivity(new Intent(Profile.this, ReadLater.class));
                break;
            case R.id.refreshOP:
                adapter.clear();
                loadProfile();
                break;
            case R.id.settingsOP:
                startActivity(new Intent(Profile.this, Settings.class));
                break;
        }
        return false;
    }
}