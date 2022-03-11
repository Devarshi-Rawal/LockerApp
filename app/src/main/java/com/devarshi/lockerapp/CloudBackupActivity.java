package com.devarshi.lockerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CloudBackupActivity extends AppCompatActivity {


    //Layouts, Views, etc.
    ImageView imageViewExit;

    //Intents and Variables
    Intent intent;
    ArrayList<String> itemsToBeBackedUpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_backup);

        findViews();

        initViews();

        intent = getIntent();
        itemsToBeBackedUpList = (ArrayList<String>) intent.getSerializableExtra("imagePaths");
    }

    private void findViews() {
        imageViewExit = findViewById(R.id.exitImageView);
    }

    private void initViews() {
        imageViewExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}