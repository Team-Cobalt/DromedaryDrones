package location;

/**
 * Utility functions for manipulating GPS coordinates.
 *
 * @author Christian Burns
 */
public class GpsUtility {

    /**
     * Converts a latitude in DMS format (41°09'18.6"N) into
     * its decimal degrees representation. (41.155152)
     */
    public static double latitudeToDegrees(int degrees, int minutes, double seconds, boolean north) {
        return (degrees + (minutes * 60 + seconds) / 3600) * (north ? 1 : -1);
    }

    /**
     * Converts a longitude in DMS format (80°04'43.0"W) into
     * its decimal degrees representation. (-80.078614)
     */
    public static double longitudeToDegrees(int degrees, int minutes, double seconds, boolean east) {
        return (degrees + (minutes * 60 + seconds) / 3600) * (east ? 1 : -1);
    }

    /**
     * Calculates the relative position of one coordinate to another in feet.<p>
     *
     * The returned int array will contain the [x,y] coordinates of the destination
     * in feet assuming the origin is centered at [0,0]. Positive X values indicate
     * North of the origin, and positive Y values indicate East of the origin.<p>
     *
     * A return value of [5,-10] would indicate the destination is 5 feet North and
     * 10 feet West of the origin.
     *
     * @param origin       first coordinate {latitude, longitude}
     * @param destination  second coordinate {latitude, longitude}
     * @return             relative position in feet {x, y}
     */
    public static int[] degreesToFeet(double[] origin, double[] destination) {
        if (origin.length < 2) throw new IllegalArgumentException(
                "invalid origin argument length: expected length 2, found length " + origin.length);
        if (destination.length < 2) throw new IllegalArgumentException(
                "invalid destination argument length: expected length 2, found length " + destination.length);
        double lonMiles = distanceInMiles(origin, new double[]{origin[0], destination[1]});
        double latMiles = distanceInMiles(origin, new double[]{destination[0], origin[1]});
        return new int[]{
                (int) Math.round(origin[1] > destination[1] ? -1 * lonMiles * 5280 : lonMiles * 5280),
                (int) Math.round(origin[0] > destination[0] ? -1 * latMiles * 5280 : latMiles * 5280)};
    }

    /**
     * Calculates the distance between two coordinates in miles assuming a spherical earth.
     *
     * @param p1  first coordinate {latitude, longitude}
     * @param p2  second coordinate {latitude, longitude}
     * @return    distance in miles
     */
    public static double distanceInMiles(double[] p1, double[] p2) {
        if (p1.length < 2) throw new IllegalArgumentException(
                "invalid p1 argument length: expected length 2, found length " + p1.length);
        if (p2.length < 2) throw new IllegalArgumentException(
                "invalid p2 argument length: expected length 2, found length " + p2.length);
        double a = p1[0]/57.29577951, b = p1[1]/57.29577951;
        double c = p2[0]/57.29577951, d = p2[1]/57.29577951;
        double e = Math.sin(a) * Math.sin(c) + Math.cos(a) * Math.cos(c) * Math.cos(b - d);
        return 3963.1 * Math.acos(e > 1 ? 1 : e);
    }
}
