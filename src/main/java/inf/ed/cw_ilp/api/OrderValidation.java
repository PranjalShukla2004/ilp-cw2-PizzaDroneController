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

                // order date to day-of-week
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

                // Track flags to see how far each restaurant gets in the checks
                boolean dayOpen = false;
                boolean pizzasOk = false;

                List<nameData.Restaurant> matchingOpenRestaurants = new ArrayList<>();

                for (nameData.Restaurant restaurant : allRestaurants) {
                    if (!restaurant.getOpeningDays().contains(dayOfWeek)) {
                        continue; // not open
                    }
                    dayOpen = true;

                    // Check if all pizzas in the order appear on this restaurant’s menu
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

                    long totalPriceWithDelivery = sumPrices + DELIVERY_COST;  // DELIVERY_COST is 100

                    if (totalPriceWithDelivery == order.priceTotalInPence) {
                        matchingOpenRestaurants.add(restaurant);
                    }
                }

                if (matchingOpenRestaurants.isEmpty()) {
                    if (!dayOpen) {
                        return new OrderValidationResult(
                                Orderstats.OrderStatus.INVALID.name(),
                                Orderstats.OrderValidationCode.RESTAURANT_CLOSED.name()
                        );
                    } else if (dayOpen && !pizzasOk) {
                        // We had an open restaurant but the pizzas never matched
                        return new OrderValidationResult(
                                Orderstats.OrderStatus.INVALID.name(),
                                Orderstats.OrderValidationCode.PIZZA_NOT_DEFINED.name()
                        );
                    } else {
                        // We found an open restaurant with correct pizzas, but never matched the total
                        return new OrderValidationResult(
                                Orderstats.OrderStatus.INVALID.name(),
                                Orderstats.OrderValidationCode.TOTAL_INCORRECT.name()
                        );
                    }
                }

                // Order matched with multiple restaurants
                if (matchingOpenRestaurants.size() > 1) {
                    return new OrderValidationResult(
                            Orderstats.OrderStatus.INVALID.name(),
                            Orderstats.OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS.name()
                    );
                }

                // Exactly one matched restaurant
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
