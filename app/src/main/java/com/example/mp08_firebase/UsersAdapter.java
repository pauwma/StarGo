package com.example.mp08_firebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<String> users;
    private OnUserClickListener onUserClickListener;

    public UsersAdapter(OnUserClickListener onUserClickListener) {
        this.users = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    public void setUsers(List<String> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view, onUserClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnUserClickListener {
        void onUserClick(String username);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView usernameTextView;
        private OnUserClickListener onUserClickListener;

        public ViewHolder(@NonNull View itemView, OnUserClickListener onUserClickListener) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            this.onUserClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(String username) {
            usernameTextView.setText(username);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onUserClickListener != null) {
                onUserClickListener.onUserClick(UsersAdapter.this.users.get(position));
            }
        }
    }
}
