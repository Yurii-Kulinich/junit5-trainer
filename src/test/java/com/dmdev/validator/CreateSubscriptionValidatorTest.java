package com.dmdev.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dmdev.dto.CreateSubscriptionDto;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class CreateSubscriptionValidatorTest {

  private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

  @Test
  void happyFlowPassesValidation() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .expirationDate(ZonedDateTime.now().plusDays(1).toInstant())
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertFalse(result.hasErrors());

  }

  @Test
  void invalidName() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("")
        .userId(123)
        .expirationDate(ZonedDateTime.now().plusDays(1).toInstant())
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(1, result.getErrors().size());
    assertEquals("name is invalid", result.getErrors().get(0).getMessage());

  }

  @Test
  void invalidUserId() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .expirationDate(ZonedDateTime.now().plusDays(1).toInstant())
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(1, result.getErrors().size());
    assertEquals("userId is invalid", result.getErrors().get(0).getMessage());

  }

  @Test
  void invalidProvider() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .expirationDate(ZonedDateTime.now().plusDays(1).toInstant())
        .provider("PLUM")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(1, result.getErrors().size());
    assertEquals("provider is invalid", result.getErrors().get(0).getMessage());

  }

  @Test
  void invalidExpirationDate() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .expirationDate(ZonedDateTime.now().minusDays(1L).toInstant())
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(1, result.getErrors().size());
    assertEquals("expirationDate is invalid", result.getErrors().get(0).getMessage());

  }

  @Test
  void expirationDateIsNull() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(1, result.getErrors().size());
    assertEquals("expirationDate is invalid", result.getErrors().get(0).getMessage());

  }

  @Test
  void invalidExpirationDateAndName() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("")
        .userId(123)
        .expirationDate(ZonedDateTime.now().minusDays(1).toInstant())
        .provider("APPLE")
        .build();

    ValidationResult result = validator.validate(dto);

    assertTrue(result.hasErrors());
    assertEquals(2, result.getErrors().size());
    assertEquals("name is invalid", result.getErrors().get(0).getMessage());
    assertEquals("expirationDate is invalid", result.getErrors().get(1).getMessage());

  }
}
