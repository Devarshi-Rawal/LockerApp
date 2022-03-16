package com.devarshi.lockerapp;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshi.data.DBConstants;
import com.devarshi.data.InfoRepository;
import com.devarshi.google.GoogleDriveActivity;
import com.devarshi.google.GoogleDriveApiDataRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    ConstraintLayout driveConstraintLayout, driveLoginConstraintLayout, backupNowConstraintLayout;
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
        Log.d(TAG, "onCreate: ListSize: " + itemsToBeBackedUpList.size());
    }

    private void findViews() {
        imageViewExit = findViewById(R.id.exitImageView);
        signInToDriveCv = findViewById(R.id.cardViewSignInToDrive);
        driveConstraintLayout = findViewById(R.id.constraintLayoutDrive);
        backupNowConstraintLayout = findViewById(R.id.constraintLayoutBackupNow);
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
        backupNowConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupNow();
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

    private void backupNow() {
        try {
            InfoRepository repos = new InfoRepository();

            for (String s : itemsToBeBackedUpList) {
                repos.writeInfo(s);
                Log.d(TAG, "backupNow: repos: " + repos.getInfo());
            }

            java.io.File db = new java.io.File(DBConstants.DB_LOCATION);

            if (repository == null) {
                showMessage(R.string.message_google_sign_in_failed);
                return;
            }

            repository.uploadFile(db, GOOGLE_DRIVE_DB_LOCATION)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            repository.queryFiles()
                                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                                        @Override
                                        public void onSuccess(FileList fileList) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
        catch (Exception e){
            e.getMessage();
        }

            /*repository.queryFiles()
                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                        @Override
                        public void onSuccess(FileList fileList) {

                            for (File file : fileList.getFiles()) {

                                Log.d(TAG, "onSuccess: file: " + file);

                                repository.uploadFile(db, String.valueOf(file))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                showMessage("Upload Success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showMessage("Error Uploading Backup");
                                                Log.e(TAG, "onFailure: Upload Exception: " + e.getMessage());
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        } catch (Exception e) {
            showMessage(e.getMessage());
            Log.e(TAG, "backupNow: exception: " + e.getMessage());
        }*/

        /*repository.queryFiles()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {

                        for (File file : fileList.getFiles()){
                            InfoRepos repos = new InfoRepos();
                            repos.writeInfo(String.valueOf(file));

                            repository.uploadFile(db, String.valueOf(file))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            showMessage("Upload success");
                                            Log.d(TAG, "actionOnClickOfUploadToDrive: Uploaded: " + file);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Not Uploaded: ", "error upload file " + e.getMessage());
                                            showMessage("Error upload");
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Not Uploaded: ", "error upload file " + e.getMessage());
                        showMessage("Error upload");
                    }
                });*/
    }
}