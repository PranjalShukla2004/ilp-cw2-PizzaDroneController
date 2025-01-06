package inf.ed.cw_ilp.controller;

import inf.ed.cw_ilp.api.LngLatAPI;
import inf.ed.cw_ilp.data.*;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.Regions.Requests;
import inf.ed.cw_ilp.model.orderRelated.Order;
import inf.ed.cw_ilp.model.orderRelated.OrderValidationResult;
import inf.ed.cw_ilp.model.orderRelated.Orderstats;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class PizzaDroneController {

    private final PizzaDroneLogicRepo runrepo;
    private final LngLatAPI LngLatRequest;
    public PizzaDroneController(PizzaDroneLogicRepo runRepo, LngLatAPI lngLatRequest) {
        this.runrepo = runRepo;
        this.LngLatRequest = lngLatRequest;
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
        PizzaDroneLogicRepo.OrderValidationService validationService = new PizzaDroneLogicRepo.OrderValidationService();
        OrderValidationResult result = validationService.validateOrder(order);
        if (result.orderStatus.equals(Orderstats.OrderStatus.INVALID.name())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);

    }



}