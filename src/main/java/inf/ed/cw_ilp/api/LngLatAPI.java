package inf.ed.cw_ilp.api;

import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Region;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import inf.ed.cw_ilp.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public class LngLatAPI {

    //End-point 2
    // Endpoint to calculate the Euclidean distance between two positions
    public ResponseEntity<Double> calculateDistance(Requests.LngLatPairRequest request) {
        // Validate request data and give error if wrong
        if (request == null || request.position1() == null || request.position2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            // Validate the keys of the request to ensure proper spelling
            if (!request.getClass().getDeclaredFields()[0].getName().equals("position1") ||
                    !request.getClass().getDeclaredFields()[1].getName().equals("position2")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            double lng1 = request.position1().lng();
            double lat1 = request.position1().lat();
            double lng2 = request.position2().lng();
            double lat2 = request.position2().lat();

            // Calculate the Euclidean distance
            double distance = calculateEuclideanDistance(lng1, lat1, lng2, lat2);

            // Return the distance with HTTP 200 OK else 400
            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Helper method to calculate Euclidean distance
    private double calculateEuclideanDistance(double lng1, double lat1, double lng2, double lat2) {
        return Math.sqrt(Math.pow(lng2 - lng1, 2) + Math.pow(lat2 - lat1, 2));
    }

    // End-point 3
    // Endpoint to check if two points are close by or not
    public ResponseEntity<Boolean> isCloseTo(Requests.LngLatPairRequest request) {
        // Validate request data
        if (request.position1() == null || request.position2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);  // return false for invalid input
        }

        try {
            double lng1 = request.position1().lng();
            double lat1 = request.position1().lat();
            double lng2 = request.position2().lng();
            double lat2 = request.position2().lat();

            double distance = calculateEuclideanDistance(lng1, lat1, lng2, lat2);

            return ResponseEntity.ok(distance <= 0.00015);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    // Helper method for checking if a node is close to a point.
    public boolean isCloseToPoint(Position Node, Position position) {
        double lng1 = Node.lng();
        double lat1 = Node.lat();
        double lng2 = position.lng();
        double lat2 = position.lat();

        double distance = calculateEuclideanDistance(lng1, lat1, lng2, lat2);

        return (distance <= 0.00015);
    }

    // End-point 4
    // End-point to get the next Position of the drone each move is constant 0.00015
    private static final double MOVE_DISTANCE = 0.00015;
    public ResponseEntity<Position> nextPosition(Requests.LngLatAngleRequest request) {
        if (request.start() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        double startLng = request.start().lng();
        double startLat = request.start().lat();
        double angle = request.angle();

        // Normalize angle to [0, 360)
        while (angle < 0) {
            angle += 360;
        }
        angle = angle % 360;

        if (Constants.VALID_ANGLES.contains(angle)) {
            double angleInRadians = Math.toRadians(angle);
            double deltaLng = MOVE_DISTANCE * Math.cos(angleInRadians);
            double deltaLat = MOVE_DISTANCE * Math.sin(angleInRadians);
            double newLng = startLng + deltaLng;
            double newLat = startLat + deltaLat;

            return ResponseEntity.ok(new Position(newLng, newLat)); // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    //End-point 5
    // End-point to check if a given position is within the central-area
    public boolean isInsideRegion(Position position, Region region) {
        Position[] vertices = region.vertices();
        return isPointInsidePolygon(position, vertices);
    }

    public boolean isPointInRegion(Position position, nameData.NamedRegion region) {
        if (region == null || region.getCoordinates() == null || region.getCoordinates().length == 0) {
            throw new IllegalArgumentException("Region vertices are null or empty");
        }

        Position[] vertices = region.getCoordinates();
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
