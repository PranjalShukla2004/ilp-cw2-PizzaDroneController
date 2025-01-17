package inf.ed.cw_ilp;

import static org.junit.jupiter.api.Assertions.*;

import inf.ed.cw_ilp.api.DynamicDataService;
import inf.ed.cw_ilp.api.OrderValidation;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.orderRelated.*;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderValidationTest {

    private OrderValidation.OrderValidationService orderValidationService;
    private DynamicDataService mockDynamicDataService;

    @BeforeEach
    public void setup() {
        mockDynamicDataService = Mockito.mock(DynamicDataService.class);
        orderValidationService = new OrderValidation.OrderValidationService(mockDynamicDataService);
    }

    @Test
    public void testValidateEmptyOrder() {
        Order order = new Order();
        order.pizzasInOrder = new ArrayList<>();

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.EMPTY_ORDER.name(), result.getValidationCode(), "Validation code should indicate empty order.");
    }

    @Test
    public void testValidateOrderExceedingMaxPizzas() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("R1: Margarita", 1000), new Pizza("R1: Calzone", 1400), new Pizza("R2: Meat Lover", 1400), new Pizza("R2: Vegan Delight", 1100), new Pizza("R3: Super Cheese", 1400));

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED.name(), result.getValidationCode(), "Validation code should indicate max pizza count exceeded.");
    }

    @Test
    public void testValidateOrderWithInvalidCreditCard() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("R1: Margarita", 1000));
        order.creditCardInformation = new creditCardInformation("123456789012345", "123", "12/23");

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.CARD_NUMBER_INVALID.name(), result.getValidationCode(), "Validation code should indicate card number invalid.");
    }

    @Test
    public void testValidateOrderWithExpiredCreditCard() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("R1: Margarita", 1000));
        order.creditCardInformation = new creditCardInformation("1234567890123456", "123", "01/20");

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.EXPIRY_DATE_INVALID.name(), result.getValidationCode(), "Validation code should indicate expiry date invalid.");
    }

    @Test
    public void testValidateOrderOnClosedDay() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("Margherita", 800));
        order.orderDate = LocalDate.now().toString();
        order.priceTotalInPence = 900;

        Mockito.when(mockDynamicDataService.fetchRestaurants()).thenReturn(new ArrayList<>());

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.RESTAURANT_CLOSED.name(), result.getValidationCode(), "Validation code should indicate restaurant closed.");
    }

    @Test
    public void testValidateOrderWithValidDetails() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("Margherita", 800));
        order.orderDate = LocalDate.now().toString();
        order.priceTotalInPence = 900;
        order.creditCardInformation = new creditCardInformation("1234567890123456", "123", "12/30");

        nameData.Restaurant restaurant = new nameData.Restaurant("Test Restaurant", new Position(-3.17979897206425,
                55.939884084483), List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                List.of(new Pizza("Margherita", 800)));

        Mockito.when(mockDynamicDataService.fetchRestaurants()).thenReturn(List.of(restaurant));

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.VALID.name(), result.getStatus(), "Order should be marked valid.");
        assertEquals(Orderstats.OrderValidationCode.NO_ERROR.name(), result.getValidationCode(), "Validation code should indicate no error.");
    }

    @Test
    public void testValidateOrderWithPriceMismatch() {
        Order order = new Order();
        order.pizzasInOrder = List.of(new Pizza("Margherita", 800));
        order.orderDate = LocalDate.now().toString();
        order.priceTotalInPence = 1000;
        order.creditCardInformation = new creditCardInformation("1234567890123456", "123", "12/30");

        nameData.Restaurant restaurant = new nameData.Restaurant("Test Restaurant",new Position( -3.20254147052765,
                55.9432847375794), List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                List.of(new Pizza("Margherita", 800)));

        Mockito.when(mockDynamicDataService.fetchRestaurants()).thenReturn(List.of(restaurant));

        OrderValidationResult result = orderValidationService.validateOrder(order);

        assertEquals(Orderstats.OrderStatus.INVALID.name(), result.getStatus(), "Order should be marked invalid.");
        assertEquals(Orderstats.OrderValidationCode.PRICE_FOR_PIZZA_INVALID.name(), result.getValidationCode(), "Validation code should indicate price mismatch.");
    }
}
