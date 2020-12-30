package com.example.login;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CardsRecyclerView extends RecyclerView.Adapter<CardsRecyclerView.ViewHolder> {

    private static ViewGroup parent;
    private SimpleDateFormat formatter;
    private List<Post> dataList = new ArrayList<Post>();
    public  static String thubnailUrl;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        CardsRecyclerView.parent = parent;
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        holder.cardTitleTV.setText(dataList.get(position).getName());
        holder.cardSubTitleTV.setText(getDuration(dataList.get(position).getCreated()));
        holder.descriptionHeaderTV.setText((dataList.get(position).getDescriptionTitle()));
        String content = dataList.get(position).getContent();
        holder.descriptionTV.setText(content.substring(0, Math.min(content.length(), 100)));


//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(dataList.get(position).getCardImage()).getContent());
//                    holder.cardImage.setImageBitmap(bitmap);
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
        try {
            DataQueryBuilder query = DataQueryBuilder.create();
            query.setWhereClause("email = " + "'"+dataList.get(position).getEmail()+"'");
            Backendless.Data.of(BackendlessUser.class).find(query, new AsyncCallback<List<BackendlessUser>>() {
                        @Override
                        public void handleResponse(List<BackendlessUser> response) {
                            try {
                                CardsRecyclerView.thubnailUrl = response.get(0).getProperty("profileImage").toString();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(parent.getContext(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            Glide.with(parent.getContext()).load(thubnailUrl)
                    .placeholder(R.mipmap.profile_placeholder)
                    .centerCrop()
                    .into(holder.thumbnail);
            Glide.with(parent.getContext())
                    .load(dataList.get(position).getCardImage())
                    .placeholder(R.mipmap.add_image_placeholder)
                    .fitCenter()
                    .into(holder.cardImage);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        holder.moreBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expanded) {
                    holder.descriptionTV.setText(content.substring(0, Math.min(content.length(), 100)));
                    holder.moreBTN.setText("MORE");
                    holder.setExpanded(false);
                } else {
                    holder.descriptionTV.setText(content);
                    holder.moreBTN.setText("LESS");
                    holder.setExpanded(true);
                }
            }
        });

        holder.readLaterBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postId = dataList.get(position).getObjectId();
                String userEmail = Server.currentUser.getEmail();
                ReadLaterTabel obj = new ReadLaterTabel();
                obj.setId(userEmail+postId);
                obj.setPostId(postId);
                obj.setUserEmail(userEmail);
                Backendless.Data.of(ReadLaterTabel.class).save(obj, new AsyncCallback<ReadLaterTabel>() {
                    @Override
                    public void handleResponse(ReadLaterTabel response) {
                        Toast.makeText(parent.getContext(), "Added To Read Later", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(parent.getContext(), "Already Added", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.cardTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent.getContext(), ProfileSecondary.class);
                intent.putExtra("userID", dataList.get(position).getEmail() );
                parent.getContext().startActivity(intent);
            }
        });

    }

    public void clear(){
        dataList.clear();
        notifyDataSetChanged();
    }

    public void setDataList(ArrayList<Post> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    private String getDuration(Date date){
        Date now = new Date();
        long duration = TimeUnit.MILLISECONDS.toSeconds( now.getTime() - date.getTime());
        final long MIN = 60;
        final long HOUR = 3600;
        final long DAYS = 86400;
        final long WEEK = 604800;
        final long MONTH = 2628002;
        final long YEAR = 31536000;
        String returnValue;

        if(duration < MIN){
            return Long.toString(duration) + " Seconds Ago";
        }
        else if(duration< HOUR ){
            returnValue = Long.toString(TimeUnit.SECONDS.toMinutes(duration));
            return returnValue + " Minutes ago ";
        }
        else if(duration < DAYS){
            returnValue = Long.toString(TimeUnit.SECONDS.toHours(duration));
            return returnValue + " Hours ago ";
        }
        else if(duration < WEEK){
            returnValue = Long.toString(TimeUnit.SECONDS.toDays(duration));
            return returnValue + " Day ago ";
        }
        else if(duration < MONTH ){
            returnValue = Long.toString(duration / WEEK) ;
            return returnValue + " Weeks ago ";
        }
        else if(duration < YEAR){
            returnValue = Long.toString(duration / MONTH) ;
            return returnValue + " Months ago ";
        }
        returnValue = Long.toString(duration / YEAR) ;
        return  returnValue + " Years ago";
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView cardTitleTV;
        private  TextView cardSubTitleTV;
        private  TextView descriptionHeaderTV;
        private  TextView descriptionTV;
        private  ImageView cardImage;
        private ImageView   thumbnail;
        private  Button moreBTN, readLaterBTN;
        private boolean expanded = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTitleTV = itemView.findViewById(R.id.cardTitle);
            cardSubTitleTV = itemView.findViewById(R.id.cardSubTitle);
            descriptionHeaderTV = itemView.findViewById(R.id.cardDescriptionTitle);
            descriptionTV = itemView.findViewById(R.id.cardDescription);
            cardImage = itemView.findViewById(R.id.cardImage);
            moreBTN = itemView.findViewById(R.id.cardMoreButton);
            thumbnail = itemView.findViewById(R.id.cardThumbnail);
            readLaterBTN = itemView.findViewById(R.id.cardReadLaterButton);
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }

}
