package com.example.myhealthtrackerapp.utils;

import com.example.myhealthtrackerapp.models.WorkoutSummary;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * Manager for fetching and summarizing today's workouts.
 */
public class DashboardWorkoutManager {

    /**
     * Callback for delivering workout summary results.
     */
    public interface WorkoutFetchListener {
        /** Called on successful fetch */
        void onFetched(WorkoutSummary summary);
        /** Called on error or if user not signed in */
        void onError(String errorMessage);
    }

    /**
     * Fetches today’s workout entries from Firebase, sums durations,
     * and returns a WorkoutSummary via the listener.
     */
    public static void fetchTodayWorkout(final WorkoutFetchListener listener) {
        DatabaseReference ref = FirebaseUtil.getWorkoutRef();
        if (ref == null) {
            listener.onError("User not authenticated");
            return;
        }

        // compute start/end of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();
        long endTime   = startTime + 24L*60*60*1000 - 1;

        ref.orderByChild("timestamp")
                .startAt(startTime)
                .endAt(endTime)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long total = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Object v = child.child("duration").getValue();
                            if (v instanceof Number) {
                                total += ((Number) v).longValue();
                            } else if (v instanceof String) {
                                try {
                                    total += Long.parseLong((String) v);
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                        listener.onFetched(new WorkoutSummary(total));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }
}
