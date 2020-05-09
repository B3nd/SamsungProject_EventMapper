package com.example.samsung_project;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ChatActivity extends AppCompatActivity {


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    FirebaseAuth auth;
    FirebaseUser fbUser;
    ImageButton send;
    EditText text, author;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    RecyclerView messagesList;
    DataAdapter adapter;
    String currentGroupName, currentUserID, currentUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        send = findViewById(R.id.send_button);
        text = (EditText)findViewById(R.id.message_text);
        messagesList = (RecyclerView)findViewById(R.id.messages_list);
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        currentGroupName = getIntent().getExtras().get("group_name").toString();
        adapter = new DataAdapter(this, messages);
        messagesList.setAdapter(adapter);

        setTitle(currentGroupName);


        ref.child("Groups").child(currentGroupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    messages.add(new ChatMessage(d.child("text").getValue(String.class), d.child("author").getValue(String.class), d.child("id").getValue(String.class)));
                }


                adapter.notifyDataSetChanged();
                messagesList.smoothScrollToPosition(messages.size());

                */
                 messages.add(new ChatMessage(dataSnapshot.child("text").getValue(String.class), dataSnapshot.child("author").getValue(String.class), dataSnapshot.child("id").getValue(String.class)));
                adapter.notifyDataSetChanged();
                messagesList.smoothScrollToPosition(messages.size());
                for (ChatMessage c : messages){
                    if(c.text == null || c.text.equals("")){
                        messages.remove(c);
                    }
                }
            }


        });

        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message_text = text.getText().toString();
                if(!message_text.equals("")){
                    //messages.add(new ChatMessage(message_text, currentUserName, currentUserID));
                    ref.child("Groups").child(currentGroupName).push().setValue(new ChatMessage(message_text, currentUserName, currentUserID));

                }
                adapter.notifyDataSetChanged();
                text.getText().clear();
            }
        });

    }

    @Keep
    static class ChatMessage{
        public String text, author, id;
        public ChatMessage(){}
        public ChatMessage(String text, String author, String id){
            this.text = text;
            this.author = author;
            this.id = id;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{


        TextView message, author;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message =  itemView.findViewById(R.id.message_text);
            author = itemView.findViewById(R.id.message_user);
        }
    }

    class DataAdapter extends RecyclerView.Adapter<ViewHolder>{
        private int MSG_TYPE_OTHER = 0;
        private int MSG_TYPE_YOUR = 1;

        ArrayList<ChatMessage> messagesList;
        LayoutInflater inflater;

        public DataAdapter(Context context, ArrayList<ChatMessage> messagesList) {
            this.messagesList = messagesList;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if(viewType == MSG_TYPE_YOUR) {
                view = inflater.inflate(R.layout.message2, parent, false);
            } else {
                view = inflater.inflate(R.layout.message, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            holder.message.setText(messagesList.get(position).text);
            //holder.author.setText(messagesList.get(position).author);
            String id = messagesList.get(position).id;
            ref.child("Users").child(id).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if(name == null){
                        name = messagesList.get(position).author;
                    }
                    holder.author.setText(name);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            if(messagesList.get(position).id.equals(currentUserID)){
                return MSG_TYPE_YOUR;
            } else {
                return MSG_TYPE_OTHER;
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}