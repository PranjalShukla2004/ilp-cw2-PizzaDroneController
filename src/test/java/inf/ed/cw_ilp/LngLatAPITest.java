package inf.ed.cw_ilp;

import static org.junit.jupiter.api.Assertions.*;

import inf.ed.cw_ilp.api.LngLatAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        assertTrue(lngLatAPI.isInsideRegion(point, region), "Point should be inside the region");
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

        assertFalse(lngLatAPI.isInsideRegion(point, region), "Point should be outside the region");
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
}
