package com.jogtown.jogtown.utils;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.AppActivity;
import com.jogtown.jogtown.activities.JogDetailActivity;
import com.jogtown.jogtown.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {

    public List<Object> jogs;

    public HistoryRecyclerAdapter(List arr) {
        jogs = arr;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView runDate;
        TextView runDuration;
        TextView runAveragePace;
        TextView runDistance;

        public ViewHolder(View view) {
            super(view);
            layout = view;
            runDate = view.findViewById(R.id.history_item_header_text);
            runDuration = view.findViewById(R.id.history_item_run_duration);
            runAveragePace = view.findViewById(R.id.history_item_average_pace);
            runDistance = view.findViewById(R.id.history_item_run_distance);
        }
    }




    @NonNull
    @Override
    public HistoryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.history_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jogs.get(position).toString());
            String dateTime = jsonObject.getString("created_at");

            holder.runDate.setText(Conversions.formatDateTime(dateTime));

            holder.runDuration.setText(
                    Conversions.formatToHHMMSS(
                    Integer.parseInt(jsonObject.getString("duration"))
                    ));

            holder.runAveragePace.setText(Conversions.displayPace(
                    Integer.parseInt(jsonObject.getString("distance")),
                    Integer.parseInt(jsonObject.getString("duration"))
            ));

            holder.runDistance.setText(
                    Conversions.displayKilometres(
                    Integer.parseInt(jsonObject.getString("distance"))
                    ) + " km");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), JogDetailActivity.class);

                try {
                    JSONObject jsonObj = new JSONObject(jogs.get(position).toString());
                    int duration = jsonObj.getInt("duration");
                    int distance = jsonObj.getInt("distance");
                    float calories = jsonObj.getInt("calories");
                    float startLatitude = jsonObj.getInt("start_latitude");
                    float endLatitude = jsonObj.getInt("end_latitude");
                    float startLongitude = jsonObj.getInt("start_longitude");
                    float endLongitude = jsonObj.getInt("end_longitude");
                    float averageSpeed = jsonObj.getInt("average_speed");
                    float maxSpeed = jsonObj.getInt("max_speed");
                    int averagePace = jsonObj.getInt("average_pace");
                    int maxPace = jsonObj.getInt("max_pace");
                    int hydration = jsonObj.getInt("hydration");
                    float maxAltitude = jsonObj.getInt("max_altitude");
                    float minAltitude = jsonObj.getInt("min_altitude");
                    int totalAscent = jsonObj.getInt("total_ascent");
                    int totalDescent = jsonObj.getInt("total_descent");
                    String coordinates = jsonObj.getString("coordinates");
                    String paces = jsonObj.getJSONArray("paces").toString();
                    String speeds = jsonObj.getJSONArray("speeds").toString();

                    intent.putExtra("duration", duration);
                    intent.putExtra("distance", distance);
                    intent.putExtra("calories", calories);
                    intent.putExtra("startLatitude", startLatitude);
                    intent.putExtra("endLatitude", endLatitude);
                    intent.putExtra("startLongitude", startLongitude);
                    intent.putExtra("endLongitude", endLongitude);
                    intent.putExtra("averageSpeed", averageSpeed);
                    intent.putExtra("maxSpeed", maxSpeed);
                    intent.putExtra("averagePace", averagePace);
                    intent.putExtra("maxPace", maxPace);
                    intent.putExtra("coordinates", coordinates);
                    intent.putExtra("speeds", speeds);
                    intent.putExtra("paces", paces);
                    intent.putExtra("hydration", hydration);
                    intent.putExtra("maxAltitude", maxAltitude);
                    intent.putExtra("minAltitude", minAltitude);
                    intent.putExtra("totalAscent", totalAscent);
                    intent.putExtra("totalDescent", totalDescent);

                    intent.putExtra("canGoBack", true);
                    intent.putExtra("shouldSave", false);

                    v.getContext().startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return jogs.size();
    }

}
