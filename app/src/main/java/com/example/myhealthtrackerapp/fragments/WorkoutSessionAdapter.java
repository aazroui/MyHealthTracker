package com.example.myhealthtrackerapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myhealthtrackerapp.R;
import com.example.myhealthtrackerapp.models.Workout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionAdapter extends RecyclerView.Adapter<WorkoutSessionAdapter.ViewHolder> {

    private final List<Workout> sessions;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d", Locale.getDefault());

    public WorkoutSessionAdapter(List<Workout> sessions) {
        this.sessions = sessions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Workout w = sessions.get(position);
        holder.tvType.setText(w.getWorkoutType());
        holder.tvDuration.setText(w.getDuration() + " min");
        holder.tvDate.setText(dateFormat.format(new Date(w.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDuration, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvType     = itemView.findViewById(R.id.tv_session_type);
            tvDuration = itemView.findViewById(R.id.tv_session_duration);
            tvDate     = itemView.findViewById(R.id.tv_session_date);
        }
    }
}
