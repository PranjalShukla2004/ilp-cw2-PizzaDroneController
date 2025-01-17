package inf.ed.cw_ilp;

import static org.junit.jupiter.api.Assertions.*;

import inf.ed.cw_ilp.api.LngLatAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import inf.ed.cw_ilp.model.Regions.Region;
import java.util.Arrays;

public class LngLatAPITest {

    private LngLatAPI lngLatAPI;

    @BeforeEach
    public void setup() {
        lngLatAPI = new LngLatAPI();
    }

    @Test
    public void testCalculateDistanceValidRequest() {
        Position position1 = new Position(0, 0);
        Position position2 = new Position(3, 4);
        Requests.LngLatPairRequest request = new Requests.LngLatPairRequest(position1, position2);

        ResponseEntity<Double> response = lngLatAPI.calculateDistance(request);

        assertEquals(200, response.getStatusCodeValue(), "Should return HTTP 200");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(5.0, response.getBody(), 0.0001, "Distance should be correct");
    }

    @Test
    public void testCalculateDistanceInvalidRequest() {
        Requests.LngLatPairRequest request = new Requests.LngLatPairRequest(null, null);

        ResponseEntity<Double> response = lngLatAPI.calculateDistance(request);

        assertEquals(400, response.getStatusCodeValue(), "Should return HTTP 400 for invalid request");
        assertNull(response.getBody(), "Response body should be null for invalid request");
    }

    @Test
    public void testIsCloseToValidRequestTrue() {
        Position position1 = new Position(0, 0);
        Position position2 = new Position(0.0001, 0.0001);
        Requests.LngLatPairRequest request = new Requests.LngLatPairRequest(position1, position2);

        ResponseEntity<Boolean> response = lngLatAPI.isCloseTo(request);

        assertEquals(200, response.getStatusCodeValue(), "Should return HTTP 200");
        assertTrue(response.getBody(), "Points should be close");
    }

    @Test
    public void testIsCloseToValidRequestFalse() {
        Position position1 = new Position(0, 0);
        Position position2 = new Position(1, 1);
        Requests.LngLatPairRequest request = new Requests.LngLatPairRequest(position1, position2);

        ResponseEntity<Boolean> response = lngLatAPI.isCloseTo(request);

        assertEquals(200, response.getStatusCodeValue(), "Should return HTTP 200");
        assertFalse(response.getBody(), "Points should not be close");
    }

    @Test
    public void testNextPositionValidAngle() {
        Position start = new Position(0, 0);
        Requests.LngLatAngleRequest request = new Requests.LngLatAngleRequest(start, 0);

        ResponseEntity<Position> response = lngLatAPI.nextPosition(request);

        assertEquals(200, response.getStatusCodeValue(), "Should return HTTP 200");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(0.00015, response.getBody().lng(), 0.00001, "Longitude should be correct");
        assertEquals(0, response.getBody().lat(), 0.00001, "Latitude should be correct");
    }

    @Test
    public void testNextPositionInvalidAngle() {
        Position start = new Position(0, 0);
        Requests.LngLatAngleRequest request = new Requests.LngLatAngleRequest(start, 45);

        ResponseEntity<Position> response = lngLatAPI.nextPosition(request);

        assertEquals(400, response.getStatusCodeValue(), "Should return HTTP 400 for invalid angle");
    }

    @Test
    public void testIsInsideRegionPointInside() {
        Position[] vertices = {
                new Position(0, 0),
                new Position(4, 0),
                new Position(4, 4),
                new Position(0, 4)
        };
        Region region = new Region("TestRegion", vertices);
        Position point = new Position(2, 2);

        assertTrue(isInsideTestRegion(point, region), "Point should be inside the region");
    }

    @Test
    public void testIsInsideRegionPointOutside() {
        Position[] vertices = {
                new Position(0, 0),
                new Position(4, 0),
                new Position(4, 4),
                new Position(0, 4)
        };
        Region region = new Region("TestRegion", vertices);
        Position point = new Position(5, 5);

        assertFalse(isInsideTestRegion(point, region), "Point should be outside the region");
    }

