package com.example.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

public class CardsRecyclerView extends RecyclerView.Adapter<CardsRecyclerView.ViewHolder> implements PopupMenu.OnMenuItemClickListener{

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

//        MENU CONTROLLER
        PopupMenu popupMenu = new PopupMenu(parent.getContext(), holder.optionBTN);
        popupMenu.inflate(R.menu.card_menu);
        Menu popup = popupMenu.getMenu();
        if(!dataList.get(position).getEmail().equals(Server.currentUser.getEmail())){
            popup.findItem(R.id.deletePostOP).setVisible(false);
            popup.findItem(R.id.editPostOP).setVisible(false);
        }
        holder.optionBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        popup.findItem(R.id.editPostOP).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(parent.getContext(), EditPost.class);
                intent.putExtra("post" ,dataList.get(position));
                parent.getContext().startActivity(intent);
                return false;
            }
        });

        popup.findItem(R.id.deletePostOP).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deletePost(holder, dataList.get(position));
                return false;
            }
        });

        popup.findItem(R.id.reportPostOP).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ReportData reportData = new ReportData();
                reportData.setPostID(dataList.get(position).getObjectId());
                reportData.setReporter(Server.currentUser.getEmail());
                Backendless.Data.of(ReportData.class).save(reportData, new AsyncCallback<ReportData>() {
                    @Override
                    public void handleResponse(ReportData response) {
                        Toast.makeText(parent.getContext(), "Post Reported", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.i("TAG", "handleFault: "+ fault.getMessage());
                    }
                });
                return false;
            }
        });
//

        try {
            DataQueryBuilder query = DataQueryBuilder.create();
            query.setWhereClause("email = " + "'"+dataList.get(position).getEmail()+"'");
            Backendless.Data.of(BackendlessUser.class).find(query, new AsyncCallback<List<BackendlessUser>>() {
                        @Override
                        public void handleResponse(List<BackendlessUser> response) {
                            try {
                                holder.cardTitleTV.setText(response.get(0).getProperty("name").toString());
                                CardsRecyclerView.thubnailUrl = response.get(0).getProperty("profileImage").toString();
                                Glide.with(parent.getContext()).load(thubnailUrl)
                                        .placeholder(R.mipmap.profile_placeholder)
                                        .circleCrop()
                                        .into(holder.thumbnail);
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

    private void deletePost(ViewHolder holder, Post post){

        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
        builder.setMessage("Are You Sure?");
        builder.setTitle("Delete Post? ");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String whereClause = "postId = '" + post.getObjectId() + "'";
                Backendless.Data.of(ReadLaterTabel.class).remove(whereClause, new AsyncCallback<Integer>() {
                    @Override
                    public void handleResponse(Integer response) {
                        Log.i("TAG", "post ready to be deleted");
                        Backendless.Data.of(Post.class).remove(post, new AsyncCallback<Long>() {
                            @Override
                            public void handleResponse(Long response) {
                                Toast.makeText(parent.getContext(), "Post Deleted Successfully ", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.i("TAG", "handleFault: " + fault.getMessage());
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.i("TAG", "handleFault: " + fault.getMessage());
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

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


//    Menu Item click Listener
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.editPostOP:

                break;
        }
        return false;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView cardTitleTV;
        private  TextView cardSubTitleTV;
        private  TextView descriptionHeaderTV;
        private  TextView descriptionTV;
        private  ImageView cardImage;
        private ImageView   thumbnail, optionBTN;
        private  Button moreBTN, readLaterBTN;
        private MenuItem deletePostOP;
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
            optionBTN = itemView.findViewById(R.id.optionsBTN);
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }

}
