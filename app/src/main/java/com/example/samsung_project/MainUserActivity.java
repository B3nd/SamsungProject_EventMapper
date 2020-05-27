package com.example.samsung_project;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
import java.util.Set;

public class MainUserActivity extends AppCompatActivity{
    FirebaseAuth auth;
    DatabaseReference ref;
    String currentUserID;
    String user_name;
    ChatsListFragment chatsListFragment;
    public ArrayAdapter<String> arrayAdapter;
    ArrayList<String> groupsList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        currentUserID = auth.getCurrentUser().getUid();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, groupsList){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.Light_Grey));
                textView.setPadding(3, 5, 3, 5);
                return textView;
            }
        };

        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();

                for (DataSnapshot d : dataSnapshot.getChildren()){
                    if(d.child("members").hasChild(currentUserID)){
                        set.add(d.getKey());
                    }
                }
                groupsList.clear();
                groupsList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });




        ViewPager viewPager = findViewById(R.id.viewPager);
        chatsListFragment = new ChatsListFragment();
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(chatsListFragment);
        pagerAdapter.addFragment(new MapFragment());
        viewPager.setAdapter(pagerAdapter);

    }

    static class MainPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> fragmentList = new ArrayList<>();
        public MainPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {return fragmentList.get(position);}

        @Override
        public int getCount() {return fragmentList.size();}

        void addFragment(Fragment fragment){
            fragmentList.add(fragment);
        }
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

        if(item.getItemId() == R.id.logout_menu) {
             auth.signOut();
             startActivity(new Intent(MainUserActivity.this, EnterActivity.class));
        }else if(item.getItemId() == R.id.settings_menu) {
            startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.Enter_group_menu) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainUserActivity.this, R.style.AlertDialog);
            builder.setTitle("Войти в группу");
            final EditText groupNameField = new EditText(MainUserActivity.this);
            groupNameField.setHint("Название группы");
            groupNameField.setTextColor(getResources().getColor(R.color.black));
            builder.setView(groupNameField);
            builder.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String groupName = groupNameField.getText().toString();
                    if (groupName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                    } else {
                        ref.child("Groups").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(groupName)) {
                                    if (!dataSnapshot.child(groupName).child("members").hasChild(currentUserID)) {
                                        ref.child("Groups").child(groupName).child("members").child(currentUserID).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                groupsList.add(groupName);
                                                arrayAdapter.notifyDataSetChanged();
                                            }
                                            });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "You already are in this group", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Entered group does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }
                }
            });

            builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        } else if (item.getItemId() == R.id.Create_group__menu) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainUserActivity.this, R.style.AlertDialog);
            builder2.setTitle("Создать группу");

            final EditText groupNameField2 = new EditText(MainUserActivity.this);
            groupNameField2.setHint("Название группы");
            groupNameField2.setTextColor(getResources().getColor(R.color.black));
            builder2.setView(groupNameField2);
            builder2.setPositiveButton("Создать группу", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String groupName = groupNameField2.getText().toString();
                    if (groupName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Пожалуйста, введите название группы", Toast.LENGTH_SHORT).show();
                    } else {
                        ref.child("Groups").child(groupName).child("members").child(currentUserID).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    groupsList.add(groupName);
                                    Toast.makeText(getApplicationContext(), groupName + " created!", Toast.LENGTH_LONG).show();
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                            });
                    }
                }
            });
            builder2.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
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