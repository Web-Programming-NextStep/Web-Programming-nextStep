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
import java.util.Collection;
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

			while (!line.equals("")) {
				line = br.readLine();
				if (line.startsWith("Cookie")) {
					Cookie cookie = new Cookie(line);
					httpRequest.setCookie(cookie);
				}
				if (line.startsWith("Content-Length")) {
					contentLength = Integer.parseInt(line.split(":")[1].trim());
				}
			}

			responseResource(url, dos, httpRequest);
			 if (httpRequest.isHtml() || httpRequest.isCss()) {
				return;
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
			} else if (url.startsWith("/user/list")) {
				Cookie cookie = httpRequest.getCookie();
				String value = cookie.getCookieValue("logined");

				if (value.equals("true")) {
					// 노가다
					Collection<User> findAllUsers = DataBase.findAll();
					String html = getUserListHtml(findAllUsers);
					byte[] bytes = html.getBytes();
					response200Header(dos, bytes.length);
					responseBody(dos, bytes);
				} else {
					response302LoginRequestHeader(dos);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private String getUserListHtml(Collection<User> users) {
		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<!DOCTYPE html>\n");
		htmlBuilder.append("<html lang=\"kr\">\n");
		htmlBuilder.append("<head>\n");
		htmlBuilder.append("    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n");
		htmlBuilder.append("    <meta charset=\"utf-8\">\n");
		htmlBuilder.append("    <title>SLiPP Java Web Programming</title>\n");
		htmlBuilder.append(
			"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">\n");
		htmlBuilder.append("    <link href=\"../css/bootstrap.min.css\" rel=\"stylesheet\">\n");
		htmlBuilder.append("    <!--[if lt IE 9]>\n");
		htmlBuilder.append("    <script src=\"//html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n");
		htmlBuilder.append("    <![endif]-->\n");
		htmlBuilder.append("    <link href=\"../css/styles.css\" rel=\"stylesheet\">\n");
		htmlBuilder.append("</head>\n");
		htmlBuilder.append("<body>\n");
		htmlBuilder.append("<nav class=\"navbar navbar-fixed-top header\">\n");
		htmlBuilder.append("    <div class=\"col-md-12\">\n");
		htmlBuilder.append("        <div class=\"navbar-header\">\n");
		htmlBuilder.append("            <a href=\"../index.html\" class=\"navbar-brand\">SLiPP</a>\n");
		htmlBuilder.append(
			"            <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#navbar-collapse1\">\n");
		htmlBuilder.append("                <i class=\"glyphicon glyphicon-search\"></i>\n");
		htmlBuilder.append("            </button>\n");
		htmlBuilder.append("        </div>\n");
		htmlBuilder.append("        <div class=\"collapse navbar-collapse\" id=\"navbar-collapse1\">\n");
		htmlBuilder.append("            <form class=\"navbar-form pull-left\">\n");
		htmlBuilder.append("                <div class=\"input-group\" style=\"max-width:470px;\">\n");
		htmlBuilder.append(
			"                    <input type=\"text\" class=\"form-control\" placeholder=\"Search\" name=\"srch-term\" id=\"srch-term\">\n");
		htmlBuilder.append("                    <div class=\"input-group-btn\">\n");
		htmlBuilder.append(
			"                        <button class=\"btn btn-default btn-primary\" type=\"submit\"><i class=\"glyphicon glyphicon-search\"></i></button>\n");
		htmlBuilder.append("                    </div>\n");
		htmlBuilder.append("                </div>\n");
		htmlBuilder.append("            </form>\n");
		htmlBuilder.append("            <ul class=\"nav navbar-nav navbar-right\">\n");
		htmlBuilder.append("                <li>\n");
		htmlBuilder.append(
			"                    <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"glyphicon glyphicon-bell\"></i></a>\n");
		htmlBuilder.append("                    <ul class=\"dropdown-menu\">\n");
		htmlBuilder.append(
			"                        <li><a href=\"https://slipp.net\" target=\"_blank\">SLiPP</a></li>\n");
		htmlBuilder.append(
			"                        <li><a href=\"https://facebook.com\" target=\"_blank\">Facebook</a></li>\n");
		htmlBuilder.append("                    </ul>\n");
		htmlBuilder.append("                </li>\n");
		htmlBuilder.append(
			"                <li><a href=\"../user/list.html\"><i class=\"glyphicon glyphicon-user\"></i></a></li>\n");
		htmlBuilder.append("            </ul>\n");
		htmlBuilder.append("        </div>\n");
		htmlBuilder.append("    </div>\n");
		htmlBuilder.append("</nav>\n");
		htmlBuilder.append("<div class=\"navbar navbar-default\" id=\"subnav\">\n");
		htmlBuilder.append("    <div class=\"col-md-12\">\n");
		htmlBuilder.append("        <div class=\"navbar-header\">\n");
		htmlBuilder.append(
			"            <a href=\"#\" style=\"margin-left:15px;\" class=\"navbar-btn btn btn-default btn-plus dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"glyphicon glyphicon-home\" style=\"color:#dd1111;\"></i> Home <small><i class=\"glyphicon glyphicon-chevron-down\"></i></small></a>\n");
		htmlBuilder.append("            <ul class=\"nav dropdown-menu\">\n");
		htmlBuilder.append(
			"                <li><a href=\"../user/profile.html\"><i class=\"glyphicon glyphicon-user\" style=\"color:#1111dd;\"></i> Profile</a></li>\n");
		htmlBuilder.append("                <li class=\"nav-divider\"></li>\n");
		htmlBuilder.append(
			"                <li><a href=\"#\"><i class=\"glyphicon glyphicon-cog\" style=\"color:#dd1111;\"></i> Settings</a></li>\n");
		htmlBuilder.append("            </ul>\n");
		htmlBuilder.append(
			"            <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#navbar-collapse2\">\n");
		htmlBuilder.append("                <span class=\"sr-only\">Toggle navigation</span>\n");
		htmlBuilder.append("                <span class=\"icon-bar\"></span>\n");
		htmlBuilder.append("                <span class=\"icon-bar\"></span>\n");
		htmlBuilder.append("                <span class=\"icon-bar\"></span>\n");
		htmlBuilder.append("            </button>\n");
		htmlBuilder.append("        </div>\n");
		htmlBuilder.append("        <div class=\"collapse navbar-collapse\" id=\"navbar-collapse2\">\n");
		htmlBuilder.append("            <ul class=\"nav navbar-nav navbar-right\">\n");
		htmlBuilder.append("                <li class=\"active\"><a href=\"../index.html\">Posts</a></li>\n");
		htmlBuilder.append("                <li><a href=\"../user/login.html\" role=\"button\">로그인</a></li>\n");
		htmlBuilder.append("                <li><a href=\"../user/form.html\" role=\"button\">회원가입</a></li>\n");
		htmlBuilder.append("                <li><a href=\"#\" role=\"button\">로그아웃</a></li>\n");
		htmlBuilder.append("                <li><a href=\"#\" role=\"button\">개인정보수정</a></li>\n");
		htmlBuilder.append("            </ul>\n");
		htmlBuilder.append("        </div>\n");
		htmlBuilder.append("    </div>\n");
		htmlBuilder.append("</div>\n");
		htmlBuilder.append("<div class=\"container\" id=\"main\">\n");
		htmlBuilder.append("   <div class=\"col-md-10 col-md-offset-1\">\n");
		htmlBuilder.append("      <div class=\"panel panel-default\">\n");
		htmlBuilder.append("          <table class=\"table table-hover\">\n");
		htmlBuilder.append("              <thead>\n");
		htmlBuilder.append("                <tr>\n");
		htmlBuilder.append("                    <th>#</th> <th>사용자 아이디</th> <th>이름</th> <th>이메일</th><th></th>\n");
		htmlBuilder.append("                </tr>\n");
		htmlBuilder.append("              </thead>\n");
		htmlBuilder.append("              <tbody>\n");
		for (User user : users) {
			String userId = user.getUserId();
			String name = user.getName();
			String email = user.getEmail();

			htmlBuilder.append("                <tr>\n");
			htmlBuilder.append("                    <th scope=\"row\">1</th> <td>")
				.append(userId)
				.append("</td> <td>")
				.append(name)
				.append("</td> <td>")
				.append(email)
				.append("</td><td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>\n");
			htmlBuilder.append("                </tr>\n");
		}
		htmlBuilder.append("              </tbody>\n");
		htmlBuilder.append("          </table>\n");
		htmlBuilder.append("        </div>\n");
		htmlBuilder.append("    </div>\n");
		htmlBuilder.append("</div>\n");
		htmlBuilder.append("<script src=\"../js/jquery-2.2.0.min.js\"></script>\n");
		htmlBuilder.append("<script src=\"../js/bootstrap.min.js\"></script>\n");
		htmlBuilder.append("<script src=\"../js/scripts.js\"></script>\n");
		htmlBuilder.append("    </body>\n");
		htmlBuilder.append("</html>");

		return htmlBuilder.toString();
	}

	private void responseResource(String url, DataOutputStream dos, HttpRequest httpRequest) throws IOException {
		byte[] body = Files.readAllBytes(new File(ROOT_DIRECTORY + url).toPath());
		if (httpRequest.isHtml()) {
			response200Header(dos, body.length);
		} else if (httpRequest.isCss()) {
			response200CssHeader(dos, body.length);
		}

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

	private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
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

	private void response302LoginRequestHeader(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
			dos.writeBytes("Content-Type: text/html \r\n");
			dos.writeBytes("Location: /user/login.html \r\n");
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
