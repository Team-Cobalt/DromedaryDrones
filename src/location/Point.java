package location;

import java.util.Objects;

/**
 * @author Christian Burns
 */
public class Point {

    private String name;
    private double latitude;
    private double longitude;
    private Point origin;
    private int x;
    private int y;

    Point(String name, double latitude, double longitude, Point origin) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.origin = origin;
        refreshOrigin();
    }

    /* INTERNAL METHODS */

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    void refreshOrigin() {
        if (origin == null) return;
        int[] coords = GpsUtility.degreesToFeet(
                new double[]{origin.latitude, origin.longitude},
                new double[]{latitude, longitude});
        x = coords[0];
        y = coords[1];
    }

    /* PUBLIC METHODS */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the X coordinate relative to the origin.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y coordinate relative to the origin.
     */
    public int getY() {
        return y;
    }

    /**
     * Calculates the distance in feet between this point and another point.
     * If other is {@code null}, then the distance from the origin will be used.
     *
     * @param other  the other point or null
     */
    public double distanceFromPoint(Point other) {
        int xDiff = other != null ? Math.abs(getX() - other.getX()) : getX();
        int yDiff = other != null ? Math.abs(getY() - other.getY()) : getY();
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.latitude, latitude) == 0 &&
                Double.compare(point.longitude, longitude) == 0 &&
                Objects.equals(name, point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
}
