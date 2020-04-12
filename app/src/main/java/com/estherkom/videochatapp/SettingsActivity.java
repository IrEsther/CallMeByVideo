package com.estherkom.videochatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
private Button saveBtn;
private EditText userName, userBio;
private ImageView profileImageV;
private static int GalleryPick = 1;
private Uri imageUri;
private StorageReference userProfileImgRef;
private String downLoadUrl;
private DatabaseReference userRef;
private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        setContentView(R.layout.activity_settings);
        saveBtn = findViewById(R.id.save_settings_btn);
        userName = findViewById(R.id.user_settings);
        userBio = findViewById(R.id.bio_settings);
        profileImageV = findViewById(R.id.settings_profile_image);
        progressDialog =  new ProgressDialog(this);

        profileImageV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        saveUserData();
    }
});
        retrieveUserInfo();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
if (requestCode==GalleryPick&&resultCode==RESULT_OK&& data!=null){
    imageUri = data.getData();
    profileImageV.setImageURI(imageUri);
}



    }

    private void saveUserData() {
        final String getUserName = userName.getText().toString();
        final String getUserStatus = userBio.getText().toString();

        if (imageUri == null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                        saveInputOnlyWithoutImage();
                    }else{
                        Toast.makeText(SettingsActivity.this,"Please select image first",Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (getUserName.equals("")) {
            Toast.makeText(this,"userName is mandatory",Toast.LENGTH_SHORT).show();

        }else if (getUserStatus.equals("")) {
            Toast.makeText(this,"bio is mandatory",Toast.LENGTH_SHORT).show();

        }else {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait");
            progressDialog.show();
            final StorageReference filePath = userProfileImgRef
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                  if (!task.isSuccessful()){
                      throw task.getException();
                  }

                  downLoadUrl = filePath.getDownloadUrl().toString();

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downLoadUrl=task.getResult().toString();

                        HashMap<String,Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name",getUserName);
                        profileMap.put("status",getUserStatus);
                        profileMap.put("image",downLoadUrl);



                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                               startActivity(intent);
                               finish();
                               progressDialog.dismiss();

                               Toast.makeText(SettingsActivity.this,"Profile settings has been updated! Congratulation!",Toast.LENGTH_SHORT).show();
                           }


                            }
                        });



                    }
                }
            });
        }
    }

    private void saveInputOnlyWithoutImage() {

        final String getUserName = userName.getText().toString();
        final String getUserStatus = userBio.getText().toString();


        if (getUserName.equals("")) {
            Toast.makeText(this,"userName is mandatory",Toast.LENGTH_SHORT).show();

        }else if (getUserStatus.equals("")) {
            Toast.makeText(this,"bio is mandatory",Toast.LENGTH_SHORT).show();

        }else{
progressDialog.setTitle("Account Settings");
progressDialog.setMessage("Please wait");
progressDialog.show();
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name",getUserName);
            profileMap.put("status",getUserStatus);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser()
                    .getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,"Profile settings has been updated! Congratulation!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }



    }




    private void retrieveUserInfo(){
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   String imageDb = dataSnapshot.child("image").getValue().toString();
                   String nameDb = dataSnapshot.child("name").getValue().toString();
                   String bioDb = dataSnapshot.child("status").getValue().toString();
userName.setText(nameDb);
userBio.setText(bioDb);
                   Picasso.get().load(imageDb).placeholder(R.drawable.profi).into(profileImageV);
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}