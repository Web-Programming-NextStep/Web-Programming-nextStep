package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import db.DataBase;
import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Cookie;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private static final String ROOT_DIRECTORY = "./webapp";
	private final Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	@Override
	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (
			InputStream in = connection.getInputStream();
			OutputStream out = connection.getOutputStream()
		) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			DataOutputStream dos = new DataOutputStream(out);

			String line = br.readLine();
			if (line == null) {
				return;
			}
			int contentLength = 0;
			HttpRequest httpRequest = new HttpRequest(line);

			String url = httpRequest.getSimpleUrl();
			if (httpRequest.isHtml()) {
				responseResource(url, dos);
				return;
			}

			while (!line.equals("")) {
				line = br.readLine();
				if (line.startsWith("Cookie")) {
					Cookie cookie = new Cookie(line);
				}
				if (line.startsWith("Content-Length")) {
					contentLength = Integer.parseInt(line.split(":")[1].trim());
				}
			}

			String requestBody = IOUtils.readData(br, contentLength);
			Map<String, String> queryMap = HttpRequestUtils.parseQueryString(requestBody);
			if (url.startsWith("/user/create")) {
				String name = queryMap.get("name");
				String password = queryMap.get("password");
				String userId = queryMap.get("userId");
				String email = queryMap.get("email");

				User user = new User(userId, password, name, email);
				DataBase.addUser(user);
				log.info("user : {}", user);
				response302Header(dos, 0);
			} else if (url.startsWith("/user/login")) {
				String userId = queryMap.get("userId");
				String password = queryMap.get("password");
				log.info("userId : {}, password : {}", userId, password);
				User user = DataBase.findUserById(userId);
				log.info("user : {}", user);
				if (user.isSamePassword(password)) {
					response302LoginSuccessHeader(dos);
				} else {
					response302LoginFailHeader(dos);
				}
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseResource(String url, DataOutputStream dos) throws IOException {
		byte[] body = Files.readAllBytes(new File(ROOT_DIRECTORY + url).toPath());
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302LoginSuccessHeader(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
			dos.writeBytes("Content-Type: text/html \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302LoginFailHeader(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
			dos.writeBytes("Content-Type: text/html \r\n");
			dos.writeBytes("Location: /user/login_failed.html \r\n");
			dos.writeBytes("Set-Cookie: logined=false \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
