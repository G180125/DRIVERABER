package com.example.driveraber.Adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserViewHolder>{
    private List<User> userList;
    private UserChatAdapter.RecyclerViewClickListener mListener;
    private FirebaseManager firebaseManager;

    public UserChatAdapter(List<User> userList, UserChatAdapter.RecyclerViewClickListener listener){
        this.userList = userList;
        this.mListener = listener;
        firebaseManager = new FirebaseManager();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserChatAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_container, parent, false);
        return new UserChatAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserChatAdapter.UserViewHolder holder, @SuppressLint("RecyclerView") int position) {
        User user = userList.get(position);
        if(user.getAvatar().isEmpty()){
            holder.bind(user, position, null);
            return;
        }
        firebaseManager.retrieveImage(user.getAvatar(), new FirebaseManager.OnRetrieveImageListener() {
            @Override
            public void onRetrieveImageSuccess(Bitmap bitmap) {
                holder.bind(user, position, bitmap);
            }

            @Override
            public void onRetrieveImageFailure(String message) {
                holder.bind(user, position, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView avatar;
        TextView nameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            nameTextView = itemView.findViewById(R.id.name);

            // Set click listener on the itemView
            itemView.setOnClickListener(this);
        }

        public void bind(User user, int position, Bitmap bitmap) {
            if(bitmap != null) {
                avatar.setImageBitmap(bitmap);
            }
            nameTextView.setText(user.getName());
        }

        @Override
        public void onClick(View v) {
            mListener.onCardClick(getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onCardClick(int position);
    }
}

