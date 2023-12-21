package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import util.Cookie;

public class HttpRequest {

	private HttpMethod httpMethod;
	private String path;
	private Cookie cookie;
	private Map<String, String> queryParameter = new HashMap<>();
	private Map<String, String> headers = new HashMap<>();

	public HttpRequest(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		if (line == null) {
			return;
		}

		System.out.println("startLine : " + line);
		parseHttpMethod(line);
		parsePath(line);
		parseQueryString(line);
		parseHeader(br);
		if (httpMethod == HttpMethod.POST) {
			parseBody(br);
		}
	}

	private void parseBody(BufferedReader br) throws IOException {
		String body = br.readLine();
		System.out.println(body);
		parseQuery(body);
	}

	public HttpRequest(String httpRequest) {
		parseQueryString(httpRequest);

	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getParameter(String key) {
		return queryParameter.get(key);
	}

	public boolean isHtml() {
		return path.contains(".html");
	}

	public boolean isCss() {
		return path.contains(".css");
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
			parseQuery(queryString);
		}
	}

	private void parsePath(String httpRequest) {
		String url = httpRequest.split(" ")[1];

		if (url.equals("/")) {
			path = "/index.html";
			return;
		}

		if (url.contains("?")) {
			int queryStartIndex = url.indexOf('?');
			path = url.substring(0, queryStartIndex);
			return;
		}

		path = url;
	}

	private void parseQuery(String query) {
		String[] tokens = query.split("&");

		Arrays.stream(tokens)
				.forEach(token -> {
					String[] keyValue = token.split("=");
					queryParameter.put(keyValue[0], keyValue[1]);
				});
	}

	private void parseHttpMethod(String httpRequest) {
		String inputHttpMethod = httpRequest.split(" ")[0];

		httpMethod = HttpMethod.findByValue(inputHttpMethod);
	}

	private void parseHeader(BufferedReader br) throws IOException {
		String line;

		while ((line = br.readLine()) != null) {

			if (line.isEmpty()) {
				return;
			}

			String[] headerKeyValue = line.split(": ");
			headers.put(headerKeyValue[0], headerKeyValue[1]);
		}
	}
}
