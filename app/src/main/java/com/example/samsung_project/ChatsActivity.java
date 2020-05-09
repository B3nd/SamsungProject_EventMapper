package com.example.samsung_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChatsActivity extends AppCompatActivity {
    FirebaseAuth auth;
    private DatabaseReference ref;
    ListView list;
    String currentUserID;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groupsList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chats_list);

        ref = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();
        setTitle("Выберите чат");

        list = (ListView) findViewById(R.id.ChatsList);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, groupsList) /*{
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)list.getChildAt(position);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        }*/;
        list.setAdapter(arrayAdapter);


        ref.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                //ArrayList<String> set = new ArrayList<>();

                Iterator it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){

                    //if(((DataSnapshot)it.next()).child("members").hasChild(currentUserID)){
                        set.add(((DataSnapshot)it.next()).getKey());
                    //}


                }


                //for (DataSnapshot d : dataSnapshot.getChildren()){
                    //if(d.child("members").hasChild(currentUserID)){

                        //set.add(d.getValue(String.class));
                    //}
                //}
                groupsList.clear();
                groupsList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String group_name = parent.getItemAtPosition(position).toString();
                startActivity(new Intent(ChatsActivity.this, ChatActivity.class).putExtra("group_name", group_name));
            }
        });




        currentUserID = auth.getCurrentUser().getUid();

        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("name").exists()){
                    startActivity(new Intent(ChatsActivity.this, SettingsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        if(id == R.id.logout_menu){
            auth.signOut();
            startActivity(new Intent(ChatsActivity.this, MainActivity.class));
        } else if(id == R.id.settings_menu){
            startActivity(new Intent(ChatsActivity.this, SettingsActivity.class));
        } else if(id == R.id.Enter_group_menu) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this, R.style.AlertDialog);
            builder.setTitle("Enter Group name");

            final EditText groupNameField = new EditText(ChatsActivity.this);
            groupNameField.setHint("Group name");
            groupNameField.setTextColor(getResources().getColor(R.color.black));
            builder.setView(groupNameField);
            builder.setPositiveButton("Enter group", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String groupName = groupNameField.getText().toString();
                    if(groupName.equals("")){
                        Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                    } else {
                        ref.child("Groups").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(groupName)){
                                    if(!dataSnapshot.child(groupName).child("members").hasChild(currentUserID)) {
                                        ref.child("Groups").child(groupName).child("members").child(currentUserID).setValue("");
                                        arrayAdapter.notifyDataSetChanged();
                                    } else{
                                        Toast.makeText(getApplicationContext(), "You already are in this group", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Entered group does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else if(id == R.id.Create_group__menu){
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this, R.style.AlertDialog);
            builder.setTitle("Enter Group name");

            final EditText groupNameField = new EditText(ChatsActivity.this);
            groupNameField.setHint("Group name");
            groupNameField.setTextColor(getResources().getColor(R.color.black));
            builder.setView(groupNameField);
            builder.setPositiveButton("Create group", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String groupName = groupNameField.getText().toString();
                    if(groupName.equals("")){
                        Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                    } else {
                        ref.child("Groups").child(groupName).child("members").push().setValue(currentUserID).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    groupsList.add(groupName);
                                    Toast.makeText(getApplicationContext(), groupName + " created!", Toast.LENGTH_LONG).show();
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        return true;
    }

}
