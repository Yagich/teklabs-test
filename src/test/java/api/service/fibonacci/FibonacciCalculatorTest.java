package api.service.fibonacci;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciCalculatorTest {

    private static int overflowThreshold;

    private FibonacciCalculator calculator = new FibonacciCalculator();

    @BeforeClass
    public static void calculateOverflowThreshold() {
        double goldenValue = (Math.sqrt(5) + 1) / 2;
        overflowThreshold = (int) Math.round(Math.log(Integer.MAX_VALUE) / Math.log(goldenValue) + 1);
    }

    @Test
    public void calculateOnSmallValues() throws Exception {
        assertEquals(1, calculator.calculate(1));
        assertEquals(1, calculator.calculate(2));
        assertEquals(2, calculator.calculate(3));
        assertEquals(3, calculator.calculate(4));
        assertEquals(5, calculator.calculate(5));
    }

    @Test(expected = ArithmeticException.class)
    public void checkOverflowHandling() {
        calculator.calculate(overflowThreshold + 1);
    }

    @Test
    public void calculateBelowOverflowThreshold() {
        int expected = calculator.calculate(overflowThreshold - 1) + calculator.calculate(overflowThreshold - 2);
        assertEquals(calculator.calculate(overflowThreshold), expected);
    }

}