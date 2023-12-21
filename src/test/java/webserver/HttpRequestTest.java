package webserver;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import util.Cookie;

class HttpRequestTest {
	private final String TEST_DIRECTORY = "./src/test/resources/";

	@DisplayName("GET 요청에 대한 HttpRequest 객체를 InputStream을 받아 생성 할 수 있다.")
	@Test
	void request_GET() throws IOException {
		// given
		InputStream in = new FileInputStream(new File(TEST_DIRECTORY + "Http_GET.txt"));

		// when
		HttpRequest httpRequest = new HttpRequest(in);

		// then
		assertThat(httpRequest.getHttpMethod()).isEqualTo(HttpMethod.GET);
		assertThat(httpRequest.getPath()).isEqualTo("/user/create");
		assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
		assertThat(httpRequest.getParameter("userId")).isEqualTo("javajigi");
	}

	@DisplayName("POST 요청에 대한 HttpRequest 객체를 InputStream을 받아 생성 할 수 있다.")
	@Test
	void request_POST() throws IOException {
		// given
		InputStream in = new FileInputStream(new File(TEST_DIRECTORY + "Http_POST.txt"));

		// when
		HttpRequest httpRequest = new HttpRequest(in);

		// then
		assertThat(httpRequest.getHttpMethod()).isEqualTo(HttpMethod.POST);
		assertThat(httpRequest.getPath()).isEqualTo("/user/create");
		assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
		assertThat(httpRequest.getParameter("userId")).isEqualTo("javajigi");
	}

	@Disabled
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

}
