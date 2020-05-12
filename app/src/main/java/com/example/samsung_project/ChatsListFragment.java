package com.example.samsung_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.fragment.app.Fragment;

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

public class ChatsListFragment extends Fragment {
    FirebaseAuth auth;
    private DatabaseReference ref;
    ListView list;
    String currentUserID;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groupsList = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.chats_list, null);
        getActivity().getActionBar().setTitle("Выберите чат");
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        list = (ListView) root.findViewById(R.id.ChatsList);
        currentUserID = auth.getCurrentUser().getUid();

        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groupsList){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.Light_Grey));
                textView.setPadding(3, 5, 3, 5);
                return textView;
            }
        };
        list.setAdapter(arrayAdapter);


        ref.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();

                Iterator it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){
                    if(((DataSnapshot)it.next()).child("members").hasChild(currentUserID)) {
                        set.add(((DataSnapshot) it.next()).getKey());
                    }
                }

                groupsList.clear();
                groupsList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}});

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String group_name = parent.getItemAtPosition(position).toString();
                startActivity(new Intent(ChatsListFragment.this.getActivity(), ChatActivity.class).putExtra("group_name", group_name));
            }
        });

        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("name").exists()){
                    startActivity(new Intent(ChatsListFragment.this.getActivity(), SettingsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();


        switch (id){
            case R.id.logout_menu:
                auth.signOut();
                startActivity(new Intent(ChatsListFragment.this.getActivity(), EnterActivity.class));
            case R.id.settings_menu:
                startActivity(new Intent(ChatsListFragment.this.getActivity(), SettingsActivity.class));
            case R.id.Enter_group_menu:
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatsListFragment.this.getContext(), R.style.AlertDialog);
                builder.setTitle("Введите название группы");
                final EditText groupNameField = new EditText(ChatsListFragment.this.getContext());
                groupNameField.setHint("Название группы");
                groupNameField.setTextColor(getResources().getColor(R.color.black));
                builder.setView(groupNameField);
                builder.setPositiveButton("Enter group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String groupName = groupNameField.getText().toString();
                        if(groupName.equals("")){
                            Toast.makeText(getContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                        } else {
                            ref.child("Groups").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(groupName)){
                                        if(!dataSnapshot.child(groupName).child("members").hasChild(currentUserID)) {
                                            ref.child("Groups").child(groupName).child("members").child(currentUserID).setValue("");
                                            arrayAdapter.notifyDataSetChanged();
                                        } else{
                                            Toast.makeText(getContext(), "You already are in this group", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Entered group does not exist", Toast.LENGTH_SHORT).show();
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
            case R.id.Create_group__menu:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(ChatsListFragment.this.getContext(), R.style.AlertDialog);
                builder2.setTitle("Enter Group name");

                final EditText groupNameField2 = new EditText(ChatsListFragment.this.getContext());
                groupNameField2.setHint("Group name");
                groupNameField2.setTextColor(getResources().getColor(R.color.black));
                builder2.setView(groupNameField2);
                builder2.setPositiveButton("Create group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String groupName = groupNameField2.getText().toString();
                        if(groupName.equals("")){
                            Toast.makeText(getContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                        } else {
                            ref.child("Groups").child(groupName).child("members").child(currentUserID).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        groupsList.add(groupName);
                                        Toast.makeText(getContext(), groupName + " created!", Toast.LENGTH_LONG).show();
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                        }
                    }
                });
                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder2.show();



        }



        return true;
    }
}
