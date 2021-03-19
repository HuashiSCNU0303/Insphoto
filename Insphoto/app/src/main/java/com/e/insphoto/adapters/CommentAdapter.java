package com.e.insphoto.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.e.insphoto.R;
import com.e.insphoto.entities.Comment;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends XRecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> mComments;
    private Context mContext;
    OnRecyclerViewItemClickListener mItemClickListener = null;

    public CommentAdapter(Context context, List<Comment> comments) {
        mContext = context;
        mComments = comments;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.commentsitem, parent, false);
        CommentAdapter.ViewHolder holder = new CommentAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, final int position) {
        Comment comment = mComments.get(position);
        String userProfile = comment.getUser().getProfileImgPath();
        Glide.with(mContext).load(userProfile).centerCrop().override(50, 50).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.imageView);
        Log.e("到达",position+"_");
        holder.userName.setText(comment.getUser().getProfileName());
        holder.commentText.setText(comment.getComment());
        holder.commentTime.setText(comment.getTime());
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
    }

    public void resetData(List<Comment> newData) {
        mComments = new ArrayList<>();
        mComments.addAll(newData);
        notifyDataSetChanged();
    }

    public void addDataToFront(List<Comment> newData) {
        mComments.addAll(0, newData);
        for(int i=0;i<newData.size();i++) {
            notifyItemInserted(i);
        }
        notifyItemRangeChanged(newData.size(), mComments.size());
    }

    public void addDataToBack(List<Comment> newData) {
        for (int i=0;i<newData.size();i++) {
            mComments.add(newData.get(i));
            notifyItemInserted(getItemCount());
            notifyItemChanged(getItemCount());
        }
    }

    public Comment getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(CommentAdapter.OnRecyclerViewItemClickListener listener) {
        this.mItemClickListener = listener;
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView userName, commentText, commentTime;
        public ViewHolder (View view)
        {
            super(view);
            imageView = view.findViewById(R.id.commentprofile);
            userName = view.findViewById(R.id.commentname);
            commentText = view.findViewById(R.id.commenttext);
            commentTime = view.findViewById(R.id.commenttime);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        int position = getAdapterPosition() - 2;
                        if (position != RecyclerView.NO_POSITION) {
                            mItemClickListener.onItemClick(view, position);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null) {
                        int position = getAdapterPosition() - 2;
                        mItemClickListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }
}
