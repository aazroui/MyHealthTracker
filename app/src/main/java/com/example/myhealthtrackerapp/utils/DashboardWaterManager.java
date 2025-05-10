package com.example.myhealthtrackerapp.utils;

import com.example.myhealthtrackerapp.models.WaterSummary;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class DashboardWaterManager {

    /** Callback for when today’s water-sum query completes. */
    public interface WaterFetchListener {
        /** Called on success with summed volumes. */
        void onFetched(WaterSummary summary);
        /** Called on failure (or if user isn’t signed in). */
        void onError(String errorMessage);
    }

    /**
     * Fetches all of today’s water entries from Firebase, sums them,
     * and returns a WaterSummary via the listener.
     */
    public static void fetchTodayWater(final WaterFetchListener listener) {
        DatabaseReference ref = FirebaseUtil.getWaterRef();
        if (ref == null) {
            listener.onError("User not authenticated");
            return;
        }

        // Calculate start/end of today in millis
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE,      0);
        start.set(Calendar.SECOND,      0);
        start.set(Calendar.MILLISECOND, 0);
        long startTime = start.getTimeInMillis();

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 1);
        long endTime = end.getTimeInMillis() - 1;

        // Query by timestamp between [startTime, endTime]
        ref.orderByChild("timestamp")
                .startAt(startTime)
                .endAt(endTime)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double totalMl = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Object v = child.child("amount").getValue();
                            double ml = 0;
                            if (v instanceof Number) {
                                ml = ((Number) v).doubleValue();
                            } else if (v instanceof String) {
                                try {
                                    ml = Double.parseDouble((String) v);
                                } catch (NumberFormatException ignored) {}
                            }
                            totalMl += ml;
                        }
                        double totalCups = totalMl / 236.59;
                        listener.onFetched(new WaterSummary(totalMl, totalCups));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }
}
