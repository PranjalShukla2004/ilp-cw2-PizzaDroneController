package inf.ed.cw_ilp.controller;

import inf.ed.cw_ilp.api.DynamicDataService;
import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.data.*;
import inf.ed.cw_ilp.model.pathFinder.*;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.orderRelated.Order;
import inf.ed.cw_ilp.model.orderRelated.OrderValidationResult;
import inf.ed.cw_ilp.model.orderRelated.Orderstats;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class PizzaDroneController {

    private final PizzaDroneLogicRepo runrepo;
    private final LngLatAPI LngLatRequest;
    private final DynamicDataService dds;
    public PizzaDroneController(PizzaDroneLogicRepo runRepo, LngLatAPI lngLatRequest, DynamicDataService dds) {
        this.runrepo = runRepo;
        this.LngLatRequest = lngLatRequest;
        this.dds = dds;
    }

    // End-Point 1
    @GetMapping("/uuid")
    String return_id(){
        return runrepo.uuid();
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
    public ResponseEntity<Boolean> validRegion(@RequestBody Requests.LngLatRegionRequest json) {
        return LngLatRequest.isWithinRange(json);
    }

    // End-point 6
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order){
        OrderValidation.OrderValidationService validationService = new OrderValidation.OrderValidationService();
        OrderValidationResult result = validationService.validateOrder(order);
        if (result.orderStatus.equals(Orderstats.OrderStatus.INVALID.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<?> calcDeliveryPath(@RequestBody Order order) {
        OrderValidation.OrderValidationService validationService = new OrderValidation.OrderValidationService();
        OrderValidationResult validationResult = validationService.validateOrder(order);

        if (validationResult.orderStatus.equals(Orderstats.OrderStatus.INVALID.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        nameData.NamedRegion centralArea = dds.fetchCentralArea();
        List<nameData.NamedRegion> noFlyZones = dds.fetchNoFlyZones();

        Position start = new Position(
                order.getRestaurantLng(),  // or however you store it
                order.getRestaurantLat()
        );

        Position end = new Position(
                order.getDeliveryLng(),
                order.getDeliveryLat()
        );

        // 4) Build A_Star with these positions + DDS data
        A_Star aStar = new A_Star(start, end, centralArea, noFlyZones);

        // 5) Calculate the path
        List<Position> path = aStar.calculatePath();

        // 6) If no path, respond with 400
        if (path.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No valid path found for this order");
        }

        // 7) Otherwise, return 200 (or 201) with the path array
        return ResponseEntity.ok(path);
    }


}