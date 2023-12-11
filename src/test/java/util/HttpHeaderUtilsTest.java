package util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HttpHeaderUtilsTest {

	@DisplayName("헤더에 있는 요청 URL을 분리할 수 있다.")
	@CsvSource(value = {"GET / HTTP/1.1:/", "POST /index.html HTTP/1.1:/index.html",
		"PUT /home.html HTTP/1.1:/home.html",
		"DELETE /users HTTP/1.1:/users"}, delimiter = ':')
	@ParameterizedTest
	void parseUrl(String requestHeader, String expected) {
		String url = HttpHeaderUtils.parseUrl(requestHeader);
		assertThat(url).isEqualTo(expected);
	}
}
