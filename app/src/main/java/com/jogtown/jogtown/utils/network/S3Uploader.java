package com.jogtown.jogtown.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.secrets.AWSKeys;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class S3Uploader {
    //Uploads Images (mostly) to AWS S3.

    Context context;
    CognitoCachingCredentialsProvider credentialsProvider = null;
    s3UploadInterface delegate;
    String directory;
    S3Uploader instance;

    public S3Uploader(Context context, String directory, s3UploadInterface s3UploadInterface) {
        this.context = context;
        this.delegate = s3UploadInterface;
        this.directory = directory;
        instance = this;
    }


    public void upload(String filePath) {

        File file = new File(filePath);

        String fileType = this.context.getContentResolver().getType(Uri.parse(filePath));

        Date date = new Date();
        String uniqueNum = Long.toString(date.getTime()); //just wants something unique
        final String imageName = uniqueNum + "-" + file.getName(); //Needs to be unique

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(fileType);

        TransferUtility transferUtility = getTransferUtility();

        TransferObserver transferObserver = transferUtility.upload(AWSKeys.BUCKET, this.directory + imageName, file);

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    AmazonS3Client s3Client = new AmazonS3Client(credentials());
                    String uploadedImageUrl = String.valueOf(s3Client.getUrl(AWSKeys.BUCKET, instance.directory+imageName));

                    delegate.onUploadSuccess("success " + uploadedImageUrl);
                } else if (state == TransferState.FAILED) {
                    delegate.onUploadError("failed");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                delegate.onUploadError("error");
            }
        });

    }


    private TransferUtility getTransferUtility() {
        AmazonS3 s3Client = new AmazonS3Client(credentials());
        return new TransferUtility(s3Client, this.context);

    }

    private CognitoCachingCredentialsProvider credentials() {
        if (this.credentialsProvider == null) {
            this.credentialsProvider = new CognitoCachingCredentialsProvider(
                    this.context, AWSKeys.COGNITO_POOL_ID, AWSKeys.REGION);
            return this.credentialsProvider;
        } else {
            return this.credentialsProvider;
        }
    }



    public interface s3UploadInterface {
        public void onUploadSuccess(String response);

        public void onUploadError(String response);
    }

}
