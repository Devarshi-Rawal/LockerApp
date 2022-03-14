package com.devarshi.google;


import com.devarshi.lockerapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

public abstract class GoogleDriveActivity extends GoogleSignInActivity {

    public void startGoogleDriveSignIn() {
        startGoogleSignIn();
    }

    public void startGoogleDriveSignOut(){
        startGoogleSignOut();
    }

    protected abstract void onGoogleDriveSignedInSuccess(final Drive driveApi, final GoogleSignInAccount signInAccount);

    protected abstract void onGoogleDriveSignedInFailed(final ApiException exception);

    protected abstract void onGoogleDriveSignedOutSuccess(GoogleSignInAccount signOutAccount);

    @Override
    protected GoogleSignInOptions getGoogleSignInOptions() {
        Scope scopeDriveAppFolder = new Scope(Scopes.DRIVE_APPFOLDER);

//        Scope scopeDrive = new Scope(Scopes.DRIVE_FILE);
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(scopeDriveAppFolder)
                .build();
    }

    @Override
    protected void onGoogleSignedOutSuccess(final GoogleSignInAccount account) {
        initializeDriveClientForLogOut();
    }

    @Override
    protected void onGoogleSignedInSuccess(final GoogleSignInAccount signInAccount) {
        initializeDriveClient(signInAccount);
    }

    @Override
    protected void onGoogleSignedInFailed(final ApiException exception) {
        onGoogleDriveSignedInFailed(exception);
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        List<String> scopes = new ArrayList<>();
        scopes.add(DriveScopes.DRIVE_APPDATA);

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, scopes);
        credential.setSelectedAccount(signInAccount.getAccount());
        Drive.Builder builder = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential
        );

        String appName = String.valueOf(R.string.app_name);
        Drive driveApi = builder
                .setApplicationName(appName)
                .build();
        onGoogleDriveSignedInSuccess(driveApi,signInAccount);
    }

    private void initializeDriveClientForLogOut(){
        List<String> scopes = new ArrayList<>();
        scopes.add(DriveScopes.DRIVE_APPDATA);
        GoogleSignInAccount signOutAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signOutAccount != null && signOutAccount.getGrantedScopes().containsAll(scopes)){
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,getGoogleSignInOptions());
            googleSignInClient.signOut();
        }
        onGoogleDriveSignedOutSuccess(signOutAccount);
    }
}
