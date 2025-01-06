package inf.ed.cw_ilp.model.pathFinder;

import inf.ed.cw_ilp.model.Regions.*;
import inf.ed.cw_ilp.model.pathFinder.nameData;

import java.util.*;

/**
 * A_Star class implements the A* algorithm to calculate delivery paths.
 * It uses no-fly zones, the central area, and restaurant coordinates to compute valid paths.
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
        LngLatHandler lngLatHandler = new LngLatHandler();
        PriorityQueue<Node> openSet = new PriorityQueue<>(new NodePriorityComparator());
        Map<LngLat, Node> visited = new HashMap<>();

        Node startNode = new Node(null, start, 0, lngLatHandler.distanceTo(start, end), Constants.HOVER_ANGLE);
        openSet.add(startNode);

        Node currentNode;

        while (!openSet.isEmpty()) {
            currentNode = openSet.poll();
            if (lngLatHandler.isCloseTo(currentNode.getLngLat(), end)) {
                return constructPath(currentNode);
            }
            List<Node> neighbors = getNeighbors(currentNode, lngLatHandler);
            updateNeighbors(openSet, visited, currentNode, neighbors, lngLatHandler);
        }

        return Collections.emptyList(); // Return empty path if no valid path is found
    }

    private List<LngLat> constructPath(Node currentNode) {
        List<LngLat> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(0, currentNode.getLngLat());
            currentNode = currentNode.getParent();
        }
        return path;
    }

    private void updateNeighbors(PriorityQueue<Node> openSet, Map<LngLat, Node> visited, Node currentNode, List<Node> neighbors, LngLatHandler lngLatHandler) {
        for (Node neighbor : neighbors) {
            if (visited.containsKey(neighbor.getLngLat())) {
                if (neighbor.getF() < visited.get(neighbor.getLngLat()).getF()) {
                    visited.put(neighbor.getLngLat(), neighbor);
                } else {
                    continue;
                }
            }

            Node newNode = new Node(
                    currentNode,
                    neighbor.getLngLat(),
                    currentNode.getG() + lngLatHandler.distanceTo(currentNode.getLngLat(), neighbor.getLngLat()),
                    lngLatHandler.distanceTo(neighbor.getLngLat(), end),
                    neighbor.getAngle()
            );
            openSet.add(newNode);
            visited.put(newNode.getLngLat(), newNode);
        }
    }

    private List<Node> getNeighbors(Node currentNode, LngLatHandler lngLatHandler) {
        List<Node> neighbors = new ArrayList<>();
        boolean currentInCentral = lngLatHandler.isInRegion(currentNode.getLngLat(), centralArea);

        for (Double angle : Constants.VALID_ANGLES) {
            LngLat nextPosition = lngLatHandler.nextPosition(currentNode.getLngLat(), angle);
            boolean nextInCentral = lngLatHandler.isInRegion(nextPosition, centralArea);

            if (isValidMove(currentInCentral, nextPosition, nextInCentral, lngLatHandler)) {
                neighbors.add(new Node(
                        currentNode,
                        nextPosition,
                        currentNode.getG() + lngLatHandler.distanceTo(currentNode.getLngLat(), nextPosition),
                        lngLatHandler.distanceTo(nextPosition, end),
                        angle
                ));
            }
        }

        return neighbors;
    }

    private boolean isValidMove(boolean currentInCentral, LngLat nextPosition, boolean nextInCentral, LngLatHandler lngLatHandler) {
        if (!currentInCentral && nextInCentral) {
            return false;
        }
        for (NamedRegion noFlyZone : noFlyZones) {
            if (lngLatHandler.isInRegion(nextPosition, noFlyZone)) {
                return false;
            }
        }
        return true;
    }

    private static class NodePriorityComparator implements Comparator<Node> {
        @Override
        public int compare(Node node1, Node node2) {
            return Double.compare(node1.getF(), node2.getF());
        }
    }
}
