package com.example.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class ReadLater extends AppCompatActivity {


    private RecyclerView cardsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private  RLCardsRecyclerView adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_later);


        cardsRecyclerView =findViewById(R.id.cardsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        adapter = new RLCardsRecyclerView();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                loadReadlaterFeed();
            }
        });
        loadReadlaterFeed();

    }

    private void loadReadlaterFeed(){
        swipeRefreshLayout.setRefreshing(true);
        List<String> postIdList = new ArrayList<>();
        String whereClause = " userEmail =" + "'" + Server.currentUser.getEmail() + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(ReadLaterTabel.class).find(queryBuilder, new AsyncCallback<List<ReadLaterTabel>>() {
            @Override
            public void handleResponse(List<ReadLaterTabel> response) {
                if(response.size() > 0) {
                    for (int i = 0; i < response.size(); i++) {
                        postIdList.add(response.get(i).getPostId());
                    }

//                TODO: improvement required in  where clause i.e whereClause2

                    String whereClause2 = "objectId  in(";
                    for (int i = 0; i < postIdList.size() - 1; i++) {
                        whereClause2 += ("'" + postIdList.get(i) + "'" + ",");
                    }
                    whereClause2 += "'" + postIdList.get(postIdList.size() - 1) + "'" + ")";
                    DataQueryBuilder query = DataQueryBuilder.create();
                    query.setSortBy("created DESC");
                    query.setWhereClause(whereClause2);
                    Backendless.Data.of(Post.class).find(query, new AsyncCallback<List<Post>>() {
                        @Override
                        public void handleResponse(List<Post> response) {
                            adapter.setDataList(response);
                            cardsRecyclerView.setAdapter(adapter);
                            cardsRecyclerView.setLayoutManager(new LinearLayoutManager(ReadLater.this));
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(ReadLater.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ReadLater.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}