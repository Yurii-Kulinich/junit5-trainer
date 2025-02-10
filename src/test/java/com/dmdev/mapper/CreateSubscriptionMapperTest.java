package com.dmdev.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CreateSubscriptionMapperTest {

  CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

  @Test
  void map() {
    Instant fixedInstant = LocalDate.of(2026, 11, 12)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant();

    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .expirationDate(fixedInstant)
        .provider(Provider.APPLE.name())
        .build();

    Subscription expectedSubscription = Subscription.builder()
        .name("dummy")
        .userId(123)
        .expirationDate(fixedInstant)
        .provider(Provider.APPLE)
        .status(Status.ACTIVE)
        .build();

    Subscription resultSubscription = mapper.map(dto);

    assertNotNull(resultSubscription);
    assertEquals(expectedSubscription, resultSubscription);

  }
}
