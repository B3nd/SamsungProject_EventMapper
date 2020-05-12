package com.example.samsung_project;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
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

public class MapFragment extends Fragment implements Session.SearchListener, CameraListener {
    private MapView mapView;
    private EditText searchEdit;
    private SearchManager searchManager;
    ViewGroup root;
    private Session searchSession;
    BottomSheetBehavior bottomSheetBehavior;
    TextView information_title, information_text;
    private final String MAPKIT_API_KEY = "e18cbf4f-2ce4-481d-9a7c-2ae1cf2a6003";

    public MapFragment(){}

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

        root = (ViewGroup) inflater.inflate(R.layout.coordinator_layout, null);
        getActivity().getActionBar().hide();

        root.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatsActivity.class));
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
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

// настройка максимальной высоты
        bottomSheetBehavior.setPeekHeight(340);

// настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.setHideable(true);

// настройка колбэков при изменениях
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
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

        map.move(
                new CameraPosition(new Point(55.753595, 37.621031), 19.0f, 0.0f, 0.0f));

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
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {

            Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();

            if (resultLocation != null) {
                mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(root.getContext(), R.drawable.search_result));

            }
        }

        mapView.getMap().addTapListener(new GeoObjectTapListener() {
            @Override
            public boolean onObjectTap(@NonNull GeoObjectTapEvent geoObjectTapEvent) {

                String title = geoObjectTapEvent.getGeoObject().getName();
                try {
                    if (!title.equals("")) {
                        //Toast.makeText(root.getContext(), str, Toast.LENGTH_SHORT).show();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        information_title.setText(title);
                        Log.i("TAP_REGISTERED", title);
                        String desc = geoObjectTapEvent.getGeoObject().getDescriptionText();
                        //String desc = geoObjectTapEvent.getGeoObject().getAref().get(0);
                        //String desc = geoObjectTapEvent.getGeoObject().getMetadataContainer().getItem(String.class);
                        Log.i("description", desc);
                        if(!desc.equals("")){
                            information_text.setText(desc);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            information_text.setHeight(0);
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
