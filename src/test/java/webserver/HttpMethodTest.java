package webserver;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpMethodTest {

	@DisplayName("String 타입의 Http 메소드를 Enum으로 변환한다.")
	@MethodSource("findByHttpMethodStream")
	@ParameterizedTest
	void findHttpMethod(String httpMethod, HttpMethod expected) {
		// when
		HttpMethod actual = HttpMethod.findByValue(httpMethod);

		// then
		assertThat(actual).isEqualTo(expected);
	}

	static Stream<Arguments> findByHttpMethodStream() {
		return Stream.of(
			Arguments.arguments("GET", HttpMethod.GET),
			Arguments.arguments("POST", HttpMethod.POST),
			Arguments.arguments("PUT", HttpMethod.PUT),
			Arguments.arguments("DELETE", HttpMethod.DELETE),
			Arguments.arguments("PATCH", HttpMethod.PATCH),
			Arguments.arguments("OPTIONS", HttpMethod.OPTIONS)
		);
	}
}
