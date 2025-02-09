package com.dmdev.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropertiesUtilTest {


  @ParameterizedTest
  @MethodSource("getPropertyArguments")
  void checkGet(String key, String expectedValue) {
    String actualResult = PropertiesUtil.get(key);

    assertThat(actualResult).isEqualTo(expectedValue);
  }

  static Stream<Arguments> getPropertyArguments() {

    return Stream.of(
        Arguments.of("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
        Arguments.of("db.user", "sa"),
        Arguments.of("db.password", "")
    );
  }
}
