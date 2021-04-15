package app.data;

import java.util.Arrays;

/**
 * Helper data structure that a request body for the /api/interaction routes are decoded into
 */
public class InteractionRequest {
    public final String[] names;
    public final long datetime;
    public final int duration;
    public final boolean document;


    public InteractionRequest(String[] names, long datetime, int duration, boolean document) {
        this.names = names;
        this.datetime = datetime;
        this.duration = duration;
        this.document = document;
    }

    @Override
    public boolean equals(Object data) {
        if (!(data instanceof InteractionRequest)) {
            return false;
        }
        InteractionRequest iData = (InteractionRequest) data;

        return Arrays.equals(this.names, iData.names) && this.datetime == iData.datetime && this.duration == iData.duration;
    }
}
