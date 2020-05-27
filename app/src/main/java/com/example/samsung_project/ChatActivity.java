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
import com.google.gson.Gson;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.Iterator;

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    FirebaseAuth auth;
    FirebaseUser fbUser;
    ImageButton send;
    EditText text;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    RecyclerView messagesList;
    DataAdapter adapter;
    String currentGroupName, currentUserID, currentUserName;
    final Point[] point = new Point[1];
    private final String MAPKIT_API_KEY = "e18cbf4f-2ce4-481d-9a7c-2ae1cf2a6003";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(ChatActivity.this);
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

        try{
            //Получаем точку из intent
            Gson gson  = new Gson();
            String s = getIntent().getStringExtra("point");
            point[0] = gson.fromJson(s, Point.class);

            if(point[0] == null) {
                ref.child("Groups").child(currentGroupName).push().setValue(new ChatMessage(getIntent().getExtras().get("map_message").toString(), currentUserName, currentUserID));
            } else {
                ref.child("Groups").child(currentGroupName).push().setValue(new ChatMessage(getIntent().getExtras().get("map_message").toString(), currentUserName, currentUserID, point[0]));
                point[0] = null;
            }
            adapter.notifyDataSetChanged();
        } catch(NullPointerException e){
            e.getLocalizedMessage();
        }



        ref.child("Groups").child(currentGroupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatMessage m;
                if(dataSnapshot.child("point").getValue(Point.class) == null) {
                    m = new ChatMessage(dataSnapshot.child("text").getValue(String.class), dataSnapshot.child("author").getValue(String.class), dataSnapshot.child("id").getValue(String.class));
                } else {
                    m = new ChatMessage(dataSnapshot.child("text").getValue(String.class), dataSnapshot.child("author").getValue(String.class), dataSnapshot.child("id").getValue(String.class), dataSnapshot.child("point").getValue(Point.class));
                }
                messages.add(m);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message_text = text.getText().toString();
                if(!message_text.equals("")){
                    if(point[0] == null) {
                        ref.child("Groups").child(currentGroupName).push().setValue(new ChatMessage(message_text, currentUserName, currentUserID));
                    } else {
                        ref.child("Groups").child(currentGroupName).push().setValue(new ChatMessage(message_text, currentUserName, currentUserID, point[0]));
                        point[0] = null;
                    }
                }
                adapter.notifyDataSetChanged();
                text.getText().clear();
            }
        });

    }

    @Keep
    static class ChatMessage{
        public String text, author, id;
        public Point point;
        public ChatMessage(){}
        public ChatMessage(String text, String author, String id){
            this.text = text;
            this.author = author;
            this.id = id;
            this.point = null;
        }
        public ChatMessage(String text, String author, String id, Point point){
            this.text = text;
            this.author = author;
            this.id = id;
            this.point = point;
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView message, author;
        MapView map;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message =  itemView.findViewById(R.id.message_text);
            author = itemView.findViewById(R.id.message_user);
            map = itemView.findViewById(R.id.message_map_view);
        }
    }

    class DataAdapter extends RecyclerView.Adapter<ViewHolder>{
        private int MSG_TYPE_OTHER = 0;
        private int MSG_TYPE_YOUR = 1;
        private int MSG_TYPE_OTHER_WITH_POINT = 3;
        private int MSG_TYPE_YOUR_WITH_POINT = 4;

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
            } else if(viewType == MSG_TYPE_OTHER){
                view = inflater.inflate(R.layout.message, parent, false);
            } else if (viewType == MSG_TYPE_YOUR_WITH_POINT){
                view = inflater.inflate(R.layout.map_message2, parent, false);
            } else {
                view = inflater.inflate(R.layout.map_message, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY);
            MapKitFactory.initialize(ChatActivity.this);
            final ChatMessage message = messagesList.get(position);
            holder.message.setText(message.text);
            try {
                Map map = holder.map.getMap();

                map.move(
                        new CameraPosition(message.point, 19.0f, 0.0f, 0.0f));

                map.addCameraListener(new CameraListener() {
                    @Override
                    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateSource cameraUpdateSource, boolean b) {
                        if (message.point != null) {
                            MapObjectCollection mapObjects = map.getMapObjects();
                            mapObjects.clear();
                            mapObjects.addPlacemark(message.point);
                        }
                    }
                });
            } catch (NullPointerException e){
                e.getLocalizedMessage();
            }
            String id = message.id;
            ref.child("Users").child(id).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if(name == null){
                        name = message.author;
                    }
                    holder.author.setText(name);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage message = messagesList.get(position);
            if(message.id.equals(currentUserID)){
                if(message.point == null) {
                    return MSG_TYPE_YOUR;
                } else {
                    return MSG_TYPE_YOUR_WITH_POINT;
                }
            } else {
                if(message.point == null) {
                    return MSG_TYPE_OTHER;
                } else{
                    return MSG_TYPE_OTHER_WITH_POINT;
                }
            }
        }

        @Override
        public int getItemCount() {return messages.size();}
    }
}