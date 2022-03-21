package com.devarshi.lockerapp;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshi.data.DBConstants;
import com.devarshi.data.InfoRepository;
import com.devarshi.google.GoogleDriveActivity;
import com.devarshi.google.GoogleDriveApiDataRepository;
import com.devarshi.model.ImageModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;

public class CloudBackupActivity extends GoogleDriveActivity {

    //Static variables and constants
    private static final String TAG = "CloudBackupActivity";
    private static final String GOOGLE_DRIVE_DB_LOCATION = "db";


    //Layouts, Views, etc.
    ImageView imageViewExit;
    CardView signInToDriveCv;
    LinearLayout containerLinearLayout;
    ConstraintLayout driveLoginLinearLayout;
    TextView logOutButton, loggedInEmailTextView;
    MaterialButton restoreButton,backupNowButton;

    //Intents and Variables
    Intent intent;
    ArrayList<String> itemsToBeBackedUpList;
    public GoogleDriveApiDataRepository repository;
    GoogleSignInAccount signInA;
    Drive driveService;

    ArrayList<ImageModel> writeToDbList;

    ImageModel imageModel = new ImageModel();
    private String fName;

    boolean isOnSuccessTrue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_backup);

        findViews();

        initViews();

        intent = getIntent();
        itemsToBeBackedUpList = (ArrayList<String>) intent.getSerializableExtra("imagePaths");
        Log.d(TAG, "onCreate: ListSize: " + itemsToBeBackedUpList.size());
    }

    private void findViews() {
        imageViewExit = findViewById(R.id.exitImageView);
        signInToDriveCv = findViewById(R.id.cardViewSignInToDrive);
        containerLinearLayout = findViewById(R.id.linearLayoutContainer);
        backupNowButton = findViewById(R.id.buttonBackUpNow);
        restoreButton = findViewById(R.id.buttonRestore);
        driveLoginLinearLayout = findViewById(R.id.constraintLayoutDriveLogin);
        logOutButton = findViewById(R.id.buttonLogOut);
        loggedInEmailTextView = findViewById(R.id.textViewLoggedInEmail);
    }

    private void initViews() {

        writeToDbList = new ArrayList<>();

        containerLinearLayout.setVisibility(View.INVISIBLE);
        driveLoginLinearLayout.setVisibility(View.VISIBLE);
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

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleDriveSignOut();
            }
        });
        backupNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupNow();
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
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
        containerLinearLayout.setVisibility(View.VISIBLE);
        driveLoginLinearLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onGoogleDriveSignedInFailed(ApiException exception) {
        Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onGoogleDriveSignedOutSuccess(GoogleSignInAccount signOutAccount) {
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        containerLinearLayout.setVisibility(View.INVISIBLE);
        driveLoginLinearLayout.setVisibility(View.VISIBLE);
    }

    private void backupNow() {
        InfoRepository repos = new InfoRepository();

        java.io.File db = new java.io.File(DBConstants.DB_LOCATION);
        imageModel.setDb_file(db);

        for (String s : itemsToBeBackedUpList) {
            repos.writeInfo(s);
            imageModel.setFile(new java.io.File(s));
            writeToDbList.add(imageModel);
            Log.d(TAG, "backupNow: repos: " + repos.getInfo());
            Log.d(TAG, "backupNow: The writeToDbList element is: " + imageModel.getFile());

            if (repository == null) {
                showMessage(R.string.message_google_sign_in_failed);
                return;
            }

            repository.uploadFile(imageModel.getDb_file(), String.valueOf(imageModel.getFile()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            isOnSuccessTrue = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            isOnSuccessTrue = false;
                        }
                    });
        }

        if (isOnSuccessTrue){
            showMessage("Upload Successful");
        }
        else {
            showMessage("Upload Unsuccessful");
        }
    }

    private void restore(){

        if (repository == null) {
            showMessage(R.string.message_google_sign_in_failed);
            return;
        }

        repository.queryFiles()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {

                        for (File file : fileList.getFiles()){
                            String reInfo = file.getName();
                            fName = reInfo.substring(reInfo.lastIndexOf("/") + 1);

                            Log.d(TAG, "onSuccess: FileName: " + fName);
                            Log.d(TAG, "onSuccess: FileIds: " + file.getId());
                        }
                        showMessage("Restore Successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Restore Unsuccessful");
                    }
                });
    }
}