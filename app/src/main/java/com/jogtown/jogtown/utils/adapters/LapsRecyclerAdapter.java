package com.jogtown.jogtown.utils.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    int totalPace = 1;

    public LapsRecyclerAdapter(List<JSONObject> laps) {
        this.laps = laps;
        for (JSONObject jsonObject : laps) {
            try {
                int distance = jsonObject.getInt("distance");
                int duration = jsonObject.getInt("duration");
                totalPace += Conversions.calculatePace(distance, duration);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    public class LapsViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView lapKM;
        //TextView lapTime;
        TextView lapPace;
        ProgressBar paceStrengthBar;

        public LapsViewHolder(View view) {
            super(view);
            layout = view;
            lapKM = view.findViewById(R.id.lapKM);
            //lapTime = view.findViewById(R.id.lapTime);
            lapPace = view.findViewById(R.id.lapPace);
            paceStrengthBar = view.findViewById(R.id.paceStrengthBar);
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

            int distance = jsonObject.getInt("distance");
            int duration = jsonObject.getInt("duration");

            String km = Conversions.displayKilometres(distance);
            String time = Conversions.formatToHHMMSS(duration);
            String pace = Conversions.displayPace(distance, duration);

            holder.lapKM.setText(km);
            //holder.lapTime.setText(time);
            holder.lapPace.setText(pace);

            int paceInt = Conversions.calculatePace(distance, duration);
            int paceStrength = Math.round((float) paceInt/this.totalPace * 100);

            //To know the percentage which a paceStrngthBar should show we need to know
            //the total laps present. So for example, a total laps of 4 means for each to
            //the same strength bar level, the paceStrength has to be 100/4 = 25. So anything
            //less than 25 is a good pace and anything greater than 25 is slower. So let us color
            //the best pace green

            int personalSteadyPacePercentage = 100/this.laps.size();

            holder.paceStrengthBar.setProgress(paceStrength);

            if (paceStrength <= personalSteadyPacePercentage) {
                holder.paceStrengthBar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#29EB7F")));
            } else {
                holder.paceStrengthBar.setProgressTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFC82C")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return laps.size();
    }


}
