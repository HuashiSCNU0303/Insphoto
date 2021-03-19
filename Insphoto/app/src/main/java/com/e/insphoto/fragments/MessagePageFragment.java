package com.e.insphoto.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.e.insphoto.R;
import com.e.insphoto.activities.ChatActivity;
import com.e.insphoto.entities.RecentMessage;
import com.e.insphoto.entities.RecentMessagePool;
import com.e.insphoto.entities.User;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class MessagePageFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<RecentMessage>,
        DialogsListAdapter.OnDialogLongClickListener<RecentMessage> {

    private ImageLoader imageLoader;
    private DialogsListAdapter<RecentMessage> recentMessageAdapter;
    private DialogsList recentMessageList;

    private DialogUpdateReceiver dialogUpdateReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.framelayout_messagepage, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Glide.with(getContext()).load(url).into(imageView);
            }
        };
        recentMessageList = getActivity().findViewById(R.id.recentMessageList);
        initAdapter();

        dialogUpdateReceiver = new DialogUpdateReceiver();
        IntentFilter filter = new IntentFilter("UPDATE_DIALOG_LIST");
        getActivity().registerReceiver(dialogUpdateReceiver, filter);

        loadMessages();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("MessagePageFragment", "重新展示");
        // loadMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(dialogUpdateReceiver);
    }

    @Override
    public void onDialogClick(RecentMessage dialog) {
        Intent intent = new Intent();
        intent.setClass(getContext(), ChatActivity.class);
        intent.putExtra("receiver", Parcels.wrap(User.class, dialog.getUsers().get(0)));
        startActivity(intent);
    }

    @Override
    public void onDialogLongClick(RecentMessage dialog) {

    }

    private void initAdapter() {
        recentMessageAdapter = new DialogsListAdapter<>(imageLoader);
        List<RecentMessage> messages = new ArrayList<>();
        recentMessageAdapter.setItems(messages);

        recentMessageAdapter.setOnDialogClickListener(this);
        recentMessageAdapter.setOnDialogLongClickListener(this);

        recentMessageList.setAdapter(recentMessageAdapter);
    }

    private void loadMessages() {
        Log.e("loadMessages()", "修改的有"+RecentMessagePool.modifiedMessages.size());
        for (RecentMessage recentMessage_: RecentMessagePool.modifiedMessages) {
            RecentMessage recentMessage = recentMessageAdapter.getItemById(recentMessage_.getId());
            // 如果没有的话就把旧的插进去
            if (recentMessage == null) {
                recentMessage = recentMessage_;
                recentMessageAdapter.addItem(recentMessage);
            }
            recentMessage.setUnreadCount(recentMessage_.getUnreadCount());
            recentMessageAdapter.updateDialogWithMessage(recentMessage.getId(), recentMessage_.getLastMessage());
        }
        RecentMessagePool.modifiedMessages.clear();
    }

    private class DialogUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DialogUpdateReceiver", "接收到广播");
            loadMessages();
        }
    }
}
