package com.example.samsung_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatsListFragment extends Fragment {
    ViewGroup root;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private ListView list;
    private String currentUserID;
    public ArrayAdapter<String> arrayAdapter;
    ArrayList<String> groupsList = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.chats_list, null);
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        list = (ListView) root.findViewById(R.id.ChatsList);
        currentUserID = auth.getCurrentUser().getUid();

        //Редактирование отображения списка групп
        arrayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_list_item_1, groupsList){
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

        //Получение всех групп, в которых состоит пользователь
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
            public void onCancelled(@NonNull DatabaseError databaseError){}});

        //Отправка пользователя в выбранный чат
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String group_name = parent.getItemAtPosition(position).toString();
                startActivity(new Intent(ChatsListFragment.this.getActivity(), ChatActivity.class).putExtra("group_name", group_name));
            }
        });

        //Проверка на наличие имени пользователя
        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("name").exists()){
                    startActivity(new Intent(ChatsListFragment.this.getActivity(), SettingsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return root;
    }
}
