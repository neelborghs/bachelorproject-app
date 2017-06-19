package com.thomasmore.ezgreen.adapters;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.model.Plant;

import java.util.List;

/**
 * Created by Tomas-Laptop on 29/04/2017.
 */

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.CardViewHolder> {

    List<Plant> plants;
    int moisture;

    public DashboardAdapter(List<Plant> plants) {
        this.plants = plants;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tvNoData;
        //ImageButton ibPlantPhoto;
        TextView tvModuleName;
        TextView tvTemperature;
        TextView tvLight;
        ImageView ivLight;
        TextView tvHumidity;
        TextView tvMoisture;
        View vModuleState;

        CardViewHolder(View cardView) {
            super(cardView);
            cv = (CardView)itemView.findViewById(R.id.recycler_view);
            tvNoData = (TextView) cardView.findViewById(R.id.no_module);

            //ibPlantPhoto = (ImageButton) cardView.findViewById(R.id.ib_plant_photo);
            tvModuleName = (TextView) cardView.findViewById(R.id.tv_module_name);
            tvTemperature = (TextView)cardView.findViewById(R.id.tv_temperature);
            tvLight = (TextView)cardView.findViewById(R.id.tv_light);
            ivLight = (ImageView)cardView.findViewById(R.id.iv_light);
            tvHumidity = (TextView)cardView.findViewById(R.id.tv_humidity);
            tvMoisture = (TextView)cardView.findViewById(R.id.tv_moisture);
            vModuleState = cardView.findViewById(R.id.v_module_state);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_dashboard_card, viewGroup, false);
        CardViewHolder cvh = new CardViewHolder(view);
        return cvh;
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int position) {
        cardViewHolder.tvModuleName.setText("Module " + plants.get(position).getModule_id());
        if(plants.get(position).getLight().equals("1")) {
            cardViewHolder.tvLight.setText("Light");
            cardViewHolder.ivLight.setImageResource(R.drawable.sun);
        } else {
            cardViewHolder.tvLight.setText("Dark");
            cardViewHolder.ivLight.setImageResource(R.drawable.moon);
        }
        cardViewHolder.tvHumidity.setText(plants.get(position).getHumidity().split("\\.")[0] + "%");
        cardViewHolder.tvTemperature.setText(plants.get(position).getTemperature().split("\\.")[0] + "°C");
        cardViewHolder.tvMoisture.setText(plants.get(position).getMoisture() + "%");

        moisture = Integer.valueOf(plants.get(position).getMoisture());
        if(moisture >= 55 ) {
            cardViewHolder.vModuleState.setBackgroundColor(Color.parseColor("#2ECC71"));
        }
        else if (moisture < 55 && moisture >= 50){
            cardViewHolder.vModuleState.setBackgroundColor(Color.parseColor("#FFB74D"));
        }

        else {
            cardViewHolder.vModuleState.setBackgroundColor(Color.parseColor("#EF5350"));
        }

        if (position + 1 == getItemCount()) {
            //bottom margin so user can scroll past fab
            setBottomMargin(cardViewHolder.itemView, (int) (90 * Resources.getSystem().getDisplayMetrics().density));
        } else {
            setBottomMargin(cardViewHolder.itemView, 0);
        }
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private static void setBottomMargin(View view, int bottomMargin) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);
            view.requestLayout();
        }
    }
}