    @Test
    public void testIsPointInRegionNamedRegion() {
        Position[] vertices = {
                new Position(0, 0),
                new Position(4, 0),
                new Position(4, 4),
                new Position(0, 4)
        };
        nameData.NamedRegion namedRegion = new nameData.NamedRegion("TestNamedRegion", vertices);
        Position point = new Position(2, 2);

        assertTrue(lngLatAPI.isPointInRegion(point, namedRegion), "Point should be inside the named region");
    }

    @Test
    public void testIsPointInRegionNamedRegionOutside() {
        Position[] vertices = {
                new Position(0, 0),
                new Position(4, 0),
                new Position(4, 4),
                new Position(0, 4)
        };
        nameData.NamedRegion namedRegion = new nameData.NamedRegion("TestNamedRegion", vertices);
        Position point = new Position(5, 5);

        assertFalse(lngLatAPI.isPointInRegion(point, namedRegion), "Point should be outside the named region");
    }

    public boolean isInsideTestRegion(Position position, Region region) {
        Position[] vertices = region.vertices();
        return isPointInsidePolygon(position, vertices);
    }

    private boolean isPointInsidePolygon(Position position, Position[] vertices) {
        int count = 0;

        // Check if the point is on any edge first
        for (int i = 0; i < vertices.length; i++) {
            Position currentVertex = vertices[i];
            Position nextVertex = vertices[(i + 1) % vertices.length];

            // Check if the point is on the edge
            if (isPointOnEdge(position, currentVertex, nextVertex)) {
                return true;
            }
        }

        // Ray-casting algorithm for point inside polygon
        for (int i = 0; i < vertices.length; i++) {
            Position currentVertex = vertices[i];
            Position nextVertex = vertices[(i + 1) % vertices.length];

            if (isPointInsideRay(position, currentVertex, nextVertex)) {
                count++;
            }
        }
        return count % 2 == 1;  // If odd, point is inside; if even, point is outside
    }

    private boolean isPointOnEdge(Position position, Position currentVertex, Position nextVertex) {
        // Check if the point is on the horizontal edge

        boolean isHorizontal = currentVertex.lat() == nextVertex.lat();
        boolean isVertical = currentVertex.lng() == nextVertex.lng();

        if (isHorizontal) {
            // If the edge is horizontal, check if the point's latitude matches and its longitude is between the edge's bounds
            return position.lat() == currentVertex.lat() &&
                    position.lng() >= Math.min(currentVertex.lng(), nextVertex.lng()) &&
                    position.lng() <= Math.max(currentVertex.lng(), nextVertex.lng());
        }

        if (isVertical) {
            // If the edge is vertical, check if the point's longitude matches and its latitude is between the edge's bounds
            return position.lng() == currentVertex.lng() &&
                    position.lat() >= Math.min(currentVertex.lat(), nextVertex.lat()) &&
                    position.lat() <= Math.max(currentVertex.lat(), nextVertex.lat());
        }

        // General case: non-horizontal and non-vertical edge, check the slope of the line
        boolean firstCondition = position.lng() > Math.min(currentVertex.lng(), nextVertex.lng());
        boolean secondCondition = position.lng() <= Math.max(currentVertex.lng(), nextVertex.lng());
        if (firstCondition && secondCondition) {
            // Calculate the slope of the line between current and next vertex
            double slope = (nextVertex.lat() - currentVertex.lat()) / (nextVertex.lng() - currentVertex.lng());
            double latitude = slope * (position.lng() - currentVertex.lng()) + currentVertex.lat();
            if (position.lat() == latitude) {
                return true;
            }
        }

        return false;
    }


    private boolean isPointInsideRay(Position position, Position currentVertex, Position nextVertex) {
        boolean latitudeCondition = position.lat() <= Math.max(nextVertex.lat(), currentVertex.lat()) &&
                position.lat() > Math.min(nextVertex.lat(), currentVertex.lat());

        double latitudeFraction = ((position.lat() - currentVertex.lat()) / (nextVertex.lat() - currentVertex.lat())) * (nextVertex.lng() - currentVertex.lng());
        boolean longitudeCondition = position.lng() < (currentVertex.lng() + latitudeFraction);

        return longitudeCondition && latitudeCondition;
    }

}
