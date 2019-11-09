/* This retrieves the number,name,email,gender,status,image,thumb_image value from the FireBase RealTime database.*/
package com.infiam.firstbottomnav;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import account.more.NewStatus;
import auth.pkg.RegisterActivity;
import auth.pkg.RegisterDetailsActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String current_uid;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private TextView profile_name;
    private TextView profile_status;
    private CircleImageView profile_pic;
    private static final int GALLERY_PICK = 1;
    private Uri filePath;
    private StorageReference mStorageReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile_name = view.findViewById(R.id.profile_name);
        profile_status = view.findViewById(R.id.profile_status);
        profile_pic = view.findViewById(R.id.profile_image);
        ImageButton changeStatus = view.findViewById(R.id.change_status_btn);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);  //Firebase Offline persistence.
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                //String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                profile_name.setText(name);
                profile_status.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                            .into(profile_pic, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_image).into(profile_pic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cur_status = profile_status.getText().toString();

                Intent new_status_intent = new Intent(getActivity(), NewStatus.class);
                new_status_intent.putExtra("status", cur_status);
                startActivity(new_status_intent);
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*include bottomSheet: show and change.*/

                CharSequence options[] = new CharSequence[]{"View Image","Change Profile Image"};
                AlertDialog.Builder imageBuilder = new AlertDialog.Builder(getActivity());
                //imageBuilder.setTitle("Select");
                imageBuilder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                break;
                            case 1:
                                showGalleryIntent();   //UDF: opens gallery to pick image.
                                break;
                        }
                    }
                });
                imageBuilder.show();
            }
        });
        return view;
    }

    //UDF: opens gallery to pick image.
    private void showGalleryIntent() {

        Intent gallery_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery_intent, GALLERY_PICK);

        /*Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),GALLERY_PICK);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            //Toast.makeText(getActivity(),"Image selected "+filePath.toString(),Toast.LENGTH_LONG).show();
            uploadImage(); //UDF: uploads image to firebase.
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            Toast.makeText(getActivity(), "Uploading", Toast.LENGTH_LONG).show();

            final ProgressDialog mProgress = new ProgressDialog(getActivity());
            mProgress.setMessage("Uploading your picture");
            mProgress.setCancelable(false);
            mProgress.show();

            //Firebase path for image
            StorageReference imagePath = mStorageReference.child("profile_images").child(profile_name.getText().toString() + current_uid + ".jpg");

            imagePath.putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            //onComplete for image storing to database.
                            if (task.isSuccessful()) {
                                final String download_url = task.getResult().getDownloadUrl().toString();

                                mUserDatabase.child("image").setValue(download_url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //onComplete for setting value RTDB for image attr.
                                                if (task.isSuccessful()) {
                                                    uploadThumb(download_url);
                                                    Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_LONG).show();
                                                    mProgress.dismiss();
                                                } else {
                                                    mProgress.dismiss();
                                                    Toast.makeText(getActivity(), "Failed to update database", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(getActivity(), "Failed to upload to storage", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        }
    }

    public void uploadThumb(String download_url) {
        try {
            //creating thumbnail.
        File thumb_FilePath = new File(filePath.getPath());
        //Toast.makeText(getActivity(),filePath.toString(),Toast.LENGTH_LONG).show();
            Bitmap thumb_bitmap = new Compressor(getContext())
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(50)
                    .compressToBitmap(thumb_FilePath);


            //creating byte array for our thumbnail so to upload to firebase.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumb_byte = baos.toByteArray();

            //Firebase path for thumnail
            StorageReference thumbPath = mStorageReference.child("profile_images").child("thumbs").child(profile_name.getText().toString() + current_uid + ".jpg");

            UploadTask myUploadTask = thumbPath.putBytes(thumb_byte);

            myUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        String thumb_downloadUrl = task.getResult().getDownloadUrl().toString();

                        mUserDatabase.child("thumb_image").setValue(thumb_downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Thumb stored", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to store thumb to database", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "Failed to upload thumb to storage", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        catch (IOException e) {
            //Toast.makeText(getActivity(),"Exception: "+e.getMessage(),Toast.LENGTH_LONG).show();
            mUserDatabase.child("thumb_image").setValue(download_url)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Toast.makeText(getActivity(),"Thumb linked",Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getActivity(),"Thumb linked failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /*public void uploadThumb(){
        try {
            Bitmap thumb_bitmap = SiliCompressor.with(getActivity()).getCompressBitmap(filePath.getPath());

            //creating byte array for our thumbnail so to upload to firebase.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumb_byte = baos.toByteArray();

            //Firebase path for thumnail
            StorageReference thumbPath = mStorageReference.child("profile_images").child("thumbs").child(current_uid + ".jpg");

            UploadTask myUploadTask = thumbPath.putBytes(thumb_byte);

            myUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        String thumb_downloadUrl = task.getResult().getDownloadUrl().toString();

                        mUserDatabase.child("thumb_image").setValue(thumb_downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Thumb stored", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to store thumb to database", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "Failed to upload thumb to storage", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"Exception: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }*/


}
