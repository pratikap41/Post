package com.example.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class EditPost extends Activity {

    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private TextView cardTitleTV, cardsubTitleTV;
    private EditText descriptionHeaderET, descriptionET;
    private ImageView cardImage, thumbnailImage;
    private Button postBTN, cancelBTN;
    private String thumbnailURL = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        //progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.ps_createPostLayout);
        showProgress(true);
//   View Init
        cardImage = findViewById(R.id.ps_cardImage);
        cardTitleTV = findViewById(R.id.ps_cardTitle);
        postBTN = findViewById(R.id.ps_postButton);
        cancelBTN = findViewById(R.id.ps_cancelButton);
        descriptionHeaderET = findViewById(R.id.ps_descriptionHeader);
        descriptionET = findViewById(R.id.ps_content);
        thumbnailImage = findViewById(R.id.ps_cardThumbnail);
        Post post = (Post)getIntent().getSerializableExtra("post");
        try{
            thumbnailURL = Server.currentUser.getProperty("profileImage").toString();
        }
        catch (Exception e){
            e.printStackTrace();
            showProgress(false);
        }

//    Setting-up Views value
        descriptionHeaderET.setText(post.getDescriptionTitle());
        descriptionET.setText(post.getContent());
        cardTitleTV.setText(post.getName());
        postBTN.setText("UPDATE");
        Glide.with(EditPost.this)
                .load(post.getCardImage())
                .placeholder(R.mipmap.add_image_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(cardImage);

        Glide.with(EditPost.this)
                .load(thumbnailURL)
                .placeholder(R.mipmap.profile_placeholder)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(thumbnailImage);

        showProgress(false);
//        Listener
        postBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = descriptionET.getText().toString().trim();
                String header = descriptionHeaderET.getText().toString().trim();

                if(content.equals(post.getContent()) && header.equals(post.getDescriptionTitle())){
                    Toast.makeText(EditPost.this, "Nothing To Update", Toast.LENGTH_SHORT).show();
                }
                else{
                    showProgress(true);
                    post.setDescriptionTitle(header);
                    post.setContent(content);
                    Backendless.Data.of(Post.class).save(post, new AsyncCallback<Post>() {
                        @Override
                        public void handleResponse(Post response) {
                            Toast.makeText(EditPost.this, "Post Updated", Toast.LENGTH_SHORT).show();
                            EditPost.this.finish();
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i("TAG", "handleFault: "+fault.getMessage());
                            showProgress(false);
                        }
                    });
                }
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPost.this.finish();
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
