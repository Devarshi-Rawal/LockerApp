package com.devarshi.google;

import android.content.Context;
import android.content.Intent;

import com.devarshi.app.AppActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


public abstract class GoogleSignInActivity extends AppActivity {

    private static final int GOOGLE_SIGN_IN_REQUEST = 1010;
    public static final int GOOGLE_SIGN_OUT_REQUEST = 1011;

    protected abstract GoogleSignInOptions getGoogleSignInOptions();

    protected abstract void onGoogleSignedInSuccess(final GoogleSignInAccount signInAccount);

    protected abstract void onGoogleSignedInFailed(final ApiException exception);

//    protected abstract void onGoogleSignedOutSuccess(GoogleSignInAccount account);

    protected void startGoogleSignIn() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, getGoogleSignInOptions());
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
    }

    protected void startGoogleSignOut() {
        GoogleSignInClient googleSignOutClient = GoogleSignIn.getClient(this, getGoogleSignInOptions());
        Intent signOutIntent = googleSignOutClient.getSignInIntent();
        startActivityForResult(signOutIntent,GOOGLE_SIGN_OUT_REQUEST);
        googleSignOutClient.signOut();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        /*if (requestCode == GOOGLE_SIGN_OUT_REQUEST){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignOutResult(task);
        }*/
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            onGoogleSignedInSuccess(account);
        } catch (ApiException e) {
            onGoogleSignedInFailed(e);
        }
    }

    /*private void handleSignOutResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            onGoogleSignedOutSuccess(account);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }*/
}
