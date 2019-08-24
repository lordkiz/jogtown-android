package com.jogtown.jogtown.utils.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.Conversions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LapsRecyclerAdapter extends RecyclerView.Adapter<LapsRecyclerAdapter.LapsViewHolder> {

    List<JSONObject> laps;

    public LapsRecyclerAdapter(List<JSONObject> laps) {
        this.laps = laps;

    }


    public class LapsViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView lapKM;
        TextView lapTime;
        TextView lapPace;

        public LapsViewHolder(View view) {
            super(view);
            layout = view;
            lapKM = view.findViewById(R.id.lapKM);
            lapTime = view.findViewById(R.id.lapTime);
            lapPace = view.findViewById(R.id.lapPace);
        }
    }

    @NonNull
    @Override
    public LapsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.laps_layout, parent, false);
        LapsViewHolder viewHolder = new LapsViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LapsViewHolder holder, int position) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(laps.get(position).toString());
            String km = Conversions.displayKilometres(jsonObject.getInt("distance"));
            String time = Conversions.formatToHHMMSS(jsonObject.getInt("duration"));
            String pace = Conversions.displayPace(jsonObject.getInt("distance"), jsonObject.getInt("duration"));

            holder.lapKM.setText(km);
            holder.lapTime.setText(time);
            holder.lapPace.setText(pace);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return laps.size();
    }


}
