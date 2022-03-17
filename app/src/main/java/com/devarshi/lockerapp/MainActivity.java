package com.devarshi.lockerapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.devarshi.adapter.HiddenItemsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //PERMISSION_CODES & Static Variables
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int FILE_SELECT_CODE = 101;
    public static final int DELETE_REQUEST_CODE = 102;

    //Buttons, Views, Layouts, etc.
    private FloatingActionButton fAbSelectPhotos, fAbCloudBackup;
    public RecyclerView recyclerViewHi;
    public HiddenItemsAdapter imageRVAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    //Variables
    public String filename;
    public ArrayList<String> imagePaths;
    Uri uriId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        initViews();
    }

    private void findViews() {

        fAbSelectPhotos = findViewById(R.id.selectPhotosFab);
        fAbCloudBackup = findViewById(R.id.backupFab);
        recyclerViewHi = findViewById(R.id.hiddenItemsRv);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
    }

    @SuppressLint("RestrictedApi")
    private void initViews() {

        fAbSelectPhotos.setOnClickListener(v -> browseClick());

        fAbCloudBackup.setOnClickListener(v -> viewBackupActivity());

        imagePaths = new ArrayList<>();

        if (imagePaths == null) {
            recyclerViewHi.setVisibility(View.INVISIBLE);
        }

        loadRecyclerView();

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            loadRecyclerView();
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void viewBackupActivity() {

        Intent intent = new Intent(this, CloudBackupActivity.class);
        intent.putExtra("imagePaths", imagePaths);
        startActivity(intent);
    }

    public void loadRecyclerView() {

        imagePaths.clear();
        imageRVAdapter = new HiddenItemsAdapter(MainActivity.this, imagePaths);

        if (imagePaths != null) {
            File folder = new File(Environment.getExternalStorageDirectory(), "Android/data/com.devarshi.lockerapp/files");
            if (folder.exists()) {
                for (File file : folder.listFiles()) {
                    String fname = file.getPath();
                    if (imageRVAdapter.getItemCount() != 0) {
                        imagePaths.add(imageRVAdapter.getItemCount(), fname);
                    } else {
                        imagePaths.add(0, fname);
                    }
                }
            }
        }
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);
        recyclerViewHi.setLayoutManager(manager);
        recyclerViewHi.setAdapter(imageRVAdapter);

    }

    void requestDeletePermission(List<Uri> uriList) {
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            pendingIntent = MediaStore.createDeleteRequest(this.getContentResolver(), uriList);
        }
        try {
            this.startIntentSenderForResult(pendingIntent.getIntentSender(), 10, null, 0, 0,
                    0, null);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public void browseClick() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (Exception ex) {
            System.out.println("browseClick :" + ex);
        }
    }

    @Override
    protected void onStart() {
        askStoragePermission();
        super.onStart();
    }

    private void askStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
//                askStoragePermission();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && null != data) {
                Uri returnUri = data.getData();
                Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                String destinationPath = new File(getExternalFilesDir(null), filename).getAbsolutePath();
                moveFile(data.getData(), destinationPath, this);

            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: Exception: " + e);
        }

    }


    //Do not delete comments of this method.
    private void moveFile(Uri uri, String outputPath, Context context) {

        InputStream in = null;
        OutputStream out = null;
        try {

            in = context.getContentResolver().openInputStream(uri);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
            // delete the original file
            /*String filePath = getPath(context, uri);
            Log.d(TAG, "moveFile: filepath: " + new File(filePath).getParent());*/

            /*if (filePath != null) {
                uriId = getContentUriId(uri);
            }*/
            String filePath = getPath(context, uri);
            /*if (filePath != null) {
                Log.d(TAG, "moveFile: FilePath: " + new File(filePath).getParent());
            }*/

            if (filePath != null) {
                uriId = getContentUriId(Uri.parse(filePath));
            }

            Log.d(TAG, "moveFile: UriId: " + uriId);
            try {
                if (filePath != null) {
                    deleteAPI28(uriId, context);
                } else {
                    deleteAPI28(uri, context);
                }
            } catch (Exception e) {
                Log.e(TAG, "moveFile: File not deleted " + e.getMessage());
                try {
                    if (filePath != null) {
                        deleteAPI30(uriId);
                    } else {
                        deleteAPI30(uri);
                    }
                } catch (IntentSender.SendIntentException e1) {
                    Log.e(TAG, "moveFile: File not deleted " + e1.getMessage());
                }
            }

        } catch (Exception fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
    }

    private void deleteAPI30(Uri imageUri) throws IntentSender.SendIntentException {
        ContentResolver contentResolver = this.getContentResolver();
        // API 30

        List<Uri> uriList = new ArrayList<>();
        Collections.addAll(uriList, imageUri);
        PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uriList);
        this.startIntentSenderForResult(pendingIntent.getIntentSender(),
                DELETE_REQUEST_CODE, null, 0,
                0, 0, null);

    }

    private Uri getContentUriId(Uri imageUri) {
        String[] projections = {MediaStore.MediaColumns._ID};
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projections,
                MediaStore.MediaColumns.DATA + "=?",
                new String[]{imageUri.getPath()}, null);
        long id = 0;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            }
        }
        cursor.close();
        return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf((int) id));
    }

    public static int deleteAPI28(Uri uri, Context context) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.delete(uri, null, null);
    }
}