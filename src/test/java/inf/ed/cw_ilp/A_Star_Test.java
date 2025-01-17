package inf.ed.cw_ilp;

import static org.junit.jupiter.api.Assertions.*;

import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.pathFinder.A_Star;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

public class A_Star_Test {

    private Position start;
    private Position end;
    private nameData.NamedRegion centralArea;
    private List<nameData.NamedRegion> noFlyZones;
    private A_Star aStar;

    @BeforeEach
    public void setup() {

        start = new Position(0, 0);
        end = new Position(10, 10);

        // Define central area
        centralArea = new nameData.NamedRegion("Central", new Position[]{
                new Position(2, 2),
                new Position(8, 2),
                new Position(8, 8),
                new Position(2, 8)
        });

        // Define no-fly zones
        noFlyZones = new ArrayList<>();
        noFlyZones.add(new nameData.NamedRegion("NoFlyZone1", new Position[]{
                new Position(4, 4),
                new Position(6, 4),
                new Position(6, 6),
                new Position(4, 6)
        }));

        aStar = new A_Star(start, end, centralArea, noFlyZones);
    }

    @Test
    public void testPathExistsWithoutNoFlyZones() {
        // Simple test with no obstacles
        noFlyZones.clear();
        aStar = new A_Star(start, end, centralArea, noFlyZones);

        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");
        assertFalse(path.isEmpty(), "Path should not be empty.");
        assertEquals(start, path.get(0), "Path should start at the start position.");
        assertEquals(end, path.get(path.size() - 1), "Path should end at the end position.");
    }

    @Test
    public void testPathAvoidsNoFlyZones() {
        // Path should avoid no-fly zones
        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");
        assertFalse(path.isEmpty(), "Path should not be empty.");

        for (Position position : path) {
            for (nameData.NamedRegion noFlyZone : noFlyZones) {
                assertFalse(isInsideRegion(position, noFlyZone), "Path should not enter no-fly zones.");
            }
        }
    }

    @Test
    public void testPathStaysInCentralArea() {
        // Path should stay within the central area if entered
        aStar = new A_Star(start, new Position(5, 5), centralArea, noFlyZones);

        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");

        boolean enteredCentralArea = false;
        for (Position position : path) {
            if (isInsideRegion(position, centralArea)) {
                enteredCentralArea = true;
            }
            if (enteredCentralArea) {
                assertTrue(isInsideRegion(position, centralArea), "Once inside the central area, the path should not leave it.");
            }
        }
    }

    @Test
    public void testNoPathExists() {
        // Block all possible paths
        noFlyZones.add(new nameData.NamedRegion("NoFlyZone2", new Position[]{
                new Position(0, 0),
                new Position(10, 0),
                new Position(10, 10),
                new Position(0, 10)
        }));

        List<Position> path = aStar.calculatePath();
        assertTrue(path.isEmpty(), "Path should be empty when no valid path exists.");
    }

    @Test
    public void testDirectPathWhenNoObstacles() {
        // Test direct path when no obstacles are present
        noFlyZones.clear();
        centralArea = new nameData.NamedRegion("Central", new Position[]{});
        aStar = new A_Star(start, end, centralArea, noFlyZones);

        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");
        assertTrue(path.size() > 1, "Path should have multiple positions.");
        assertEquals(start, path.get(0), "Path should start at the start position.");
        assertEquals(end, path.get(path.size() - 1), "Path should end at the end position.");
    }

    @Test
    public void testPathWithMultipleNoFlyZones() {
        // Add additional no-fly zones and test path avoidance
        noFlyZones.add(new nameData.NamedRegion("NoFlyZone2", new Position[]{
                new Position(7, 7),
                new Position(9, 7),
                new Position(9, 9),
                new Position(7, 9)
        }));

        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");
        for (Position position : path) {
            for (nameData.NamedRegion noFlyZone : noFlyZones) {
                assertFalse(isInsideRegion(position, noFlyZone), "Path should avoid all no-fly zones.");
            }
        }
    }

    @Test
    public void testPathWithStartAndEndInsideCentralArea() {
        // Test when start and end points are inside the central area
        start = new Position(3, 3);
        end = new Position(7, 7);
        aStar = new A_Star(start, end, centralArea, noFlyZones);

        List<Position> path = aStar.calculatePath();
        assertNotNull(path, "Path should not be null.");
        assertFalse(path.isEmpty(), "Path should not be empty.");
        for (Position position : path) {
            assertTrue(isInsideRegion(position, centralArea), "Path should remain within the central area.");
        }
    }


    private boolean isInsideRegion(Position position, nameData.NamedRegion region) {
        LngLatAPI api = new LngLatAPI();
        return api.isPointInRegion(position, region);
    }
}
