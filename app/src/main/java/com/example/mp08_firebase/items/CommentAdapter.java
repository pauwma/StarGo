package com.example.mp08_firebase.items;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mp08_firebase.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private List<Comment> comments;
    private Context context;
    private String uid;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.comments = comments;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        Log.d("CommentAdapter", "onCreateViewHolder called");
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Log.d("CommentAdapter", "onCreateViewHolder called");
        Comment comment = comments.get(position);
        uid = comment.getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String displayName = documentSnapshot.getString("displayName");
                            String avatar = documentSnapshot.getString("avatar");

                            // Set user info in ViewHolder
                            holder.displayNameTextView.setText(displayName);
                            holder.usernameTextView.setText("@" + username);
                            Glide.with(context).load(avatar).into(holder.avatarImageView);

                            holder.avatarImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, uid);
                                }
                            });
                            holder.displayNameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, uid);
                                }
                            });
                            holder.usernameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToProfile(v, uid);
                                }
                            });
                        }
                    }
                });

        holder.contentTextView.setText(comment.getContent());
        holder.timestampTextView.setText("· " + getTimeAgo(comment.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        Log.d("CommentAdapter", "getItemCount called, returning " + comments.size());
        return comments.size();
    }

    public String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        if (timeDiff < TimeUnit.SECONDS.toMillis(60)) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
            return seconds + "s";
        } else if (timeDiff < TimeUnit.MINUTES.toMillis(60)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
            return minutes + "m";
        } else if (timeDiff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
            return hours + "h";
        } else if (timeDiff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
            return days + "d";
        } else {
            Calendar postCalendar = Calendar.getInstance();
            postCalendar.setTimeInMillis(timestamp);
            Calendar currentCalendar = Calendar.getInstance();

            if (postCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
            }
        }
    }

    public void goToProfile(View v, String uid){
        if (uid.equals(FirebaseAuth.getInstance().getUid())){
            Navigation.findNavController(v).navigate(R.id.profileFragment);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("userUID", uid); // Pasar el UID del usuario al fragmento de perfil del usuario
            Navigation.findNavController(v).navigate(R.id.usersProfileFragment, bundle);
        }
    }
}
