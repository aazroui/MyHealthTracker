package com.example.myhealthtrackerapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.Sleep;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepSessionAdapter extends RecyclerView.Adapter<SleepSessionAdapter.ViewHolder> {

    private final List<Sleep> sessions;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d", Locale.getDefault());

    public SleepSessionAdapter(List<Sleep> sessions) {
        this.sessions = sessions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sleep s = sessions.get(position);

        // 1) Hours + optional quality
        String quality = s.getQuality();              // may be null if not set
        String hoursTxt = String.format(Locale.getDefault(),
                "%.1f hr", s.getHoursSlept());
        if (quality != null && !quality.isEmpty()) {
            hoursTxt += " – " + quality;
        }
        holder.tvHours.setText(hoursTxt);

        // 2) Date
        holder.tvDate.setText(dateFormat.format(new Date(s.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHours, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvHours = itemView.findViewById(R.id.tv_sleep_hours);
            tvDate  = itemView.findViewById(R.id.tv_sleep_date);
        }
    }
}
