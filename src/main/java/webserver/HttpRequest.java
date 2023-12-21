package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import util.Cookie;
import util.HttpRequestUtils;

public class HttpRequest {

	private HttpMethod httpMethod;
	private String simpleUrl;
	private String queryString;
	private Cookie cookie;
	private Map<String, String> queryParameter = new HashMap<>();

	public HttpRequest(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		if (line == null) {
			return;
		}
		parseHttpMethod(line);
		parseHttpUrl(line);
		parseQueryString(line);
		while (!line.isEmpty()) {

		}
	}

	public HttpRequest(String httpRequest) {
		this.httpMethod = parseHttpMethod(httpRequest);
		this.simpleUrl = parseHttpUrl(httpRequest);
		parseQueryString(httpRequest);
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public String getSimpleUrl() {
		return simpleUrl;
	}

	public String getQueryString() {
		return queryString;
	}

	public boolean isHtml() {
		return simpleUrl.contains(".html");
	}

	public boolean isCss() {
		return simpleUrl.contains(".css");
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public Cookie getCookie() {
		return cookie;
	}

	private void parseQueryString(String httpRequest) {
		String url = httpRequest.split(" ")[1];

		if (url.contains("?")) {
			int startIndex = url.indexOf('?') + 1;
			String queryString = url.substring(startIndex);
			String[] tokens = queryString.split("&");
			Arrays.stream(tokens)
				.forEach(token -> {
					String[] keyValue = token.split("=");
					queryParameter.put(keyValue[0], keyValue[1]);
				});
		}
	}

	private String parseHttpUrl(String httpRequest) {
		String url = httpRequest.split(" ")[1];

		if (url.equals("/")) {
			return "/index.html";
		}

		if (url.contains("?")) {
			int queryStartIndex = url.indexOf('?');

			return url.substring(0, queryStartIndex);
		}

		return url;
	}

	private HttpMethod parseHttpMethod(String httpRequest) {
		String inputHttpMethod = httpRequest.split(" ")[0];

		return HttpMethod.findByValue(inputHttpMethod);
	}
}
