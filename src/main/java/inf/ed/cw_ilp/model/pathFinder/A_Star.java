package inf.ed.cw_ilp.model.pathFinder;

import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.model.Regions.*;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import inf.ed.cw_ilp.utils.Constants;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * A_Star class implements the A* algorithm to calculate delivery paths.
 * It is a very strong and fast algorithm in cases where we know the end-pt. and start point.
 * It takes into consideration no-fly zones, the central area, and restaurant coordinates to compute valid paths.
 */
public class A_Star {

    private final Position start;
    private final Position end;
    private final nameData.NamedRegion centralArea;
    private final List<nameData.NamedRegion> noFlyZones;

    /**
     * Constructs an A_Star object with the given start, end points, central area, and no-fly zones.
     *
     * @param start       The starting point (LngLat).
     * @param end         The ending point (LngLat).
     * @param centralArea The central area constraint (NamedRegion).
     * @param noFlyZones  The list of no-fly zones (NamedRegion).
     */
    public A_Star(Position start, Position end, nameData.NamedRegion centralArea, List<nameData.NamedRegion> noFlyZones) {
        this.start = start;
        this.end = end;
        this.centralArea = centralArea;
        this.noFlyZones = noFlyZones;
    }

    /**
     * Calculates the optimal path using the A* algorithm.
     * @return A list of LngLat representing the calculated path.
     */
    public List<Position> calculatePath() {
        LngLatAPI lngLatAPI = new LngLatAPI();
        PriorityQueue<Node> openSet = new PriorityQueue<>(new NodePriorityComparator());
        Map<Position, Node> visited = new HashMap<>();

        Requests.LngLatPairRequest distanceRequest = new Requests.LngLatPairRequest(
                new Position(start.lng(), start.lat()),   // position1
                new Position(end.lng(),   end.lat())      // position2
        );

        ResponseEntity<Double> response = lngLatAPI.calculateDistance(distanceRequest);
        Double dist = response.getBody();
        if (dist == null) dist = 0.0;
        Node startNode = new Node(null, start, 0, dist, Constants.HOVER_ANGLE);
        openSet.add(startNode);
        Node currentNode;

        while (!openSet.isEmpty()) {
            currentNode = openSet.poll();
            if (lngLatAPI.isCloseToPoint(currentNode.getLngLat(), end)) {
                return constructPath(currentNode);
            }
            List<Node> neighbors = getNeighbors(currentNode, lngLatAPI);
            updateNeighbors(openSet, visited, currentNode, neighbors, lngLatAPI);
        }

        // Return empty path if no valid path is found
        return Collections.emptyList();
    }

    private List<Position> constructPath(Node currentNode) {
        List<Position> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(0, currentNode.getLngLat());
            currentNode = currentNode.getParent();
        }
        return path;
    }

    private void updateNeighbors(
            PriorityQueue<Node> openSet,
            Map<Position, Node> visited,
            Node currentNode,
            List<Node> neighbors,
            LngLatAPI lngLatAPI
    ) {
        for (Node neighbor : neighbors) {
            // If position is already in visited, check if we found a smaller path
            if (visited.containsKey(neighbor.getLngLat())) {
                if (neighbor.get_net_cost() < visited.get(neighbor.getLngLat()).get_net_cost()) {
                    visited.put(neighbor.getLngLat(), neighbor);
                } else {
                    continue;
                }
            }
            Requests.LngLatPairRequest distanceRequest = new Requests.LngLatPairRequest(
                    currentNode.getLngLat(),
                    neighbor.getLngLat()
            );
            Requests.LngLatPairRequest distance = new Requests.LngLatPairRequest(currentNode.getLngLat(), neighbor.getLngLat());
            ResponseEntity<Double> distResponse = lngLatAPI.calculateDistance(distance);
            Double distBetween = distResponse.getBody();
            if (distBetween == null) {
                distBetween = 0.0;
            }

            double newG = currentNode.get_start_cost() + distBetween;

            Requests.LngLatPairRequest endRequest =
                    new Requests.LngLatPairRequest(neighbor.getLngLat(), end);

            ResponseEntity<Double> endDistResponse = lngLatAPI.calculateDistance(endRequest);
            Double newH = endDistResponse.getBody();
            if (newH == null) {
                newH = 0.0;
            }

            // Create a new node with updated info on how far the end point is...
            Node newNode = new Node(
                    currentNode,
                    neighbor.getLngLat(),
                    newG,
                    newH,
                    neighbor.getAngle()
            );

            openSet.add(newNode);
            visited.put(newNode.getLngLat(), newNode);
        }
    }

        private List<Node> getNeighbors(Node currentNode, LngLatAPI LngLatAPI) {
            List<Node> neighbors = new ArrayList<>();
            boolean currentInCentral = LngLatAPI.isPointInRegion(currentNode.getLngLat(), (List<Position>) centralArea);

            for (Double angle : Constants.VALID_ANGLES) {
                Requests.LngLatAngleRequest param = new Requests.LngLatAngleRequest(currentNode.getLngLat(), angle);
                ResponseEntity<Position> response = LngLatAPI.nextPosition(param);
                Position nextPosition = response.getBody();
                boolean nextInCentral = LngLatAPI.isPointInRegion(nextPosition, (List<Position>) centralArea);

                Requests.LngLatPairRequest distRequest1 = new Requests.LngLatPairRequest(
                        currentNode.getLngLat(),
                        nextPosition
                );

                Requests.LngLatPairRequest distRequest2 = new Requests.LngLatPairRequest(
                        nextPosition,
                        end
                );

                ResponseEntity<Double> distResponse1 = LngLatAPI.calculateDistance(distRequest1);
                Double distance1 = distResponse1.getBody();
                if (distance1 == null) {
                    distance1 = 0.0;
                }

                ResponseEntity<Double> distResponse2 = LngLatAPI.calculateDistance(distRequest2);
                Double distance2 = distResponse2.getBody();
                if (distance2 == null) {
                    distance2 = 0.0;
                }


                if (isValidMove(currentInCentral, nextPosition, nextInCentral, LngLatAPI)) {
                    neighbors.add(new Node(
                            currentNode,
                            nextPosition,
                            currentNode.get_start_cost() + distance1,
                            distance2,
                            angle
                    ));
                }
            }

            return neighbors;
        }

        private boolean isValidMove(boolean currentInCentral, Position nextPosition, boolean nextInCentral, LngLatAPI LngLatAPI) {
            if (!currentInCentral && nextInCentral) {
                return false;
            }
            for (nameData.NamedRegion noFlyZone : noFlyZones) {
                if (LngLatAPI.isPointInRegion(nextPosition, (List<Position>) noFlyZone)) {
                    return false;
                }
            }
            return true;
        }

    private static class NodePriorityComparator implements Comparator<Node> {
        @Override
        public int compare(Node node1, Node node2) {
            return Double.compare(node1.get_net_cost(), node2.get_net_cost());
        }
    }
}
