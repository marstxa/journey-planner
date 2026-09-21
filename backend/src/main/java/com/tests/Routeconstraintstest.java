package modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RouteConstraintsTest {

    @Test
    void isClosedIgnoresCaseAndSurroundingWhitespace() {
        RouteConstraints constraints = new RouteConstraints(Set.of("victoria"), Map.of());

        assertTrue(constraints.isClosed("Victoria"));
        assertTrue(constraints.isClosed("  VICTORIA  "));
        assertFalse(constraints.isClosed("Piccadilly"));
    }

    @Test
    void isClosedReturnsFalseForNullStation() {
        RouteConstraints constraints = new RouteConstraints(Set.of("victoria"), Map.of());
        assertFalse(constraints.isClosed(null));
    }

    @Test
    void getActualTimeReturnsDelayWhenOneIsSet() {
        Map<String, Double> delays = Map.of("victoria-piccadilly", 15.0);
        RouteConstraints constraints = new RouteConstraints(Set.of(), delays);

        assertEquals(15.0, constraints.getActualTime("Victoria", "Piccadilly", 5.0), 0.0001);
    }

    @Test
    void getActualTimeFallsBackToNormalTimeWhenNoDelaySet() {
        RouteConstraints constraints = RouteConstraints.none();
        assertEquals(5.0, constraints.getActualTime("Victoria", "Piccadilly", 5.0), 0.0001);
    }

    @Test
    void noneHasNoClosuresOrDelays() {
        RouteConstraints constraints = RouteConstraints.none();
        assertFalse(constraints.isClosed("Anywhere"));
        assertEquals(7.0, constraints.getActualTime("A", "B", 7.0), 0.0001);
    }
}