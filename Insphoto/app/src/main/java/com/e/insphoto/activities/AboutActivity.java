package com.e.insphoto.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.e.insphoto.R;

public class AboutActivity extends MaterialAboutActivity {

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();

        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text("insPhoto")
                .desc("© 2020 软件工程概论小组")
                .icon(R.drawable.ic_launcher_round)
                .build());

        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(this,
                new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_information_outline)
                        .sizeDp(18),
                "版本",
                false));

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title("作者");

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("叶俊豪 胡媚凤 蒋沁月 原野 冯谦 曾宪泽")
                .subText("武汉大学软件工程系")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .sizeDp(18))
                .build());

        return new MaterialAboutList.Builder()
                .addCard(appCardBuilder.build())
                .addCard(authorCardBuilder.build())
                .build();
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return "关于";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_about);
    }
}
