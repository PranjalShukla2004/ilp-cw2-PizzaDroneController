package inf.ed.cw_ilp.model.pathFinder;

import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.model.Regions.*;
import inf.ed.cw_ilp.utils.Constants;

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
    private final LngLatAPI lngLatAPI;

    public A_Star(Position start, Position end, nameData.NamedRegion centralArea, List<nameData.NamedRegion> noFlyZones) {
        this.start = start;
        this.end = end;
        this.centralArea = centralArea;
        this.noFlyZones = noFlyZones;
        this.lngLatAPI = new LngLatAPI();
    }

    public List<Position> calculatePath() {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Map<Position, Node> Nodes = new HashMap<>();
        boolean inCentralArea = false;

        Node startNode = new Node(start, null, 0, euclideanDistance(start, end));
        openSet.add(startNode);
        Nodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            // If current position is close to goal, reconstruct path
            if (lngLatAPI.isCloseToPoint(currentNode.getPosition(), end)) {
                return reconstructPath(currentNode);
            }

            // Update flag if drone has entered the central area
            if (!inCentralArea && lngLatAPI.isPointInRegion(currentNode.getPosition(), centralArea)) {
                inCentralArea = true;
            }

            // Check neighbors for valid move
            for (Position neighbor : getNeighbors(currentNode)) {
                if (isInsideNoFlyZone(neighbor)) continue;

                // If drone has entered the central area, restrict it to central area
                if (inCentralArea && !lngLatAPI.isPointInRegion(neighbor, centralArea)) {
                    continue;
                }

                double newG = currentNode.get_start_cost() + euclideanDistance(currentNode.getPosition(), neighbor);
                Node neighborNode = Nodes.getOrDefault(neighbor, new Node(neighbor, null, Double.MAX_VALUE, 0));

                if (newG < neighborNode.get_start_cost()) {
                    neighborNode.setParent(currentNode);
                    neighborNode.set_start_cost(newG);
                    neighborNode.set_heuristic_cost(euclideanDistance(neighbor, end));

                    openSet.add(neighborNode);
                    Nodes.put(neighbor, neighborNode);
                }
            }
        }

        return new ArrayList<>(); // No valid path found
    }

    private double euclideanDistance(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p1.lng() - p2.lng(), 2) + Math.pow(p1.lat() - p2.lat(), 2));
    }

    private List<Position> getNeighbors(Node currentNode) {
        List<Position> neighbors = new ArrayList<>();
        for (double angle : Constants.VALID_ANGLES) {
            Position nextPosition = lngLatAPI.nextPosition(new Requests.LngLatAngleRequest(currentNode.getPosition(), angle)).getBody();
            if (nextPosition != null) {
                neighbors.add(nextPosition);
            }
        }
        return neighbors;
    }

    private List<Position> reconstructPath(Node currentNode) {
        List<Position> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(currentNode.getPosition());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private boolean isInsideNoFlyZone(Position position) {
        for (nameData.NamedRegion noFlyZone : noFlyZones) {
            if (lngLatAPI.isPointInRegion(position, noFlyZone)) {
                return true;
            }
        }
        return false;
    }
}

