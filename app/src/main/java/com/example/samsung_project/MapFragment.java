package com.example.samsung_project;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MapFragment extends Fragment implements Session.SearchListener, CameraListener {
    private MapView mapView;
    private EditText searchEdit;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private String currentUserID;
    private SearchManager searchManager;
    public ArrayAdapter<String> arrayAdapter;
    ArrayList<String> groupsList = new ArrayList<>();
    ViewGroup root;
    String user_name;
    private Session searchSession;
    Point[] message_point = new Point[1];
    String[] point_name = new String[1];
    String[] date = new String[2];
    BottomSheetBehavior bottomSheetBehavior;
    TextView information_title, information_text;
    private final String MAPKIT_API_KEY = "e18cbf4f-2ce4-481d-9a7c-2ae1cf2a6003";

    public MapFragment(){}

    //Отображение результатов поиска
    private void submitQuery(String query) {
        searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                new SearchOptions(),
                this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(getContext());
        SearchFactory.initialize(getContext());
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        currentUserID = auth.getCurrentUser().getUid();

        //Получение имени поьзователя
        ref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        root = (ViewGroup) inflater.inflate(R.layout.coordinator_layout, null);
        //Редактирование отображения списка групп
        arrayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_list_item_1, groupsList){

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setPadding(10, 5, 3, 5);
                textView.setBackground(root.getContext().getDrawable(R.drawable.listview_item_border));
                return textView;
            }
        };

        //Получение списка групп, в которых состоит пользователь
        root.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                //Создание окна выбора даты
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MapFragment.this.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month ++;
                                date[0] = "" + dayOfMonth + "." + month + "." + year;

                                //Создание окна выбора времени
                                TimePickerDialog timePickerDialog = new TimePickerDialog(MapFragment.this.getContext(), new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        if(minute / 10 == 0){
                                            date[1] = hourOfDay + ":0" + minute;
                                        } else {
                                            date[1] = hourOfDay + ":" + minute;
                                        }

                                        //Создание окна выбора группы
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MapFragment.this.getActivity(), R.style.AlertDialog);
                                        builder.setTitle("Выберите группу");

                                        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String group_name = groupsList.get(which);
                                                //Создание окна подтверждения выбранной даты, времени и группы
                                                AlertDialog.Builder builderInner = new AlertDialog.Builder(MapFragment.this.getActivity());
                                                String dialog_message_text = "Место: " + point_name[0] + "\n" +
                                                        "Группа: " + group_name + "\n" +
                                                        "Дата: " + date[0] + "\n" +
                                                        "Время: " + date[1];
                                                builderInner.setMessage(dialog_message_text);
                                                builderInner.setTitle("Вы выбрали: ");
                                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,int which) {
                                                        dialog.dismiss();

                                                        String message = user_name + " предложил пойти сюда: \n" + point_name[0] + " " + date[0] + " в " + date[1];
                                                        Gson gson = new Gson();
                                                        String p = gson.toJson(message_point[0]);
                                                        startActivity(new Intent(MapFragment.this.getActivity(), ChatActivity.class).putExtra("group_name", group_name).putExtra("map_message", message).putExtra("point", p));
                                                    }
                                                });

                                                builderInner.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });

                                                builderInner.show();

                                            }
                                        });
                                        builder.show();
                                    }
                                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
                                timePickerDialog.show();

                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
        });

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        mapView = (MapView)root.findViewById(R.id.mapview);
        mapView.getMap().addCameraListener(this);

        // получение вью нижнего экрана
        LinearLayout llBottomSheet = (LinearLayout) root.findViewById(R.id.bottom_sheet);
        // настройка поведения нижнего экрана
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        information_title = root.findViewById(R.id.information_title);
        information_text = root.findViewById(R.id.information_text);
        // настройка состояний нижнего экрана
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // настройка максимальной высоты
        bottomSheetBehavior.setPeekHeight(340);
        // настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.setHideable(true);
        // настройка колбэков при изменениях
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        searchEdit = (EditText) root.findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    submitQuery(searchEdit.getText().toString());
                }
                return false;
            }
        });

        Map map = mapView.getMap();

        map.move(new CameraPosition(new Point(55.753595, 37.621031), 16.0f, 0.0f, 0.0f));

        submitQuery(searchEdit.getText().toString());
        return root;
    }

    @Override
    public void onStop(){super.onStop();}

    @Override
    public void onStart(){
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {
        //Получение всех отметок, которые уже находятся на карте и их удаление
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

        //Добавление отметок в коллекцию
        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
            Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(root.getContext(), R.drawable.search_result));
            }
        }

        //Отображение информации о объекте, которой выбрал пользователь
        mapView.getMap().addTapListener(new GeoObjectTapListener() {
            @Override
            public boolean onObjectTap(@NonNull GeoObjectTapEvent geoObjectTapEvent) {
                GeoObject g = geoObjectTapEvent.getGeoObject();
                String title = g.getName();
                try {
                    if (!title.equals("")) {
                        message_point[0] = g.getGeometry().get(0).getPoint();
                        point_name[0] = title;
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        information_title.setText(title);
                        Log.i("TAP_REGISTERED", title);
                        String desc = g.getDescriptionText();
                        Log.i("description", desc);
                        if(!desc.equals("")){
                            //Отображение описания объекта
                            information_text.setText(desc);
                            information_text.setTextColor(Color.parseColor("#7F7F7F"));
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            information_text.setHint("");

                            information_text.setHintTextColor(Color.TRANSPARENT);
                            information_text.setHeight(2);
                            root.removeView(information_text);
                        }
                        return true;
                    }
                } catch (NullPointerException e) {
                    Log.i("TAP_ERROR", e.getLocalizedMessage());
                }
                return false;
            }
        });
    }

    @Override
    public void onSearchError(@NonNull Error error) {String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(root.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateSource cameraUpdateSource, boolean finished) {
        if (finished) {
            submitQuery(searchEdit.getText().toString());
        }
    }
}
