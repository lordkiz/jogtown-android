package com.jogtown.jogtown.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.services.LocationService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupRunActiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupRunActiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupRunActiveFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;

    PolylineOptions polylineOptions = new PolylineOptions();

    private final int LOCATION_REQUEST_CODE = 101;

    SharedPreferences sharedPreferences;

    public GroupRunActiveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupRunActiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupRunActiveFragment newInstance(String param1, String param2) {
        GroupRunActiveFragment fragment = new GroupRunActiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_run_active, container, false);

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.group_run_activity_map_container, mMapFragment);

        fragmentTransaction.commit();

        registerLocationBroadcastReceiver();


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

                //It is a bit different how get OnRequestPermissionResult to work in fragments
                //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            } else {
                Uri msg = Uri.parse("map is ready");
                mListener.onFragmentInteraction(msg);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mMap != null) {
                            mMap.setMyLocationEnabled(true);
                            Uri msg = Uri.parse("map is ready"); // Tell Parent Activity
                            mListener.onFragmentInteraction(msg);
                        }
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocation != null) {
                            LatLng latlng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            updateMap(latlng);
                            Float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
                            Float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);

                            if (startLatitude == 0.0f || startLongitude == 0.0f) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putFloat("startLatitude", (float) lastKnownLocation.getLatitude());
                                editor.putFloat("startLongitude", (float) lastKnownLocation.getLongitude());
                                editor.apply();
                            }

                        }
                    }

                } else {
                    Toast.makeText(this.getContext(),"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void updateMap(LatLng coordinates) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        polylineOptions.add(coordinates);
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(5);
        mMap.addPolyline(polylineOptions);
    }


    public void registerLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double latitude = intent.getDoubleExtra("latitude", 0);
                        double longitude = intent.getDoubleExtra("longitude", 0);

                        LatLng loc = new LatLng(latitude, longitude);
                        updateMap(loc);

                        Float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
                        Float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);

                        if (startLatitude == 0.0f || startLongitude == 0.0f) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putFloat("startLatitude", (float) latitude);
                            editor.putFloat("startLongitude", (float) longitude);
                            editor.apply();
                        }

                    }
                }, new IntentFilter(LocationService.BROADCAST_ACTION)
        );
    }


}
