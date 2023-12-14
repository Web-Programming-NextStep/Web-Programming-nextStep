package util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cookie {

	private final Map<String, String> cookieMap = new ConcurrentHashMap<>();

	public Cookie(String line) {
		initCookie(line);
	}

	public String getCookieValue(String key) {
		if (!cookieMap.containsKey(key)) {
			return null;
		}
		return cookieMap.get(key);
	}

	public String[] getKeys() {
		return cookieMap.keySet()
			.toArray(new String[0]);
	}

	private void initCookie(String line) {
		int startPosition = line.indexOf(':');
		String pureCookie = line.substring(startPosition + 1);
		String[] cookies = pureCookie.split(";");
		for (String cookie : cookies) {
			if (cookie.isEmpty()) {
				continue;
			}
			String[] splited = cookie.trim().split("=");
			String key = splited[0];
			String value = splited[1];
			cookieMap.put(key, value);
		}
	}
}
