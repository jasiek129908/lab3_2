package edu.iis.mto.time;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    @Mock
    private Clock clockMock;
    private Order order;

    @BeforeEach
    void setUp() throws Exception {
        order = new Order(clockMock);
    }

    @Test
    void TestExpiryOfTheOrderShouldThrowException() {
        Instant instantOfSubmission = Instant.parse("2021-04-30T00:00:00.00Z");
        Instant instantOfConfirmation = instantOfSubmission.plus(Order.VALID_PERIOD_HOURS + 1, ChronoUnit.HOURS);

        when(clockMock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clockMock.instant()).thenReturn(instantOfSubmission).thenReturn(instantOfConfirmation);

        order.submit();
        assertThrows(OrderExpiredException.class, () -> order.confirm());
    }

    @Test
    void TestExpiryOfTheOrderShouldNotThrowException() {
        Instant instantOfSubmission = Instant.parse("2021-04-30T00:00:00.00Z");
        Instant instantOfConfirmation = instantOfSubmission.plus(Order.VALID_PERIOD_HOURS, ChronoUnit.HOURS);

        when(clockMock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clockMock.instant()).thenReturn(instantOfSubmission).thenReturn(instantOfConfirmation);

        order.submit();
        Assertions.assertDoesNotThrow(() -> order.confirm());
    }

    @Test
    void TestOrderStateShouldBeConfirmed() {
        Instant instantOfSubmission = Instant.parse("2021-04-30T00:00:00.00Z");
        Instant instantOfConfirmation = instantOfSubmission.plus(10, ChronoUnit.HOURS);

        when(clockMock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clockMock.instant()).thenReturn(instantOfSubmission).thenReturn(instantOfConfirmation);

        order.submit();
        order.confirm();
        assertEquals(Order.State.CONFIRMED, order.getOrderState());
    }

    @Test
    void TestOrderStateShouldBeCanceled() {
        Instant instantOfSubmission = Instant.parse("2021-04-30T00:00:00.00Z");
        Instant instantOfConfirmation = instantOfSubmission.plus(25, ChronoUnit.HOURS);

        when(clockMock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clockMock.instant()).thenReturn(instantOfSubmission).thenReturn(instantOfConfirmation);

        order.submit();
        assertThrows(OrderExpiredException.class, () -> order.confirm());
        assertEquals(Order.State.CANCELLED, order.getOrderState());
    }
}
