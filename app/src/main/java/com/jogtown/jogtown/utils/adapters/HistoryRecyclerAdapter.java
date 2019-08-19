package com.jogtown.jogtown.utils.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.JogDetailActivity;
import com.jogtown.jogtown.utils.Conversions;

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

                    intent.putExtra("jog", jsonObj.toString());

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
