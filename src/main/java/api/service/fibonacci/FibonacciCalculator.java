package api.service.fibonacci;

public class FibonacciCalculator {

    public int calculate(int num) {
        if (num < 1) {
            throw new IllegalArgumentException();
        }
        int current = 1;
        int previous = 1;
        for (int i = 1; i <= num - 2; i++) {
            current = Math.addExact(current, previous);
            previous = Math.addExact(current, -previous);
        }
        return current;
    }

}
