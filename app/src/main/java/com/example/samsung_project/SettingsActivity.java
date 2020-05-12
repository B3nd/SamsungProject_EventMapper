package com.example.samsung_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    EditText nameChanging;
    CircleImageView image;
    DatabaseReference ref;
    FirebaseAuth auth;
    FirebaseUser user;
    String currentUserID;
    String name;
    private static final int GalleyPick = 1;
    private StorageReference UserProfileImagesRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        nameChanging = findViewById(R.id.changing_name);
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        currentUserID = auth.getCurrentUser().getUid();
        //UserProfileImagesRef = FirebaseStorage().getInstance().getReference().child("Profile Images");

        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameChanging.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findViewById(R.id.save_changes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("Users").child(currentUserID).child("name").setValue(nameChanging.getText().toString());
            }
        });
        /*
        image = (CircleImageView) findViewById(R.id.profile_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleyIntent = new Intent();
                galleyIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleyIntent.setType("image/*");
                startActivityForResult(galleyIntent, GalleyPick);
            }
        });

         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleyPick && resultCode == RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();

                //StorageReference filePath = UserProfileImagesRef.child(currentUserID )

            }

        }
    }
}
