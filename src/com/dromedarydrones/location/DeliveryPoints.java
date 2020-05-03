package com.dromedarydrones.location;

import com.dromedarydrones.xml.XmlSerializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Class to keep track of all delivery points and their relative locations.
 * @author Christian Burns
 */
public class DeliveryPoints implements Iterable<Point>, XmlSerializable {

    private final ArrayList<Point> points;
    private final Random random;

    public DeliveryPoints() {
        points = new ArrayList<>();
        random = new Random();
        _tmpLoadPoints();
    }

    public DeliveryPoints(DeliveryPoints other) throws IllegalArgumentException {
        if(other == null)
            throw new IllegalArgumentException("Given DeliveryPoints object cannot be null");

        points = new ArrayList<>();
        random = new Random();
        for (Point point : other) points.add(new Point(point));
    }

    public DeliveryPoints(Element root) {
        points = new ArrayList<>();
        random = new Random();
        NodeList children = root.getElementsByTagName("point");
        for (int index = 0; index < children.getLength(); index++) {
            Point point = new Point((Element) children.item(index));
            points.add(point);
        }
    }

    private void _tmpLoadPoints() {
        addPoint("Student Union", 0, 0);
        addPoint("Hall of Arts and Letters", 46, -121);
        addPoint("Physical Learning Center", -106, 115);
        addPoint("Technological Learning Center", -419, -487);
        addPoint("Pew Fine Arts Center", 27, -784);
        addPoint("Hoyt Hall of Engineering", -318, -98);
        addPoint("STEM", -303, 78);
        addPoint("Hicks", -272, -542);
        addPoint("Zerbe", -964, -239);
        addPoint("Ketler", -781, 164);
        addPoint("Library", -494, -263);
        addPoint("Lincoln", -781, -150);
        addPoint("Hopeman", -728, -311);
        addPoint("Memorial", -1143, 4);
        addPoint("Crawford", -1081, 298);
        addPoint("Rockwell", -492, 187);
        addPoint("Rathburn", -687, 774);
        addPoint("Harbison Chapel", -870, 588);
        addPoint("PLC Roundabout", -188, 331);
        addPoint("Thorn Field", -1748, 923);
        addPoint("Tennis Court", -1905, 1070);
        addPoint("Soccer Field", -91, 1010);
        addPoint("Random Field", -1514, 708);
        addPoint("Baseball Field", -494, 1137);
        addPoint("President's House", -1301, -122);
        addPoint("Helen Harker Residence Hall", -401, 319);
        addPoint("MEP Residence Hall", -263, 600);
        addPoint("MAP Residence Hall", -505, 649);
    }

    public Point addPoint(String name, int xPos, int yPos) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException("Point name cannot be null.");

        Point newPoint = new Point(name, xPos, yPos);
        points.add(newPoint);
        return newPoint;
    }

    public void removePoint(Point point) {
        points.remove(point);
    }

    /**
     * Returns a random known {@link Point} or {@code null} if no points were added.
     */
    public Point getRandomPoint() {
        if (points.size() == 0) return null;
        return points.get(random.nextInt(points.size()));
    }

    /**
     * Makes the current simulation's list of delivery points into a list for javafx
     * @author Izzy Patnode
     * @return a list of points for javafx
     */
    public ObservableList<Point> getPoints() {
        return FXCollections.observableList(points);
    }

    /**
     * Returns an iterator over elements of type {@code Point}.
     * @return an Iterator.
     */
    @Override
    public Iterator<Point> iterator() {
        return points.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (Point p : this)
     *         action.accept(p);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(Consumer<? super Point> action) {
        points.forEach(action);
    }

    @Override
    public Element toXml(Document document) {
        Element root = document.createElement("deliverypoints");
        for (Point point : points) root.appendChild(point.toXml(document));
        return root;
    }
}
