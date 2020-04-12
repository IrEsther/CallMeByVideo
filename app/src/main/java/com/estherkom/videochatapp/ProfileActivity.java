package com.estherkom.videochatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
private String receiverUserId ="", receiverUserImf ="", receiverUserName ="";
private ImageView background_profile_view;
 private TextView name_profile;
 private Button addBtn, declineBtn;
 private FirebaseAuth mAuth;
 private String senderUserId;
private String currentState ="new";
private DatabaseReference friendRequestRef, contactsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contact");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImf = getIntent().getExtras().get("user_img").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();
background_profile_view=findViewById(R.id.background_profile_view);
name_profile = findViewById(R.id.name_profile);
addBtn = findViewById(R.id.add_fried);
declineBtn = findViewById(R.id.dicline_fried_request);
        Picasso.get().load(receiverUserImf).into(background_profile_view);
        name_profile.setText(receiverUserName);




        manageClickEvents();
    }

    private void manageClickEvents() {
friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
       if (dataSnapshot.hasChild(receiverUserId)){
            String requestType = dataSnapshot.child(receiverUserId).
                    child("request_type").getValue().toString();
       if(requestType.equals("sent")) {
           currentState = "request_sent";
           addBtn.setText("Cancel Friend Request");
       }else if(requestType.equals("received")){
           currentState = "request_received";
           addBtn.setText("Accept Friend Request");
           declineBtn.setVisibility(View.VISIBLE);
           declineBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   CancelFriendRequest();
               }
           });
           }else {
           contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   if (dataSnapshot.hasChild(receiverUserId)){
                       currentState ="friends";
                       addBtn.setText("Delete Contact");
                   }else {
                       currentState = "new";
                   }
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
       }
       }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
        if (senderUserId.equals(receiverUserId)){
            addBtn.setVisibility(View.GONE);
        }else{
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
if (currentState.equals("new")){
SendFriendRequest();
}if(currentState.equals("request_sent")){
CancelFriendRequest();
}if(currentState.equals("request_received")){
AcceptFriendRequest();
}if(currentState.equals("request_sent")){
    CancelFriendRequest();

}
                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(senderUserId).
                child(receiverUserId).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
if (task.isSuccessful()){
    contactsRef.child(receiverUserId).
            child(senderUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()){
                friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        currentState = "friends";
                                        addBtn.setText("Delete Contact");

                                        declineBtn.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    });
}
            }
        });
    }

    private void CancelFriendRequest() {
       friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
if (task.isSuccessful()){
    friendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()){
                currentState = "new";
                addBtn.setText("Add Friend");
            }
        }
    });
}
           }
       });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(senderUserId).
                child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).
                            child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                currentState = "request_sent";
                                addBtn.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this,"Friend request sent",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });


    }
}
