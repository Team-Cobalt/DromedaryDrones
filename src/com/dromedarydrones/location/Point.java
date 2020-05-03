package com.dromedarydrones.location;

import com.dromedarydrones.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * @author  Christian Burns
 */
public class Point implements XmlSerializable {

    private String name;    // name of this delivery location
    private int x;          // latitudinal offset from origin in feet
    private int y;          // longitudinal offset from origin in feet

    /**
     * Constructs a new delivery point from the given name and coordinates.
     * @author Christian Burns
     * @param name       name of the location
     * @param xPosition  latitudinal offset from origin in feet
     * @param yPosition  longitudinal offset from origin in feet
     * @throws IllegalArgumentException if name is null
     */
    public Point(String name, int xPosition, int yPosition) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Point name cannot be null.");

        this.name = name;
        x = xPosition;
        y = yPosition;
    }

    /**
     * Copy constructor to duplicate an existing point.
     * @param other  an existing point to copy
     */
    public Point(Point other) {
        this.name = other.name;
        this.x = other.x;
        this.y = other.y;
    }

    public Point(Element root) {
        name = root.getAttribute("name");
        x = Integer.parseInt(root.getAttribute("x"));
        y = Integer.parseInt(root.getAttribute("y"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Point name cannot be null.");

        this.name = name;
    }

    /**
     * Returns the latitudinal offset from origin in feet
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the latitudinal offset from origin in feet
     * @param x  offset in feet
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the longitudinal offset from the origin in feet
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the longitudinal offset from origin in feet
     * @param y  offset in feet
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Method for editing the coordinates of a point
     * @param coordinates the x, y values to which the point is set
     * @author Isabella Patnode, Christian Burns
     */
    public void setCoordinates(String coordinates) throws IllegalArgumentException {
        String cleaned = coordinates.replaceAll("[( )]", "");
        String[] values = cleaned.split(",", 2);
        try {
            int xValue = Integer.parseInt(values[0]);
            int yValue = Integer.parseInt(values[1]);
            x = xValue; // set the new x value now that we know both x and y exist
            y = yValue; // set the new y value now that we know both x and y exist
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid coordinates: expected \"int,int\", found \"%s\".", coordinates));
        }
    }

    /**
     * @author Isabella Patnode
     * @return the x,y coordinates formatted as a string
     */
    public String getCoordinates() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Calculates the distance in feet between this point and another point.
     * If other is {@code null}, then the distance from the origin will be used.
     *
     * @param other  the other point or null
     */
    public double distanceFromPoint(Point other) {
        int xDiff = other != null ? x - other.x : x;
        int yDiff = other != null ? y - other.y : y;
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }

    @Override
    public Element toXml(Document doc) {
        Element root = doc.createElement("point");
        root.setAttribute("name", name);
        root.setAttribute("x", String.valueOf(x));
        root.setAttribute("y", String.valueOf(y));
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
        return point.x == x &&
                point.y == y &&
                Objects.equals(name, point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, x, y);
    }
}
