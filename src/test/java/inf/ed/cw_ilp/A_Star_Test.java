package inf.ed.cw_ilp;

import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.pathFinder.A_Star;
import inf.ed.cw_ilp.model.pathFinder.Node;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class A_Star_Test {

    @Test
    public void testValidPath() {
        // Setup mock LngLatAPI
        LngLatAPI lngLatAPI = mock(LngLatAPI.class);

        // Mock responses for next position and distance calculation
        when(lngLatAPI.nextPosition(any())).thenReturn(new Position(-3.19128692150116, 55.9455351525177));
        when(lngLatAPI.calculateDistance(any())).thenReturn(new ResponseEntity<>(100.0, HttpStatus.OK));

        // Setup sample data
        Position start = new Position(-3.192473, 55.946233);
        Position end = new Position(-3.188476, 55.943389);
        nameData.NamedRegion centralArea = new nameData.NamedRegion("central", new Position[]{start, end});
        List<nameData.NamedRegion> noFlyZones = List.of();

        A_Star aStar = new A_Star(start, end, centralArea, noFlyZones);
        List<Position> path = aStar.calculatePath();

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(path.get(0), start);
        assertEquals(path.get(path.size() - 1), end);
    }

    @Test
    public void testNoPathFound() {
        // Setup mock LngLatAPI
        LngLatAPI lngLatAPI = mock(LngLatAPI.class);

        // Mock responses for next position and distance calculation
        when(lngLatAPI.nextPosition(any())).thenReturn(new Position(-3.19128692150116, 55.9455351525177));
        when(lngLatAPI.calculateDistance(any())).thenReturn(new ResponseEntity<>(100.0, HttpStatus.OK));

        // Setup sample data with an unreachable end point
        Position start = new Position(-3.192473, 55.946233);
        Position end = new Position(-10.000000, 55.946233);  // Out of bounds
        nameData.NamedRegion centralArea = new nameData.NamedRegion("central", new Position[]{start, end});
        List<nameData.NamedRegion> noFlyZones = List.of();

        A_Star aStar = new A_Star(start, end, centralArea, noFlyZones);
        List<Position> path = aStar.calculatePath();

        assertTrue(path.isEmpty());
    }

    @Test
    public void testIsValidMoveWithNoFlyZone() {
        // Setup mock LngLatAPI
        LngLatAPI lngLatAPI = mock(LngLatAPI.class);

        // Setup sample data
        Position start = new Position(-3.192473, 55.946233);
        Position nextPosition = new Position(-3.19128692150116, 55.9455351525177);

        nameData.NamedRegion centralArea = new nameData.NamedRegion("central", new Position[]{start, nextPosition});
        List<nameData.NamedRegion> noFlyZones = List.of(new nameData.NamedRegion("noFlyZone", new Position[] {
                new Position(-3.19128692150116, 55.9455351525177),
                new Position(-3.190000, 55.944000)
        }));

        A_Star aStar = new A_Star(start, nextPosition, centralArea, noFlyZones);

        // Test if the next position is valid move considering no-fly zones
        when(lngLatAPI.isPointInRegion(nextPosition, noFlyZones.get(0))).thenReturn(true);

        boolean validMove = aStar.isValidMove(true, nextPosition, true, lngLatAPI);
        assertFalse(validMove);  // The move should be invalid due to no-fly zone
    }

    @Test
    public void testNodePriorityComparator() {
        // Setup mock nodes with different net cost
        // Ensure the Position constructor expects lng and lat
        Position position1 = new Position(-3.19128692150116, 55.9455351525177);
        Position position2 = new Position(-3.188476, 55.943389);

        // Initialize nodes with the correct Position objects
        Node node1 = new Node(null, position1, 5, 10, 0.0);
        Node node2 = new Node(null, position2, 3, 7, 0.0);

        A_Star.NodePriorityComparator comparator = new A_Star.NodePriorityComparator();

        // Test the comparator behavior
        assertTrue(comparator.compare(node1, node2) > 0);
    }



    @Test
    public void testConstructPath() {
        // Setup mock nodes
        Position start = new Position(-3.192473, 55.946233);
        Position end = new Position(-3.188476, 55.943389);
        Node node1 = new Node(null, start, 0, 10, 0.0);
        Node node2 = new Node(node1, end, 10, 5, 0.0);

        A_Star aStar = new A_Star(start, end, new nameData.NamedRegion("central", new Position[]{start, end}), List.of());

        // Construct the path
        List<Position> path = aStar.constructPath(node2);

        // Check the expected path order
        assertEquals(2, path.size());
        assertEquals(start, path.get(0));
        assertEquals(end, path.get(1));
    }
}
