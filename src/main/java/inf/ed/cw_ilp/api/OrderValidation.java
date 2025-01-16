package inf.ed.cw_ilp.api;

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

import static inf.ed.cw_ilp.utils.Constants.DELIVERY_COST;

@Repository
public class OrderValidation {

    public static class OrderValidationService {

        private static final Logger log = LoggerFactory.getLogger(OrderValidationService.class);
        private final DynamicDataService dds;

        public OrderValidationService(DynamicDataService dds) {
            this.dds = dds;
        }

        public OrderValidationResult validateOrder(Order order) {

            // (1) Existing checks (empty order, max pizzas, valid credit card, etc.)
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
                // Detailed checks for credit card validation
                if (!order.creditCardInformation.creditCardNumber.matches("\\d{16}")) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.CARD_NUMBER_INVALID.name()
                    );
                }
                if (!order.creditCardInformation.cvv.matches("\\d{3}")) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.CVV_INVALID.name()
                    );
                }
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
                    YearMonth expiry = YearMonth.parse(order.creditCardInformation.getCreditCardExpiry(), formatter);
                    YearMonth now = YearMonth.now();

                    // If the expiry date is invalid
                    if (expiry.isBefore(now)) {
                        return new OrderValidationResult(
                                Orderstats.OrderStatus.INVALID.name(),
                                Orderstats.OrderValidationCode.EXPIRY_DATE_INVALID.name()
                        );
                    }

                } catch (DateTimeParseException e) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.EXPIRY_DATE_INVALID.name()
                    );
                }
            }

            // (2) Parse order date to day-of-week
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
            List<nameData.Restaurant> allRestaurants = dds.fetchRestaurants();

            boolean dayOpen = false;
            boolean pizzasOk = false;
            boolean priceMatch = false; // Flag to check if price matches

            List<nameData.Restaurant> matchingOpenRestaurants = new ArrayList<>();

            for (nameData.Restaurant restaurant : allRestaurants) {
                if (!restaurant.getOpeningDays().contains(dayOfWeek)) {
                    continue; // Not open today
                }
                dayOpen = true;

                long sumPrices = 0;
                boolean allPizzasFound = true;

                // Check if all pizzas in the order appear on the restaurant's menu
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

                // If some pizzas weren't found in the restaurant menu, continue to next restaurant
                if (!allPizzasFound) {
                    continue;
                }
                pizzasOk = true;

                // Check if the sum of pizza prices in the order matches the restaurant menu prices
                if (sumPrices == order.pizzasInOrder.stream().mapToLong(Pizza::getPriceInPence).sum()) {
                    priceMatch = true;
                }

                // Add restaurant to matching list if price matches
                if (priceMatch) {
                    long totalPriceWithDelivery = sumPrices + DELIVERY_COST; // DELIVERY_COST is 100
                    if (totalPriceWithDelivery == order.priceTotalInPence) {
                        matchingOpenRestaurants.add(restaurant);
                    }
                }
            }

            // (3) Evaluate validation result based on matching restaurants and price
            if (matchingOpenRestaurants.isEmpty()) {
                if (!dayOpen) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.RESTAURANT_CLOSED.name()
                    );
                } else if (dayOpen && !pizzasOk) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.PIZZA_NOT_DEFINED.name()
                    );
                } else if (!priceMatch) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.PRICE_FOR_PIZZA_INVALID.name()
                    );
                } else {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.TOTAL_INCORRECT.name()
                    );
                }
            }

            // (4) Order matched with multiple restaurants
            if (matchingOpenRestaurants.size() > 1) {
                return new OrderValidationResult(
                        Orderstats.OrderStatus.INVALID.name(),
                        Orderstats.OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS.name()
                );
            }

            // (5) Exactly one matched restaurant
            return new OrderValidationResult(
                    Orderstats.OrderStatus.VALID.name(),
                    Orderstats.OrderValidationCode.NO_ERROR.name(),
                    matchingOpenRestaurants
            );
        }



        private boolean isValidCreditCard(creditCardInformation cardInfo) {
                if (!cardInfo.creditCardNumber.matches("\\d{16}")) {
                    return false;
                }
                if (!cardInfo.cvv.matches("\\d{3}")) {
                    return false;
                }

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
                    YearMonth expiry = YearMonth.parse(cardInfo.getCreditCardExpiry(), formatter);
                    YearMonth now = YearMonth.now();
                    return !expiry.isBefore(now);

                } catch (DateTimeParseException e) {
                    return false;
                }
            }
        }
    }
