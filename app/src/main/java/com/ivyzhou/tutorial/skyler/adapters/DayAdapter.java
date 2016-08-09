package com.ivyzhou.tutorial.skyler.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivyzhou.tutorial.skyler.R;
import com.ivyzhou.tutorial.skyler.weather.Day;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ivy Zhou on 8/1/2016.
 */
public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private Day[] days;

    public DayAdapter(Day [] days) {
        this.days = days;
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // null is a viewgroup root
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_list_item, parent, false);
        DayViewHolder holder = new DayViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, int position) {
        if(position == 0)
            holder.bindDay(days[position], true);
        else
            holder.bindDay(days[position], false);

    }


    @Override
    public int getItemCount() {
        return days.length;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iconImageView) ImageView iconImageView;
        @BindView(R.id.temperatureLabel) TextView temperatureLabel;
        @BindView(R.id.dayNameLabel) TextView dayLabel;

        public DayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindDay(Day day, boolean isToday) {
            iconImageView.setImageResource(day.getIconId());
            temperatureLabel.setText(day.getTempMax() + "");
            if(isToday)
                dayLabel.setText("Today");
            else
                dayLabel.setText(day.getDayOfWeek());
        }
    }
}
