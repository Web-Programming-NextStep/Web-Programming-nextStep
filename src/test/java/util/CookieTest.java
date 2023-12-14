package util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookieTest {

	@DisplayName("쿠키에 저장된 값을 가져올 수 있다.")
	@Test
	void getCookieValue() {
		// given
		String line = "Set-Cookie: logined=true; test=ok";
		Cookie cookie = new Cookie(line);
		// when
		String actual = cookie.getCookieValue("logined");
		String actual2 = cookie.getCookieValue("test");
		// then
		assertThat(actual).isEqualTo("true");
		assertThat(actual2).isEqualTo("ok");
	}

	@DisplayName("쿠키에 저장된 값이 없다면, null을 반환한다.")
	@Test
	void getCookieValueWithNotContainKey() {
		// given
		String line = "Set-Cookie: logined=true; test=ok";
		Cookie cookie = new Cookie(line);
		// when
		String actual = cookie.getCookieValue("unknown");
		// then
		assertThat(actual).isNull();
	}

	@DisplayName("쿠키에 저장된 key들을 가져올 수 있다.")
	@Test
	void getCookieKeys() {
		// given
		String line = "Set-Cookie: logined=true; test=ok";
		Cookie cookie = new Cookie(line);
		// when
		String[] actual = cookie.getKeys();
		// then
		assertThat(actual).containsExactlyInAnyOrder("logined", "test");
	}

}
