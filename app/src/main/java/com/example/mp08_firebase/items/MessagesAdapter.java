package com.example.mp08_firebase.items;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mp08_firebase.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<Message> messages;

    private static final int MESSAGE_TYPE_SENT = 0;
    private static final int MESSAGE_TYPE_RECEIVED = 1;
    private static final int MESSAGE_TYPE_ASTRA = 2;

    public MessagesAdapter() {
        this.messages = new ArrayList<>();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public List<Message> getMessages() {
        return messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sended, parent, false);
        } else if (viewType == MESSAGE_TYPE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_astra, parent, false);
        }
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return MESSAGE_TYPE_SENT;
        } else {
            if (message.getSenderId().equals("astra")){
                return MESSAGE_TYPE_ASTRA;
            }
            return MESSAGE_TYPE_RECEIVED;
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
        }

        public void bind(Message message) {
            messageTextView.setText(message.getContent());
        }
    }
}