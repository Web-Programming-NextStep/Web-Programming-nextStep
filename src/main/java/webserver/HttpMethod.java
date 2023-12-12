package webserver;

import java.util.Arrays;

public enum HttpMethod {

	GET, POST, PUT, DELETE, PATCH, OPTIONS;

	public static HttpMethod findByValue(String httpMethod) {
		return Arrays.stream(values())
			.filter(e -> e.name().equals(httpMethod))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("적절하지 않은 Http Method가 들어왔습니다."));
	}
}
