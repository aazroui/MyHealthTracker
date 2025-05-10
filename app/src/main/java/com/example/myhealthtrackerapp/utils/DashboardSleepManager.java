package com.example.myhealthtrackerapp.utils;

import com.example.myhealthtrackerapp.models.SleepSummary;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * Manager for fetching and summarizing today's sleep entries.
 */
public class DashboardSleepManager {

    /**
     * Callback interface for delivering sleep-summary results.
     */
    public interface SleepFetchListener {
        /** Called when the summary is successfully loaded. */
        void onFetched(SleepSummary summary);
        /** Called on error querying Firebase. */
        void onError(String errorMessage);
    }

    /**
     * Fetches today’s sleep entries from Firebase, sums their hours,
     * and returns a SleepSummary via the provided callback.
     */
    public static void fetchTodaySleep(final SleepFetchListener callback) {
        DatabaseReference sleepRef = FirebaseUtil.getSleepRef();
        if (sleepRef == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Compute start & end of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        final long startTime = cal.getTimeInMillis();
        final long endTime   = startTime + 24L*60*60*1000 - 1;

        // Query entries by timestamp
        sleepRef
                .orderByChild("timestamp")
                .startAt(startTime)
                .endAt(endTime)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double totalHours = 0.0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            totalHours += safeGetDouble(child, "hours");
                        }
                        callback.onFetched(new SleepSummary(totalHours));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Helper to parse a numeric child safely
    private static double safeGetDouble(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v instanceof String) {
            try {
                return Double.parseDouble((String) v);
            } catch (NumberFormatException ignored) { }
        }
        return 0.0;
    }
}
