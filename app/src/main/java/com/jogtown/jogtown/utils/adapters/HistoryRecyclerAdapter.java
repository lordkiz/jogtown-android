package com.jogtown.jogtown.utils.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.Conversions;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {

    public List<Object> jogs;
    String hostFragmentName = "HistoryFragment";


    public HistoryRecyclerAdapter(List arr) {
        jogs = arr;
    }

    public HistoryRecyclerAdapter(List arr, String holdingFragmentName) {
        jogs = arr;
        hostFragmentName = holdingFragmentName;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView runDate;
        TextView runDuration;
        TextView runAveragePace;
        TextView runDistance;
        ImageView jogTypeImageView;

        public ViewHolder(View view) {
            super(view);
            layout = view;
            runDate = view.findViewById(R.id.history_item_header_text);
            runDuration = view.findViewById(R.id.history_item_run_duration);
            runAveragePace = view.findViewById(R.id.history_item_average_pace);
            runDistance = view.findViewById(R.id.history_item_run_distance);
            jogTypeImageView = view.findViewById(R.id.history_item_jog_type_image_view);
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

            String coordsString = jsonObject.getString("coordinates");
            Gson gson = new Gson();
            Type gsonType = new TypeToken<List<List<Double>>>() {
            }.getType();
            List<List<Double>> coordinates = gson.fromJson(coordsString, gsonType);

            String jogType = "treadmill"; //jsonObject.getString("jog_type");

            if (jogType.equals("outdoor")) {
                holder.jogTypeImageView.setImageResource(R.drawable.treadmill_icon_white);
            } else {
                holder.jogTypeImageView.setImageResource(R.drawable.outdoor_icon_white);
            }

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

                try {
                    JSONObject jsonObj = new JSONObject(jogs.get(position).toString());

                    Bundle bundle = new Bundle();
                    bundle.putBoolean("canGoBack", true);
                    bundle.putBoolean("shouldSave", false);
                    bundle.putString("jog", jsonObj.toString());
                    if (hostFragmentName.equals("HistoryFragment")) {
                        Navigation.findNavController(v).navigate(R.id.action_historyFragment_to_jogDetailFragment, bundle);
                    } else if (hostFragmentName.equals("ProfileFragment")) {
                        Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_jogDetailFragment, bundle);
                    }

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



//    int IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_HEIGHT = 36;
//
//    int IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_WIDTH = 36;
//
//    int SCALE_FACTOR = 100;
//
//    Bitmap createPolylineImage(List<List<Double>> coordinates) {
//        Bitmap bitmap = Bitmap.createBitmap(
//                IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_WIDTH ,
//                IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_HEIGHT,
//                Bitmap.Config.ARGB_8888
//                );
//
//        Canvas canvas = new Canvas(bitmap);
//
//        Paint paint = new Paint();
//
//        paint.setColor(Color.WHITE);
//        paint.setStrokeWidth(2f);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setDither(true);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setAntiAlias(true);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//
//        for (int i = 0; i < coordinates.size(); i++) {
//            PointF point1 = latLngToPoint(new LatLng(coordinates.get(i).get(0), coordinates.get(i).get(1)));
//            PointF point2 = latLngToPoint(new LatLng(coordinates.get(i).get(0), coordinates.get(i).get(1)));
//            Log.i("Point", point1.toString());
//            try {
//                point2 = latLngToPoint(new LatLng(coordinates.get(i + 1).get(0), coordinates.get(i + 1).get(1)));
//            } catch (IndexOutOfBoundsException e) {
//                //
//            }
//            canvas.drawLine(
//                    point1.x, point1.y,
//                    point2.x, point2.y,
//                    paint
//            );
//        }
//
//        return Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//    }
//
//    private PointF latLngToPoint(LatLng latLng) {
//        // get x
//        double x = (latLng.longitude + 180) * (IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_WIDTH / 360);
//        // convert from degrees to radians
//        double latRad = latLng.latitude * Math.PI / 180;
//        // get y value
//        double mercatorProjection = Math.log(Math.tan((Math.PI / 4) + (latRad / 2)));
//        double y = (IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_HEIGHT / 2) - (
//                IMAGE_VIEW_IN_HISTORY_ITEM_LAYOUT_WIDTH * mercatorProjection / (2 * Math.PI));
//
//        PointF point = new PointF();
//        point.set((float) x, (float) y);
//
//        return point;
//    }


}
