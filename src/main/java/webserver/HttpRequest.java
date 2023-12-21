package webserver;

import util.Cookie;

public class HttpRequest {

	private HttpMethod httpMethod;
	private String simpleUrl;
	private String queryString;
	private Cookie cookie;

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

	public HttpRequest(String httpRequest) {
		this.httpMethod = parseHttpMethod(httpRequest);
		this.simpleUrl = parseHttpUrl(httpRequest);
		this.queryString = parseQueryString(httpRequest);
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public Cookie getCookie() {
		return cookie;
	}

	private String parseQueryString(String httpRequest) {
		String url = httpRequest.split(" ")[1];

		if (url.contains("?")) {
			int queryStartIndex = url.indexOf('?');

			return url.substring(queryStartIndex + 1);
		}

		return "";
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
