package com.example.mp08_firebase;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private List<Chat> chats;
    private OnChatClickListener onChatClickListener;

    public ChatsAdapter(OnChatClickListener onChatClickListener) {
        this.chats = new ArrayList<>();
        this.onChatClickListener = onChatClickListener;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view, onChatClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat, holder.itemView.getContext());

        // TODO Poner imagen del usuario
        /*
         // Coger "avatar" del user con el chat.getUid
        Glide.with(holder.itemView.getContext())
                .load(chat.getOtherUserProfileImageUrl())
                .circleCrop()
                .into(holder.profile_image);
        */
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView profile_image;
        private TextView chatTitleTextView;
        private OnChatClickListener onChatClickListener;

        public ViewHolder(@NonNull View itemView, OnChatClickListener onChatClickListener) {
            super(itemView);
            chatTitleTextView = itemView.findViewById(R.id.chat_title);
            profile_image = itemView.findViewById(R.id.profile_image);
            this.onChatClickListener = onChatClickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(Chat chat, Context context) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            List<String> participants = chat.getUsers();
            String otherUserId = null;

            for (String participant : participants) {
                if (!participant.equals(currentUserId)) {
                    otherUserId = participant;
                    break;
                }
            }

            if (otherUserId != null) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                User otherUser = documentSnapshot.toObject(User.class);
                                if (otherUser != null) {
                                    chatTitleTextView.setText(otherUser.getUsername());

                                    if (otherUser.getAvatar() != null && !otherUser.getAvatar().isEmpty()) {
                                        //AQUÍ
                                        Glide.with(context)
                                                .load(otherUser.getAvatar())
                                                .into(profile_image);
                                    }
                                }
                            } else {
                                Log.e("ChatsAdapter", "User not found");
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ChatsAdapter", "Error loading user details", e));
            } else {
                chatTitleTextView.setText("usuario no encontrado");
            }
        }


        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onChatClickListener != null) {
                onChatClickListener.onChatClick(ChatsAdapter.this.chats.get(position));
            }
        }
    }
}