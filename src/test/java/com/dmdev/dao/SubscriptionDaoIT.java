package com.dmdev.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SubscriptionDaoIT extends IntegrationTestBase {

  private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

  @Test
  void findAll() {
    var subscriptionFirst = subscriptionDao.insert(getSubscription("first"));
    var subscriptionSecond = subscriptionDao.insert(getSubscription("second "));
    var subscriptionThird = subscriptionDao.insert(getSubscription("third"));

    List<Subscription> result = subscriptionDao.findAll();

    assertEquals(List.of(subscriptionFirst, subscriptionSecond, subscriptionThird), result);

    List<Integer> ids = result.stream()
        .map(Subscription::getId)
        .toList();
    assertThat(ids).contains(subscriptionFirst.getId(), subscriptionSecond.getId(),
        subscriptionThird.getId());
  }


  @Test
  void findByIdWhenIdExistsThenReturnSubscription() {
    var subscriptionFirst = subscriptionDao.insert(getSubscription("first"));

    var result = subscriptionDao.findById(subscriptionFirst.getId());

    assertNotNull(result);
    assertEquals(Optional.of(subscriptionFirst), result);
  }

  @Test
  void findByIdWhenIdNotExistsThenReturnOptionalEmpty() {
    var result = subscriptionDao.findById(3212);

    assertEquals(Optional.empty(), result);
  }

  @Test
  void deleteWhenIdExistsThenReturnTrue() {
    var subscriptionFirst = subscriptionDao.insert(getSubscription("first"));

    var result = subscriptionDao.delete(subscriptionFirst.getId());

    assertTrue(result);

  }

  @Test
  void deleteWhenIdNotExistsThenReturnFalse() {
    subscriptionDao.insert(getSubscription("first"));

    var result = subscriptionDao.delete(1234);

    assertFalse(result);

  }

  @Test
  void update() {
    var subscription = getSubscription("first");
    subscriptionDao.insert(subscription);
    subscription.setName("second");
    subscription.setProvider(Provider.GOOGLE);

    var result = subscriptionDao.update(subscription);

    assertNotNull(result);
    assertEquals(subscription, result);

  }

  @Test
  void insert() {
    var subscription = getSubscription("first");

    var result = subscriptionDao.insert(subscription);

    assertNotNull(result);
  }

  @Test
  void findByUserIdWhenIdExistsThenReturnSubscriptions() {
    var userid = 123;
    var subscriptionFirst = subscriptionDao.insert(getSubscription("first").setUserId(userid));
    var subscriptionSecond = subscriptionDao.insert(getSubscription("second").setUserId(userid));

    List<Subscription> result = subscriptionDao.findByUserId(userid);

    assertNotNull(result);
    assertEquals(2, result.size());

    List<Integer> ids = result.stream()
        .map(Subscription::getId)
        .toList();
    assertThat(ids).contains(subscriptionFirst.getId(), subscriptionSecond.getId());

  }

  @Test
  void findByUserIdWhenIdNotExistsThenReturnEmptyList() {
    var userid = 123;
    subscriptionDao.insert(getSubscription("first").setUserId(userid));
    subscriptionDao.insert(getSubscription("second").setUserId(userid));

    var result = subscriptionDao.findByUserId(3212);

    assertEquals(Collections.EMPTY_LIST, result);
  }


  private Subscription getSubscription(String name) {
    return Subscription.builder()
        .userId(123)
        .name(name)
        .provider(Provider.APPLE)
        .status(Status.ACTIVE)
        .expirationDate(Instant.parse("2025-11-12T10:00:00Z"))
        .build();
  }


}
