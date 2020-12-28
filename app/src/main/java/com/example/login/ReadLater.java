package com.example.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private RecyclerView cardsRecyclerView;
    private ImageView reloadBTN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_later);

        //progress bar
        progressBar = findViewById(R.id.ma_progressBar);
        progressBarLayout = findViewById(R.id.ma_progressLayout);
        progressBarLabel = findViewById(R.id.ma_progressLabel);
        fromLayout = findViewById(R.id.readLater);
        showProgress(true);
        cardsRecyclerView =findViewById(R.id.cardsRecyclerView);
        reloadBTN = findViewById(R.id.reloadButton);

        reloadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                loadReadlaterFeed();
            }
        });

        loadReadlaterFeed();

    }

    private void loadReadlaterFeed(){
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
                    query.setWhereClause(whereClause2);
                    Backendless.Data.of(Post.class).find(query, new AsyncCallback<List<Post>>() {
                        @Override
                        public void handleResponse(List<Post> response) {
                            RLCardsRecyclerView adapter = new RLCardsRecyclerView();
                            adapter.setDataList(response);
                            cardsRecyclerView.setAdapter(adapter);
                            cardsRecyclerView.setLayoutManager(new LinearLayoutManager(ReadLater.this));
                            showProgress(false);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            showProgress(false);
                            Toast.makeText(ReadLater.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    showProgress(false);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(ReadLater.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
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
}