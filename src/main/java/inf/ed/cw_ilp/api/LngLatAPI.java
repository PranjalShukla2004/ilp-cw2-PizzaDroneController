package inf.ed.cw_ilp.api;

import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LngLatAPI {

    //End-point 2
    // Endpoint to calculate the Euclidean distance between two positions
    public ResponseEntity<Double> calculateDistance(Requests.LngLatPairRequest request) {
        // Validate request data and give error if wrong
        if (request.position1() == null || request.position2() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
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
    public ResponseEntity<Boolean> isWithinRange(Requests.LngLatRegionRequest request) {
        if (!validateRegion(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Position position = request.position();
        List<Position> vertices = request.region().vertices();
        boolean withinRange = isPointInRegion(position, vertices);
        return ResponseEntity.ok(withinRange);
    }

    public boolean isPointInRegion(Position position, List<Position> vertices){
        int size = vertices.size();
        boolean inside = false;

        for(int i = 0, j = size; i < size; j = i++){
            Position p = vertices.get(i);
            Position n = vertices.get(j);
            if (p.lat() > position.lat() != n.lat() > position.lat() &&
                    (position.lng() < (n.lng() - p.lng()) *
                            (position.lat() - p.lat()) / (n.lat() - p.lat() + p.lng()) ))   {
                inside = !inside;
            }
        }
        return inside;
    }

    public boolean validateRegion(Requests.LngLatRegionRequest request) {
        if (request == null || request.region() == null || request.position() == null) {
            return false;
        }
        List<Position> vertices = request.region().vertices();
        return vertices.size() >= 3;
    }

    private static final double EPS = 1e-9;

    // Helper function to check if a point is outside the no-fly zone - specifically if the point is on the edge ?
    private static boolean OnEdge(Position position, Position currentVertex, Position nextVertex) {
        double x = position.lng();
        double y = position.lat();
        double x1 = currentVertex.lng();
        double y1 = currentVertex.lat();
        double x2 = nextVertex.lng();
        double y2 = nextVertex.lat();
        
        if (x < Math.min(x1, x2) - EPS || x > Math.max(x1, x2) + EPS) {
            return false;
        }
        
        double dx = x2 - x1;
        if (Math.abs(dx) < EPS) {
            // if it is a vertical segment, check if position is "close enough" in lng and if its lat is between y1 and y2
            if (Math.abs(x - x1) < EPS
                    && y >= Math.min(y1, y2) - EPS
                    && y <= Math.max(y1, y2) + EPS) {
                return true;
            }
            return false;
        }

        // Calculate slope (dy/dx) and intercept
        double slope = (y2 - y1) / dx;

        // The line equation is y = slope*(x - x1) + y1 derived from [y-y1 / x-x1 = m]
        double expectedY = slope * (x - x1) + y1;

        // Check if position.lat() is very close to expectedY and ensure the lat is within bounding box
        if (Math.abs(y - expectedY) < EPS
                && y >= Math.min(y1, y2) - EPS
                && y <= Math.max(y1, y2) + EPS) {
            return true;
        }

        return false;
    }

}
