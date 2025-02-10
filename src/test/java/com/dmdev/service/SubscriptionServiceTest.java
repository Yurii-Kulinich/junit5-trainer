package com.dmdev.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private SubscriptionDao subscriptionDao;

  @Mock
  private CreateSubscriptionMapper createSubscriptionMapper;

  @Mock
  private CreateSubscriptionValidator createSubscriptionValidator;

  @Mock
  private Clock clock;


  @InjectMocks
  private SubscriptionService subscriptionService;

  @Test
  void whenCallingUpsertMethodWithInvalidDtoThenThrowValidationException() {
    var dto = Mockito.mock(CreateSubscriptionDto.class);
    var error = Error.of(100, "userId is invalid");
    var validationResult = new ValidationResult();
    validationResult.add(error);

    when(createSubscriptionValidator.validate(any())).thenReturn(validationResult);

    var resultException = assertThrows(ValidationException.class,
        () -> subscriptionService.upsert(dto));

    assertEquals(1, resultException.getErrors().size());
    assertEquals(100, resultException.getErrors().get(0).getCode());

  }

  @Test
  void whenCallingUpsertMethodWithNonExistingSubscriptionThenCreateANewOne() {
    var dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .provider(Provider.APPLE.name())
        .build();
    var newSubscription = getSubscription();

    when(createSubscriptionValidator.validate(any())).thenReturn(new ValidationResult());
    when(subscriptionDao.findByUserId(any())).thenReturn(Collections.emptyList());
    when(createSubscriptionMapper.map(dto)).thenReturn(newSubscription);
    when(subscriptionDao.upsert(newSubscription)).thenReturn(newSubscription);

    var resultSubscription = subscriptionService.upsert(dto);

    assertNotNull(resultSubscription);
    assertEquals(newSubscription, resultSubscription);
    verify(createSubscriptionMapper).map(dto);
  }

  @Test
  void whenCallingUpsertMethodWithAnExistingSubscriptionThenUpdateIt() {
    Instant expirationDate = Instant.parse("2025-11-12T10:00:00Z");
    var dto = CreateSubscriptionDto.builder()
        .name("dummy")
        .userId(123)
        .provider(Provider.APPLE.name())
        .expirationDate(expirationDate)
        .build();

    var existingSubscription = getSubscription();
    var expectedSubscription = getSubscription().setExpirationDate(expirationDate);

    when(createSubscriptionValidator.validate(any())).thenReturn(new ValidationResult());
    when(subscriptionDao.findByUserId(123)).thenReturn(
        Collections.singletonList(existingSubscription));
    when(subscriptionDao.upsert(any())).thenReturn(expectedSubscription);

    var resultSubscription = subscriptionService.upsert(dto);

    assertNotNull(resultSubscription);
    assertEquals(expectedSubscription, resultSubscription);
    verify(subscriptionDao).findByUserId(123);
    verify(subscriptionDao).upsert(expectedSubscription);
    verify(createSubscriptionMapper, never()).map(dto);

  }

  @Test
  void happyFloWhenCallingCancelMethodItUpdatesExistingSubscription() {
    var existingSubscription = getSubscription();
    var updatedSubscription = getSubscription().setStatus(Status.CANCELED);

    when(subscriptionDao.findById(any())).thenReturn(Optional.of(existingSubscription));
    when(subscriptionDao.update(any())).thenReturn(updatedSubscription);

    subscriptionService.cancel(any());

    verify(subscriptionDao, times(1)).update(updatedSubscription);
    verify(subscriptionDao, times(1)).findById(any());
  }

  @Test
  void whenCallingCancelMethodWithNonExistingSubscriptionIdThenThrowIllegalArgumentException() {

    when(subscriptionDao.findById(any())).thenThrow(new IllegalArgumentException());

    assertThrows(IllegalArgumentException.class,
        () -> subscriptionService.cancel(123));

    verify(subscriptionDao, never()).update(any());
  }

  @Test
  void whenCallingCancelMethodWithNonActiveSubscriptionThenThrowSubscriptionException() {

    int id = 123;
    Subscription subscription = getSubscription().setStatus(Status.EXPIRED);

    when(subscriptionDao.findById(id)).thenReturn(Optional.ofNullable(subscription));

    Exception resultException = assertThrows(SubscriptionException.class,
        () -> subscriptionService.cancel(id));

    assertEquals("Only active subscription 123 can be canceled", resultException.getMessage());
    verify(subscriptionDao, never()).update(any());
  }

  @Test
  void whenCallingExpireMethodWithNonExistingSubscriptionIdThenThrowIllegalArgumentException() {

    when(subscriptionDao.findById(any())).thenThrow(new IllegalArgumentException());

    assertThrows(IllegalArgumentException.class,
        () -> subscriptionService.expire(123));

    verify(subscriptionDao, never()).update(any());
  }

  @Test
  void whenCallingExpireMethodWithExpiredSubscriptionThenThrowSubscriptionException() {
    int id = 123;
    Subscription subscription = Subscription.builder()
        .id(id)
        .status(Status.EXPIRED)
        .build();

    when(subscriptionDao.findById(id)).thenReturn(Optional.ofNullable(subscription));

    Exception resultException = assertThrows(SubscriptionException.class,
        () -> subscriptionService.expire(id));

    assertEquals("Subscription 123 has already expired", resultException.getMessage());
    verify(subscriptionDao, never()).update(any());
  }

  @Test
  void happyFloWhenCallingExpireMethodItUpdatesExistingSubscription() {

    int id = 123;
    Instant expirationDate = Instant.parse("2025-11-12T10:00:00Z");
    Subscription givenSubscription = getSubscription();
    Subscription expectedSubscription = getSubscription().setStatus(Status.EXPIRED)
        .setExpirationDate(expirationDate);

    when(subscriptionDao.findById(id)).thenReturn(Optional.ofNullable(givenSubscription));
    when(subscriptionDao.update(expectedSubscription)).thenReturn(expectedSubscription);
    when(clock.instant()).thenReturn(expirationDate);

    subscriptionService.expire(id);

    verify(subscriptionDao).update(expectedSubscription);
    verify(subscriptionDao).findById(id);
  }

  Subscription getSubscription() {
    return Subscription.builder()
        .name("dummy")
        .status(Status.ACTIVE)
        .userId(123)
        .provider(Provider.APPLE)
        .build();
  }
}
