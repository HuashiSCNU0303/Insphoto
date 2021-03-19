package com.e.insphoto.adapters;

import android.content.Context;
import android.util.Pair;
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
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

public class MenuItemAdapter extends XRecyclerView.Adapter<MenuItemAdapter.ViewHolder> {
    private List<Pair<String, Integer>> mItems;
    private Context mContext;
    MenuItemAdapter.OnRecyclerViewItemClickListener mItemClickListener;

    public MenuItemAdapter(Context context, List<Pair<String, Integer>> items) {
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public MenuItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menuitem, parent, false);
        MenuItemAdapter.ViewHolder holder = new MenuItemAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MenuItemAdapter.ViewHolder holder, final int position) {
        String menuItemText = mItems.get(position).first;
        Integer menuItemIcon = mItems.get(position).second;
        Glide.with(mContext).load(menuItemIcon).override(200, 200).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.icon);
        Glide.with(mContext).load(R.drawable.rightarrow).override(200, 200).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.arrow);
        holder.text.setText(menuItemText);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onItemClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(MenuItemAdapter.OnRecyclerViewItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, arrow;
        TextView text;

        public ViewHolder (View view)
        {
            super(view);
            icon = view.findViewById(R.id.menuItemIcon);
            arrow = view.findViewById(R.id.menuItemArrow);
            text = view.findViewById(R.id.menuItemText);
        }
    }
}
