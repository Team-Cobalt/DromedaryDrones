package location;

import xml.annotations.XmlAttribute;
import xml.annotations.XmlElementList;
import xml.annotations.XmlSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Class to keep track of all delivery points and their relative locations.
 * @author Christian Burns
 */
@XmlSerializable
public class DeliveryPoints implements Iterable<Point> {

    @XmlAttribute
    private Point origin;

    @XmlElementList(embed=false)
    private ArrayList<Point> points;

    private Random rand;

    public DeliveryPoints() {
        points = new ArrayList<>();
        rand = new Random();
        origin = new Point("", 0, 0, null);
        _tmpLoadPoints(); //TODO: remove this when points are loaded in from a file
    }

    public DeliveryPoints(DeliveryPoints other) {
        points = new ArrayList<>();
        rand = new Random();
        origin = new Point("", 0, 0, null);
        for (Point pt : other) points.add(new Point(pt, origin));
        setOrigin(other.origin.getName());
    }

    private void _tmpLoadPoints() {
        addPoint("Student Union", 41.155052, -80.077733);
        addPoint("Hall of Arts and Letters", 41.154720, -80.077565);
        addPoint("Physical Learning Center", 41.155366, -80.078120);
        addPoint("Technological Learning Center", 41.153719, -80.079257);
        addPoint("Pew Fine Arts Center", 41.152904, -80.077634);
        addPoint("Hoyt Hall of Engineering", 41.154783, -80.078889);
        addPoint("STEM", 41.155266, -80.078835);
        addPoint("Hicks", 41.153567, -80.078721);
        addPoint("Zerbe", 41.154398, -80.081240);
        addPoint("Ketler", 41.155500, -80.080575);
        addPoint("Library", 41.154333, -80.079528);
        addPoint("Lincoln", 41.154640, -80.080575);
        addPoint("Hopeman", 41.154200, -80.080382);
        addPoint("Memorial", 41.155064, -80.081889);
        addPoint("Crawford", 41.155868, -80.081664);
        addPoint("Rockwell", 41.155564, -80.079522);
        addPoint("Rathburn", 41.157172, -80.080232);
        addPoint("Harbison Chapel", 41.156663, -80.080897);
        addPoint("Physical Learning Center Roundabout", 41.155958, -80.078417);
        addPoint("Thorn Field", 41.157580, -80.084088);
        addPoint("Tennis Court", 41.157983, -80.084659);
        addPoint("Soccer Field", 41.157817, -80.078064);
        addPoint("Random Field", 41.156990, -80.083239);
        addPoint("Baseball Field", 41.158164, -80.079528);
        addPoint("President's House", 41.154717, -80.082463);
        addPoint("Helen Harker Residence Hall", 41.155926, -80.079190);
        addPoint("Mary Ethel Pew Residence Hall", 41.156695, -80.078691);
        addPoint("Mary Anderson Pew Residence Hall", 41.156829, -80.079570);
        setOrigin("Student Union");
    }

    /**
     * Sets all points's coordinates relative to the location of the specified point.
     * @param name  name of the new origin point
     */
    public void setOrigin(String name) {
        for (Point p : points) {
            if (p.getName().equals(name)) {
                origin.setLatitude(p.getLatitude());
                origin.setLongitude(p.getLongitude());
                origin.setName(name);
                points.forEach(Point::refreshOrigin);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown point \"" + name + "\"");
    }

    public void addPoint(String name, double latitude, double longitude) {
        points.add(new Point(name, latitude, longitude, origin));
    }

    /**
     * Returns a random known {@link Point} or {@code null} if no points were added.
     */
    public Point getRandomPoint() {
        if (points.size() == 0) return null;
        return points.get(rand.nextInt(points.size()));
    }

    /**
     * Returns an iterator over elements of type {@code Point}.
     *
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
}
