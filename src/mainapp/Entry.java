package mainapp;

/**
 * Single entry holding an elapsed duration and the number of deliveries.
 * @author  Christian Burns
 */
public class Entry implements Comparable<Entry> {
    public int elapsedTime;     // time the order had to wait to be delivered
    public int deliveryCount;   // number of orders that had the same wait time
    public Entry(int elapsed, int count) {
        elapsedTime = elapsed;
        deliveryCount = count;
    }
    @Override
    public int compareTo(Entry other) {
        return Integer.compare(elapsedTime, other.elapsedTime);
    }
}
