package inf.ed.cw_ilp.data;

import inf.ed.cw_ilp.api.DynamicDataService;
import inf.ed.cw_ilp.model.orderRelated.*;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderValidation {

    public static class OrderValidationService {

        private static final Logger log = LoggerFactory.getLogger(OrderValidationService.class);
        private final DynamicDataService dds;

        public OrderValidationService(DynamicDataService dds) {
            this.dds = dds;
        }

        public OrderValidationResult validateOrder(Order order) {

            // (1) Existing basic checks (pizzas, credit card format, etc.)
            if (order.pizzasInOrder == null || order.pizzasInOrder.isEmpty()) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        Orderstats.OrderValidationCode.EMPTY_ORDER.name()
                );
            }
            if (order.pizzasInOrder.size() > 4) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        Orderstats.OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED.name()
                );
            }
            if (!isValidCreditCard(order.creditCardInformation)) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        Orderstats.OrderValidationCode.CARD_NUMBER_INVALID.name()
                );
            }

            // (2) Convert order date to day-of-week
            LocalDate date;
            try {
                date = LocalDate.parse(order.orderDate);
            } catch (DateTimeParseException e) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        "ORDER_DATE_PARSE_ERROR"
                );
            }
            String dayOfWeek = date.getDayOfWeek().toString();

            // (3) Fetch all restaurants
            List<nameData.Restaurant> allRestaurants = dds.fetchRestaurants();

            // Track flags to see how far each restaurant gets
            boolean dayOpen = false;
            boolean pizzasOk = false;
            boolean priceOk = false;

            List<nameData.Restaurant> matchingOpenRestaurants = new ArrayList<>();

            for (nameData.Restaurant restaurant : allRestaurants) {
                if (!restaurant.getOpeningDays().contains(dayOfWeek)) {
                    continue; // not open
                }
                dayOpen = true;

                // 2) Check if all pizzas in the order appear on this restaurant’s menu
                long sumPrices = 0;
                boolean allPizzasFound = true;
                for (Pizza orderedPizza : order.pizzasInOrder) {
                    Optional<Pizza> maybeMenuPizza = restaurant.getMenu().stream()
                            .filter(menuPizza -> menuPizza.getName().equalsIgnoreCase(orderedPizza.getName()))
                            .findFirst();
                    if (maybeMenuPizza.isPresent()) {
                        sumPrices += maybeMenuPizza.get().getPriceInPence();
                    } else {
                        allPizzasFound = false;
                        break;
                    }
                }
                if (!allPizzasFound) {
                    continue;
                }
                pizzasOk = true;

                // 3) Check total price
                if (sumPrices == order.priceTotalInPence) {
                    priceOk = true;  // <--- IMPORTANT: Mark that at least one price matched
                    matchingOpenRestaurants.add(restaurant);
                }
                // else we skip this restaurant (price mismatch)
            }

            // (4) Evaluate how many restaurants matched

            if (matchingOpenRestaurants.isEmpty()) {
                // No final matches => pick more specific code
                if (!dayOpen) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.RESTAURANT_CLOSED.name()
                    );
                }
                else if (dayOpen && !pizzasOk) {
                    // We had an open restaurant but the pizzas never matched
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.PIZZA_NOT_DEFINED.name()
                    );
                }
                else {
                    // We found an open restaurant with correct pizzas,
                    // but never matched the total
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.TOTAL_INCORRECT.name()
                    );
                }
            }

            // If more than one matched, also invalid
            if (matchingOpenRestaurants.size() > 1) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        Orderstats.OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS.name()
                );
            }

            // Exactly one match => valid
            return new OrderValidationResult(
                    Orderstats.OrderStatus.VALID.name(),
                    Orderstats.OrderValidationCode.NO_ERROR.name(),
                    matchingOpenRestaurants // a single-element list
            );
        }


        private boolean isValidCreditCard(creditCardInformation cardInfo) {
            // 1) Basic format checks:
            if (!cardInfo.creditCardNumber.matches("\\d{16}")) {
                return false;
            }
            if (!cardInfo.cvv.matches("\\d{3}")) {
                return false;
            }

            // 2) Parse expiry date "MM/yy" with a try/catch
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
                YearMonth expiry = YearMonth.parse(cardInfo.getCreditCardExpiry(), formatter);
                YearMonth now = YearMonth.now();

                // If expiry is strictly before current month, it’s invalid
                return !expiry.isBefore(now);

            } catch (DateTimeParseException e) {
                // If we can't parse the expiry string, treat as invalid
                return false;
            }
        }

    }
}
