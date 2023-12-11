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
import java.util.Arrays;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpHeaderUtils;
import util.HttpRequestUtils;

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
            String url = HttpHeaderUtils.parseUrl(line);
            String queryString = "";

            if (url.contains(".html")) {
                byte[] body = Files.readAllBytes(new File(ROOT_DIRECTORY + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            if (url.contains("?")) {
                int queryStringIndex = url.indexOf('?');
                queryString = url.substring(queryStringIndex + 1);
                url = url.substring(0, queryStringIndex);
            }

            if (url.startsWith("/user/create")) {
                Map<String, String> queryMap = HttpRequestUtils.parseQueryString(queryString);
                String name = queryMap.get("name");
                String password = queryMap.get("password");
                String userId = queryMap.get("userId");
                String email = queryMap.get("email");

                User user = new User(userId, password, name, email);
                log.info("user : {}", user);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
