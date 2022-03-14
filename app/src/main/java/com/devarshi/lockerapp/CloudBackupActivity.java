package com.devarshi.lockerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshi.google.GoogleDriveActivity;
import com.devarshi.google.GoogleDriveApiDataRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.api.services.drive.Drive;

import java.util.ArrayList;

public class CloudBackupActivity extends GoogleDriveActivity {


    //Layouts, Views, etc.
    ImageView imageViewExit;
    CardView signInToDriveCv;
    ConstraintLayout driveConstraintLayout;
    ConstraintLayout driveLoginConstraintLayout;
    TextView logOutTextView, loggedInEmailTextView;

    //Intents and Variables
    Intent intent;
    ArrayList<String> itemsToBeBackedUpList;
    public GoogleDriveApiDataRepository repository;
    GoogleSignInAccount signInA;
    Drive driveService;

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
        signInToDriveCv = findViewById(R.id.cardViewSignInToDrive);
        driveConstraintLayout = findViewById(R.id.constraintLayoutDrive);
        driveLoginConstraintLayout = findViewById(R.id.constraintLayoutDriveLogin);
        logOutTextView = findViewById(R.id.textViewLogOut);
        loggedInEmailTextView = findViewById(R.id.textViewLoggedInEmail);
    }

    private void initViews() {
        driveConstraintLayout.setVisibility(View.INVISIBLE);
        driveLoginConstraintLayout.setVisibility(View.VISIBLE);
        imageViewExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        signInToDriveCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleDriveSignIn();
            }
        });

        logOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleDriveSignOut();
            }
        });
    }

    @Override
    protected void onGoogleDriveSignedInSuccess(Drive driveApi, GoogleSignInAccount signInAccount) {
        repository = new GoogleDriveApiDataRepository(driveApi);
        driveService = driveApi;
        signInA = signInAccount;
        loggedInEmailTextView.setText(signInAccount.getEmail());

        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        driveConstraintLayout.setVisibility(View.VISIBLE);
        driveLoginConstraintLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onGoogleDriveSignedInFailed(ApiException exception) {
        Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onGoogleDriveSignedOutSuccess(GoogleSignInAccount signOutAccount) {
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        driveConstraintLayout.setVisibility(View.INVISIBLE);
        driveLoginConstraintLayout.setVisibility(View.VISIBLE);
    }
}