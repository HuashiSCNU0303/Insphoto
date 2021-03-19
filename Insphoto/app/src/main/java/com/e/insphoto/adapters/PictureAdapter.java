package com.e.insphoto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.e.insphoto.R;
import com.e.insphoto.entities.Picture;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureAdapter extends XRecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private List<Picture> mPictures;
    private Context mContext;
    OnRecyclerViewItemClickListener mItemClickListener;

    public PictureAdapter(Context context, List<Picture> pictures) {
        mContext = context;
        mPictures = pictures;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pictureitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String picture = mPictures.get(position).getImagePath();
        Glide.with(mContext).load(picture).centerCrop().override(mContext.getResources().getDisplayMetrics().widthPixels / 2, 400).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.imageView); // 加一个占位图和错误图即可，不需要对话框了
    }

    public void addDataToFront(List<Picture> newData) {
        for(int i=0;i<newData.size();i++) {
            mPictures.add(i, newData.get(i));
            notifyItemInserted(i);
            notifyItemChanged(i);
            notifyItemRangeChanged(i+1, getItemCount());
        }
    }

    public void addDataToFront(Picture newData) {
        mPictures.add(0, newData);
        notifyItemInserted(0);
        notifyItemChanged(0);
        notifyItemRangeChanged(0, getItemCount() - 1);
    }

    public void addDataToBack(List<Picture> newData) {
        for (int i=0;i<newData.size();i++) {
            mPictures.add(newData.get(i));
            notifyItemInserted(getItemCount());
            notifyItemChanged(getItemCount());
        }
    }

    @Override
    public int getItemCount() {
        return mPictures.size();
    }

    public Picture getItem(int position) {
        return mPictures.get(position);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder (View view)
        {
            super(view);
            imageView = view.findViewById(R.id.picture);

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
