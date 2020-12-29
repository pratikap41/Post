package com.example.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener{


    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private RecyclerView cardsRV;
    private View createPostBTN;
    private CardsRecyclerView adapter;
    private ImageView profileBTN, optionsBTN;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardsRV = findViewById(R.id.cardsRecyclerView);
        createPostBTN = findViewById(R.id.createPostButton);
        profileBTN = findViewById(R.id.profileButton);
        optionsBTN = findViewById(R.id.optionsBTN);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        adapter = new CardsRecyclerView();

        //progress bar
        progressBar = findViewById(R.id.ma_progressBar);
        progressBarLayout = findViewById(R.id.ma_progressLayout);
        progressBarLabel = findViewById(R.id.ma_progressLabel);
        fromLayout = findViewById(R.id.mainActivityLayout);

        loadPage();

        profileBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });

//        Backendless.Data.of(Post.class).find(new AsyncCallback<List<Post>>() {
//            @Override
//            public void handleResponse(List<Post> response) {
//                adapter.setDataList((ArrayList<Post>) response);
//                cardsRV.setAdapter(adapter);
//                cardsRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//                swipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void handleFault(BackendlessFault fault) {
//                swipeRefreshLayout.setRefreshing(false);
//                Toast.makeText(MainActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

        createPostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreatePost.class));
            }
        });

        optionsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.inflate(R.menu.main_activity_menu);
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                loadPage();
            }
        });

    }

    private void loadPage() {
        swipeRefreshLayout.setRefreshing(true);
        adapter.clear();
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created");
        Backendless.Data.of(Post.class).find(queryBuilder, new AsyncCallback<List<Post>>() {
            @Override
            public void handleResponse(List<Post> response) {
                adapter.setDataList((ArrayList<Post>) response);
                cardsRV.setAdapter(adapter);
                cardsRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //progress bar
    public void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoutOP :
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(MainActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                MainActivity.this.finish();
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAffinity();
                break;

            case R.id.readLaterOP:
                startActivity(new Intent(MainActivity.this, ReadLater.class));

            case R.id.refreshOP:
                loadPage();

            case R.id.settingsOP:
                startActivity(new Intent(MainActivity.this, Settings.class));
        }
        return false;
    }
}

