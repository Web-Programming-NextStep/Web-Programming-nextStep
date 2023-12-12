package webserver;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpRequestTest {

	@DisplayName("요청으로 들어온 Http요청으로 HttpRequest 객체를 만들 수 있다.")
	@MethodSource("createHttpRequest")
	@ParameterizedTest
	void createHttpRequest(String request, HttpMethod httpMethod, String simpleUrl, String queryString,
		boolean isHtml) {
		// when
		HttpRequest httpRequest = new HttpRequest(request);

		// then
		assertThat(httpRequest.getHttpMethod()).isEqualTo(httpMethod);
		assertThat(httpRequest.getSimpleUrl()).isEqualTo(simpleUrl);
		assertThat(httpRequest.getQueryString()).isEqualTo(queryString);
		assertThat(httpRequest.isHtml()).isEqualTo(isHtml);

	}

	static Stream<Arguments> createHttpRequest() {
		return Stream.of(
			Arguments.arguments("GET / HTTP/1.1", HttpMethod.GET, "/index.html", "", true),
			Arguments.arguments("GET /index.html HTTP/1.1", HttpMethod.GET, "/index.html", "", true),
			Arguments.arguments("GET /user/create?email=yeasung67@gmail.com HTTP/1.1", HttpMethod.GET, "/user/create",
				"email=yeasung67@gmail.com", false)
		);
	}

}
