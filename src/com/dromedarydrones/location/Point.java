package com.dromedarydrones.location;

import com.dromedarydrones.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * @author  Christian Burns
 */
public class Point implements XmlSerializable {

    private String name;
    private double latitude;
    private double longitude;

    private final Point origin;
    private int x = 0;
    private int y = 0;

    Point(String name, double latitude, double longitude, Point origin) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.origin = origin;
        refreshOrigin();
    }

    Point(Point other, Point origin) {
        this.name = other.name;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.origin = origin;
        this.x = other.x;
        this.y = other.y;
    }

    Point(Element root, Point origin) {
        this.name = root.getAttribute("name");
        this.latitude = Double.parseDouble(root.getAttribute("latitude"));
        this.longitude = Double.parseDouble(root.getAttribute("longitude"));
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
     * Method for editing the coordinates of a point
     * @param coordinates the x, y values to which the point is set
     * @author Isabella Patnode, Christian Burns
     */
    public void setCoordinates(String coordinates) {
        String cleaned = coordinates.replaceAll("[( )]", "");
        String[] values = cleaned.split(",", 2);
        try {
            int xValue = Integer.parseInt(values[0]);
            int yValue = Integer.parseInt(values[1]);
            x = xValue; // set the new x value now that we know both x and y exist
            y = yValue; // set the new y value now that we know both x and y exist
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("invalid coordinates: expected \"int,int\", found \"%s\"", coordinates));
        }
    }

    /**
     * @author Isabella Patnode
     * @return the x,y coordinates formatted as a string
     */
    public String getCoordinates() {
        return "(" + x + ", " + y + ")";
    }

    public Point getOrigin() {
        return origin;
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
    public Element toXml(Document doc) {
        Element root = doc.createElement("point");
        root.setAttribute("name", name);
        root.setAttribute("latitude", String.valueOf(latitude));
        root.setAttribute("longitude", String.valueOf(longitude));
        return root;
    }

    @Override
    public String toString() {
        return name;
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
