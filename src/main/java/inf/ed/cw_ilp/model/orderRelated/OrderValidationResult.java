package inf.ed.cw_ilp.model.orderRelated;

import inf.ed.cw_ilp.model.pathFinder.nameData;

import java.util.ArrayList;
import java.util.List;

public class OrderValidationResult {
    public String orderStatus;
    public String orderValidationCode;
    public List<nameData.Restaurant> matchedRestaurants;

    // (A) Two-arg constructor (e.g., for INVALID results)
    public OrderValidationResult(String orderStatus, String orderValidationCode) {
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
        this.matchedRestaurants = new ArrayList<>();  // default empty
    }

    // (B) Three-arg constructor (e.g., for exactly one matched restaurant)
    public OrderValidationResult(String orderStatus,
                                 String orderValidationCode,
                                 List<nameData.Restaurant> matchedRestaurants) {
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
        this.matchedRestaurants = matchedRestaurants;
    }
}


