package inf.ed.cw_ilp.model.orderRelated;

public class OrderValidationResult {
    public String orderStatus;
    public String orderValidationCode;

    public OrderValidationResult(String orderStatus, String orderValidationCode) {
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
        }
    }

