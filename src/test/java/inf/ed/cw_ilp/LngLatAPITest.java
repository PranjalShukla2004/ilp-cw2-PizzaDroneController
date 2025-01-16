package inf.ed.cw_ilp;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestClass;

public class LngLatAPITest {
    public static Position[] testIntCoords;
    public static Position[] centralArea;

    @BeforeTestClass
    public static void setUpBeforeClass() {
        Position p1 = new Position(-1, 1);
        Position p2 = new Position(2, 4);
        Position p3 = new Position(5, 0);
        Position p4 = new Position(3, -3);
        Position p5 = new Position(-3, -5);
        Position p6 = new Position(-4, -2);
        testIntCoords = new Position[]{p1, p2, p3, p4, p5, p6};

        Position c1 = new Position(-3.192473,  55.946233);
        Position c2 = new Position(-3.192473,  55.942617);
        Position c3 = new Position(-3.184319,  55.942617);
        Position c4 = new Position(-3.184319,  55.946233);
        centralArea = new Position[]{c1,c2,c3,c4};

    }
    @Test
    public void testPointInCentralArea1() {
        Position p1 = new Position(-1, 1);
    }
}
