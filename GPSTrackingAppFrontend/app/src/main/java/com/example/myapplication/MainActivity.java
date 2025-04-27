package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_GPX_FILE_REQUEST = 1;

    TextView label;

    TextView label2;
    Handler handler;

    Button menuButton;

    Button button2;


    public static String results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button2 = findViewById(R.id.button2);

        final MediaPlayer mp = MediaPlayer.create(this,R.raw.click);
        button2.setOnClickListener(v -> {
            mp.start();
            Intent intent = new Intent(MainActivity.this, MainActivity3.class);
            startActivity(intent);
        });

        menuButton = findViewById(R.id.menuButton);

        menuButton.setOnClickListener(v -> {
            mp.start();
            openFilePicker();
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                String result = message.getData().getString("result");
                label.setText(result);
                return true;
            }
        });

        label = findViewById(R.id.label);

    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set the MIME type to select all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_GPX_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_GPX_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    String cacheFilePath = saveFileToCache(uri);
                    if (cacheFilePath != null) {
                        File cacheFile = new File(cacheFilePath);
                        Log.e("cache_file_path", cacheFile.getPath());
                        // Perform your desired operations with the cache file
                        // ...
                        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);

                        loadingDialog.startLoadingDialog();
                        MyThread t1 = new MyThread(cacheFilePath, handler, label, label2, getApplicationContext());
                        t1.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String saveFileToCache(Uri uri) throws IOException {
        ContentResolver contentResolver = getContentResolver();

        // Obtain the file name from the Uri
        String fileName = getFileNameFromUri(uri);

        // Create a file in the app's cache directory
        File cacheDir = getCacheDir();
        File cacheFile = new File(cacheDir, fileName);

        // Delete any existing cache file with the same name
        if (cacheFile.exists()) {
            cacheFile.delete();
        }

        // Create a new cache file
        cacheFile.createNewFile();

        // Use OpenFileDescriptor to copy the file data to the cache file
        FileInputStream inputStream = (FileInputStream) contentResolver.openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(cacheFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // Close the streams
        inputStream.close();
        outputStream.close();

        return cacheFile.getAbsolutePath();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }
}
