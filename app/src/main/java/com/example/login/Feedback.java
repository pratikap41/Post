package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.material.appbar.MaterialToolbar;

public class Feedback extends AppCompatActivity {

    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private MaterialToolbar appTopBar;
    private RatingBar ratingBar;
    private EditText feedbackCommentET;
    private Button submitBTN, cancelBTN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        //progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.feedbackLayout);

        appTopBar = findViewById(R.id.appTopBar);
        appTopBar.setTitle("FeedBack");
        ratingBar = findViewById(R.id.ratingBar);
        submitBTN = findViewById(R.id.submitBTN);
        cancelBTN = findViewById(R.id.cancelBTN);
        feedbackCommentET = findViewById(R.id.fedbackComment);

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedbackComment = feedbackCommentET.getText().toString().trim();
                if(feedbackComment.isEmpty()){
                    Toast.makeText(Feedback.this, "Please Fill-up The Required Fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    showProgress(true);
                    FeedbackData feedbackData = new FeedbackData();
                    feedbackData.setRating(ratingBar.getRating());
                    feedbackData.setFrom(Server.currentUser.getEmail());
                    feedbackData.setFeedbackComment(feedbackComment);

                    Backendless.Data.of(FeedbackData.class).save(feedbackData, new AsyncCallback<FeedbackData>() {
                        @Override
                        public void handleResponse(FeedbackData response) {
                            Toast.makeText(Feedback.this, "Feedback Submitted", Toast.LENGTH_SHORT).show();
                            Feedback.this.finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            showProgress(false);
                            Toast.makeText(Feedback.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Feedback.this.finish();
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