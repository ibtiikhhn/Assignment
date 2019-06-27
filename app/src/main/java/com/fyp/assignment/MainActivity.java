package com.fyp.assignment;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    //declare views
    Button showDialogBT;
    String linkk;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        showDialogBT = findViewById(R.id.enterURLBT);
        progressBar = findViewById(R.id.progressBar);

        //click listener for button
        showDialogBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }
    private void openDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enter a link to download");
        builder.setTitle("Add Download");
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.download_link_dialog, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText downloadLinkET = (EditText)dialogView.findViewById(R.id.downloadLinkET);
                String link =  downloadLinkET.getText().toString();
                linkk = link;

                int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If do not grant write external storage permission.
                if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
                {
                    // Request user to grant write external storage permission.
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
                }else {
                    //downloads the file
                    new DownloadFileFromURL().execute(link);
                }



            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar.setProgress(0);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(f_url[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("Server Error","Server returned HTTP  "+ connection.getResponseCode()
                            + " " +connection.getResponseMessage());
                    return null;
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                // download the file
                input = connection.getInputStream();
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                output = new FileOutputStream(path+"/myfile.pdf");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) {// only if total length is known
                        progressBar.setProgress((int) (total * 100/ fileLength));
                    }
                    output.write(data, 0, count);

                }
            } catch (Exception e) {
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressBar.setProgress(Integer.parseInt(progress[0]));
        }


        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            addNotification();
            Toast.makeText(MainActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "You grant write external storage permission. Please click original button again to continue.", Toast.LENGTH_LONG).show();
                new DownloadFileFromURL().execute(linkk);
            } else {
                Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }
}
