package util;

public final class HttpHeaderUtils {

	private HttpHeaderUtils() {
	}

	public static String parseUrl(String line) {
		return line.split(" ")[1];
	}
}
