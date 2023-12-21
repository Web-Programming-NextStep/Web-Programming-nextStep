package webserver;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import util.Cookie;

class HttpRequestTest {
	private final String TEST_DIRECTORY = "./src/test/resources/";

	@DisplayName("")
	@Test
	void request_GET() throws FileNotFoundException {
		// given
		InputStream in = new FileInputStream(new File(TEST_DIRECTORY + "Http_GET.txt"));
		HttpRequest httpRequest = new HttpRequest(in);

		// then
		assertThat(httpRequest.getHttpMethod()).isEqualTo(HttpMethod.GET);
		assertThat(httpRequest.getPath()).isEqualTo("/user/create");
		assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
		assertThat(httpRequest.getParameter("userId")).isEqualTo("javajigi");

	}

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

	@DisplayName("request 요청에 cookie를 넣을 수 있다.")
	@Test
	void setCookie() {
	    // given
		String url = "GET /index.html HTTP/1.1";
		HttpRequest httpRequest = new HttpRequest(url);

		Cookie cookie = new Cookie("Cookie: logined=true; test=ok");

	    // when
		httpRequest.setCookie(cookie);

	    // then
		assertThat(httpRequest.getCookie()).isEqualTo(cookie);
	}

	@DisplayName("요청 값에 대해서 css파일을 확인할 수 있다.")
	@CsvSource(value = {"GET /index.html:False", "GET /index.css:True"}, delimiter = ':')
	@ParameterizedTest
	void cssParsing(String url, Boolean expected) {
	    // given
		HttpRequest httpRequest = new HttpRequest(url);

	    // when
		boolean actual = httpRequest.isCss();

		// then
		assertThat(actual).isEqualTo(expected);
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
