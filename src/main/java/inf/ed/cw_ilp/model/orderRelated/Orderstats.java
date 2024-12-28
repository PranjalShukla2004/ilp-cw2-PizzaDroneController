package inf.ed.cw_ilp.model.orderRelated;

public class Orderstats {
    public enum OrderStatus {
        VALID, INVALID, UNDEFINED
    }

    public enum OrderValidationCode {
        NO_ERROR, CARD_NUMBER_INVALID, EXPIRY_DATE_INVALID, CVV_INVALID,
        TOTAL_INCORRECT, PIZZA_NOT_DEFINED, MAX_PIZZA_COUNT_EXCEEDED,
        PIZZA_FROM_MULTIPLE_RESTAURANTS, RESTAURANT_CLOSED, PRICE_FOR_PIZZA_INVALID,
        EMPTY_ORDER, UNDEFINED
    }
}

