package com.infiam.firstbottomnav;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ImageIntent extends AppCompatActivity {

    private String chatId;
    private static final int GALLERY_PICK = 1;
    private Uri filePath;
    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_intent);

        chatId = getIntent().getStringExtra("chat_id");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        rootReference = FirebaseDatabase.getInstance().getReference();

        showGalleryIntent();

    }

    private void showGalleryIntent() {
        Intent gallery_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery_intent, GALLERY_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadImage(null); //UDF: uploads image to firebase.
        }
    }

    /*public void uploadThumb() {
        try {
            /*File thumb_FilePath = new File(filePath.getPath());
            Bitmap thumb_bitmap = new Compressor(getApplicationContext())
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(20)
                    .compressToBitmap(thumb_FilePath);//

            Bitmap thumb_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);

            //Bitmap thumb_bitmap = SiliCompressor.with(getApplicationContext()).getCompressBitmap(filePath.getPath());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
            byte[] thumb_byte = baos.toByteArray();

            StorageReference thumbPath = mStorageReference.child("chat_images").child(chatId).child("thumbs").child(System.currentTimeMillis() + ".jpg");

            UploadTask myUploadTask = thumbPath.putBytes(thumb_byte);

            myUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        String thumb_downloadUrl = task.getResult().getDownloadUrl().toString();
                        uploadImage(thumb_downloadUrl);
                    }

                }
            });
        }
        catch (IOException e) {
            Log.d("thumb_error",e.getMessage());
            uploadImage(null);
        }
    }*/

    private void uploadImage(final String thumb_url) {
        if (filePath != null) {
            Toast.makeText(getApplicationContext(), "Sending", Toast.LENGTH_LONG).show();

            //Firebase path for image
            StorageReference imagePath = mStorageReference.child("chat_images").child(chatId).child(System.currentTimeMillis() + ".jpg");

            imagePath.putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            //onComplete for image storing to database.
                            if (task.isSuccessful()) {
                                final String download_url = task.getResult().getDownloadUrl().toString();
                                sendMessage(download_url,thumb_url);

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Failed to upload to storage", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        }
    }

    public void sendMessage(String download_url,String thumb_url){

            Map messageMap = new HashMap();
            messageMap.put("message",download_url);
            messageMap.put("seen",false);
            messageMap.put("type","image");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);
            messageMap.put("thumb",thumb_url);

            String current_user_ref = "messages/" + currentUserId + "/" + chatId;
            String chat_user_ref = "messages/" + chatId + "/" + currentUserId;

            DatabaseReference user_msg_push = rootReference.child("messages").child(currentUserId).child(chatId).push();
            String pushId = user_msg_push.getKey();

            Map putMessage = new HashMap();
            putMessage.put(current_user_ref + "/" + pushId ,messageMap);
            putMessage.put(chat_user_ref + "/" + pushId , messageMap);

            rootReference.updateChildren(putMessage).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        finish();
                    }
                    else{
                        Log.d("Message Error",task.toString());
                    }
                }
            });



    }

}
