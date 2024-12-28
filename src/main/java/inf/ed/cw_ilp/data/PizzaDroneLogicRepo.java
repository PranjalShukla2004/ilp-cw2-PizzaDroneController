package inf.ed.cw_ilp.data;

import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.orderRelated.OrderValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import inf.ed.cw_ilp.model.orderRelated.Order;
import inf.ed.cw_ilp.model.orderRelated.Orderstats;
import inf.ed.cw_ilp.model.orderRelated.creditCardInformation;
import java.time.LocalDate;
import java.util.List;

@Repository
public class PizzaDroneLogicRepo {
    //End-point 1
    // End-point to get the student id
    public String uuid() {
        return "s2427231";
    }

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

    // End-point 4
    // End-point to get the next Position of the drone each move is constant 0.00015
    private static final double MOVE_DISTANCE = 0.00015;
    public ResponseEntity<Position> nextPosition(Requests.LngLatAngleRequest request) {
        if (request.start() == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();  // return false for invalid input
        }
        double startLng = request.start().lng();
        double startLat = request.start().lat();
        double angle = request.angle();

        while(angle<0){
            angle += 360;
        }
        angle = angle % 360;
        if(angle % 22.5 == 0) {
            double angleInRadians = Math.toRadians(angle);
            double deltaLng = MOVE_DISTANCE * Math.cos(angleInRadians);  // Change in longitude
            double deltaLat = MOVE_DISTANCE * Math.sin(angleInRadians);  // Change in latitude
            double newLng = startLng + deltaLng;
            double newLat = startLat + deltaLat;
            return ResponseEntity.ok(new Position(newLng, newLat));  // 200 OK with the new position in the response body
        }
        else {
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
        if(vertices.size() < 3) {
            return false;
        }
        return true;
    }


    // End-point to validate Order...
    public static class OrderValidationService {
        public OrderValidationResult validateOrder(Order order) {
            if (order.pizzasInOrder == null || order.pizzasInOrder.isEmpty()) {
                return new OrderValidationResult(Orderstats.OrderStatus.INVALID.name(), Orderstats.OrderValidationCode.EMPTY_ORDER.name());
            }

            if (order.pizzasInOrder.size() > 4) {
                return new OrderValidationResult(Orderstats.OrderStatus.INVALID.name(), Orderstats.OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED.name());
            }

            if (!isValidCreditCard(order.creditCardInformation)) {
                return new OrderValidationResult(Orderstats.OrderStatus.INVALID.name(), Orderstats.OrderValidationCode.CARD_NUMBER_INVALID.name());
            }

            if (!isValidOrderDate(order.orderDate)) {
                return new OrderValidationResult(Orderstats.OrderStatus.INVALID.name(), Orderstats.OrderValidationCode.EXPIRY_DATE_INVALID.name());
            }

            return new OrderValidationResult(Orderstats.OrderStatus.VALID.name(), Orderstats.OrderValidationCode.NO_ERROR.name());
        }

        private boolean isValidCreditCard(creditCardInformation cardInfo) {
            return cardInfo.creditCardNumber.matches("\\d{16}") && cardInfo.cvv.matches("\\d{3}");
        }

        private boolean isValidOrderDate(String orderDate) {
            return LocalDate.parse(orderDate).isAfter(LocalDate.now().minusDays(1));
        }
    }


}

