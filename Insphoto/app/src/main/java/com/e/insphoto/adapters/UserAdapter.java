package com.e.insphoto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.e.insphoto.R;
import com.e.insphoto.entities.User;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

public class UserAdapter extends XRecyclerView.Adapter<UserAdapter.ViewHolder>{
    private List<User> mUsers;
    private Context mContext;
    UserAdapter.OnRecyclerViewItemClickListener mItemClickListener;

    public UserAdapter(Context context, List<User> users) {
        mContext = context;
        mUsers = users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.useritem, parent, false);
        UserAdapter.ViewHolder holder = new UserAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ViewHolder holder, final int position) {
        String userName = mUsers.get(position).getProfileName();
        String userImg = mUsers.get(position).getProfileImgPath();
        String userDescription = mUsers.get(position).getDescription();
        Glide.with(mContext).load(userImg).override(200, 200).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.userProfileImg);
        holder.userProfileName.setText(userName);
        holder.userDescription.setText(userDescription);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public User getItem(int position) {
        return mUsers.get(position);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void addDataToBack(List<User> newData) {
        for (int i=0;i<newData.size();i++) {
            mUsers.add(newData.get(i));
            notifyItemInserted(getItemCount());
            notifyItemChanged(getItemCount());
        }
    }

    public void setOnItemClickListener(UserAdapter.OnRecyclerViewItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImg;
        TextView userProfileName;
        TextView userDescription;

        public ViewHolder (View view)
        {
            super(view);
            userProfileImg = view.findViewById(R.id.userProfileImg);
            userProfileName = view.findViewById(R.id.userProfileName);
            userDescription = view.findViewById(R.id.userDescri);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        int position = getAdapterPosition() - 1;
                        if (position != RecyclerView.NO_POSITION) {
                            mItemClickListener.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }
}
