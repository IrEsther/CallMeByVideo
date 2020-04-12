package com.estherkom.videochatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannelGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotigicatonsActivity extends
        AppCompatActivity {
    private  RecyclerView notificationList;
    private DatabaseReference friendRequestRef, contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notigicatons);
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contact");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        notificationList = findViewById(R.id.notificationt_list);
        notificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));




    }

    @Override
    protected void onStart() {
        super.onStart();
      FirebaseRecyclerOptions  options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(friendRequestRef.child(currentUserId), Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, int i, @NonNull Contacts contacts) {
                notificationViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                notificationViewHolder.cancelBtn.setVisibility(View.VISIBLE);


                final String listUserId = getRef(i).getKey();


        DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
        requestTypeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                  if(dataSnapshot.exists()){
            String type = dataSnapshot.getValue().toString();
            if (type.equals("received")){
                notificationViewHolder.cardView.setVisibility(View.VISIBLE);
                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")){
                            final  String imageStr = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(imageStr).into(notificationViewHolder.profileImageView);

                        }
                            final  String nameStr = dataSnapshot.child("name").getValue().toString();
                            notificationViewHolder.userNameTxt.setText(nameStr);
                            notificationViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {


                            @Override
                            public void onClick(View v) {
                                contactsRef.child(currentUserId).
                                        child(listUserId).child("Contact").setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    contactsRef.child(listUserId).
                                                            child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                friendRequestRef.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            friendRequestRef.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        Toast.makeText(NotigicatonsActivity.this,"New Contact Saved",Toast.LENGTH_SHORT).show();
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
                                        });                            }
                        });
                        notificationViewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {


                            @Override
                            public void onClick(View v) {
                                friendRequestRef.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            friendRequestRef.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){

                                                        Toast.makeText(NotigicatonsActivity.this,"Friend Request Canceled",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }else {
                notificationViewHolder.cardView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                NotificationViewHolder viewHolder = new NotificationViewHolder(view);
                return  viewHolder;            }
        };


        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt;
        Button acceptBtn, cancelBtn;
        ImageView profileImageView;
        RelativeLayout cardView;



        public NotificationViewHolder(@NonNull View itemView){
            super(itemView);

            userNameTxt = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_decline_btn);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.card_view);


        }
    }
//    private void CancelFriendRequest() {
//
//    }
//
//    private void AcceptFriendRequest() {
//
//
//    }
}
