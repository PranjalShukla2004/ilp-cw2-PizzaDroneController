package inf.ed.cw_ilp.data;

import inf.ed.cw_ilp.model.orderRelated.OrderValidationResult;
import org.springframework.stereotype.Repository;
import inf.ed.cw_ilp.model.orderRelated.Order;
import inf.ed.cw_ilp.model.orderRelated.Orderstats;
import inf.ed.cw_ilp.model.orderRelated.creditCardInformation;
import java.time.LocalDate;

@Repository
public class OrderValidation {
    //End-point 1
    // End-point to get the student id
    public String uuid() {
        return "s2427231";
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

