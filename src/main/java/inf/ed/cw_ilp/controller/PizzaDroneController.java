package inf.ed.cw_ilp.controller;

import inf.ed.cw_ilp.api.DynamicDataService;
import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.data.*;
import inf.ed.cw_ilp.model.Regions.Region;
import inf.ed.cw_ilp.model.pathFinder.*;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.orderRelated.Order;
import inf.ed.cw_ilp.model.orderRelated.OrderValidationResult;
import inf.ed.cw_ilp.model.orderRelated.Orderstats;
import inf.ed.cw_ilp.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class PizzaDroneController {

    private final OrderValidation runrepo;
    private final LngLatAPI LngLatRequest;
    private final DynamicDataService dds;
    private static final Logger log = LoggerFactory.getLogger(PizzaDroneController.class);
    public PizzaDroneController(OrderValidation runRepo, LngLatAPI lngLatRequest, DynamicDataService dds) {
        this.runrepo = runRepo;
        this.LngLatRequest = lngLatRequest;
        this.dds = dds;
    }

    // End-Point 1
    @GetMapping("/uuid")
    String return_id(){
        return "s2427231";
    }


    // End-Point 2
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> returnDistanceTo(@RequestBody Requests.LngLatPairRequest json) {
        // Call the service method to calculate distance
        ResponseEntity<Double> distance = LngLatRequest.calculateDistance(json);
        return ResponseEntity.ok(distance.getBody()); // Return HTTP 200 with the distance
    }

    // End-point 3
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> returnIsCloseTo(@RequestBody Requests.LngLatPairRequest json) {
        return LngLatRequest.isCloseTo(json);

    }

    // End-point 4
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/nextPosition")
    public ResponseEntity<Position> calNextPos(@RequestBody Requests.LngLatAngleRequest json) {
        return LngLatRequest.nextPosition(json);

    }

    // End-point 5
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/isInRegion")
    public boolean validRegion(@RequestBody Requests.LngLatRegionRequest json) {
        Position position = json.position();
        Region region = json.region();
        return LngLatRequest.isInsideRegion(position, region);
    }

    // End-point 6
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order){
        OrderValidation.OrderValidationService validationService = new OrderValidation.OrderValidationService(dds);
        OrderValidationResult result = validationService.validateOrder(order);
        if (result.orderStatus.equals(Orderstats.OrderStatus.INVALID.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<?> calcDeliveryPath(@RequestBody Order order) {

        // 1) Validate
        OrderValidation.OrderValidationService validationService =
                new OrderValidation.OrderValidationService(dds);
        OrderValidationResult validationResult = validationService.validateOrder(order);

        // 2) If invalid, return 400
        if (validationResult.orderStatus.equals(Orderstats.OrderStatus.INVALID.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        // 3) Fetch central + noFly
        nameData.NamedRegion centralArea = dds.fetchCentralArea();
        List<nameData.NamedRegion> noFlyZones = dds.fetchNoFlyZones();

        if (centralArea == null || centralArea.getCoordinates() == null) {
            log.error("Central area is not fetched correctly");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Central area fetch failed");
        }
        // 4) Retrieve matched restaurants
        List<nameData.Restaurant> matched = validationResult.matchedRestaurants;
        if (matched == null || matched.size() != 1) {
            // Should not happen if it's "VALID" => but let's be safe:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No single matched restaurant found");
        }

        // 5) We have exactly one match
        nameData.Restaurant matchedRestaurant = matched.get(0);

        // 6) Start from the restaurant's location
        Position start = matchedRestaurant.getLocation();

        // 7) The end is Appleton
        Position end = new Position(Constants.APPLETON_TOWER.lng(),
                Constants.APPLETON_TOWER.lat());

        // 8) Build A_Star, run it
        A_Star aStar = new A_Star(start, end, centralArea, noFlyZones);
        List<Position> path = aStar.calculatePath();

        // 9) If no path, respond 400
        if (path.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No valid path found for this order");
        }

        // 10) Otherwise, return 200 OK with path
        return ResponseEntity.ok(path);
    }


}