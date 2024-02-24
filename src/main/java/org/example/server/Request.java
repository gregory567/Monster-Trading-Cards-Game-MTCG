package org.example.server;

import org.example.http.Method;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;

@Getter
@Setter(AccessLevel.PROTECTED)
public class Request {
    private Method method;
    private String pathname;
    private String params;
    private String contentType;
    private Integer contentLength;
    private String authorization;
    private String body = "";

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final String CONTENT_TYPE = "Content-Type: ";
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final String CONTENT_LENGTH = "Content-Length: ";
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final String AUTHORIZATION = "Authorization: Bearer ";

    // Constructor taking a BufferedReader for reading the request
    public Request(BufferedReader inputStream) {
        // Call the method to build the request
        buildRequest(inputStream);
    }

    // Method to build a request from an input stream
    private void buildRequest(BufferedReader inputStream) {
        try {
            // Read the first line of the request
            String line = inputStream.readLine();

            // Check if the line is not null
            if (line != null) {
                // Split the first line into parts
                String[] splitFirstLine = line.split(" ");
                // Check if the request has parameters
                Boolean hasParams = splitFirstLine[1].indexOf("?") != -1;

                // Set HTTP method, pathname, and parameters
                setMethod(getMethodFromInputLine(splitFirstLine));
                setPathname(getPathnameFromInputLine(splitFirstLine, hasParams));
                setParams(getParamsFromInputLine(splitFirstLine, hasParams));

                // Read headers until an empty line is encountered
                while (!line.isEmpty()) {
                    line = inputStream.readLine();
                    if (line.startsWith(CONTENT_TYPE)) {
                        setContentType(getContentTypeFromInputLine(line));
                    }
                    if (line.startsWith(CONTENT_LENGTH)) {
                        setContentLength(getContentLengthFromInputLine(line));
                    }
                    // Check if the current path requires authorization
                    if (line.startsWith(AUTHORIZATION) && requiresAuthorization(getMethod(), getPathname())) {
                        setAuthorization(getAuthorizationFromInputLine(line));
                    }
                }

                // Read request body for POST or PUT requests
                if (getMethod() == Method.POST || getMethod() == Method.PUT) {
                    int asciiChar;
                    for (int i = 0; i < getContentLength(); i++) {
                        asciiChar = inputStream.read();
                        String body = getBody();
                        setBody(body + ((char) asciiChar));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get the HTTP method from the input line
    private Method getMethodFromInputLine(String[] splitFirstLine) {
        return Method.valueOf(splitFirstLine[0].toUpperCase(Locale.ROOT));
    }

    // Get the pathname from the input line
    private String getPathnameFromInputLine(String[] splitFirstLine, Boolean hasParams) {
        if (hasParams) {
            return splitFirstLine[1].split("\\?")[0];
        }

        return splitFirstLine[1];
    }

    // Get the parameters from the input line
    private String getParamsFromInputLine(String[] splittedFirstLine, Boolean hasParams) {
        if (hasParams) {
            return splittedFirstLine[1].split("\\?")[1];
        }

        return "";
    }

    // Get the content type from the input line
    private String getContentTypeFromInputLine(String line) {
        return line.substring(CONTENT_TYPE.length());
    }

    // Get the content length from the input line
    private Integer getContentLengthFromInputLine(String line) {
        return Integer.parseInt(line.substring(CONTENT_LENGTH.length()));
    }

    // Get Authorization header value from the input line
    private String getAuthorizationFromInputLine(String line) {
        int startIndex = AUTHORIZATION.length();
        if (line.length() > startIndex) {
            return line.substring(startIndex);
        } else {
            return "";
        }
    }

    // method to check if a path requires authorization
    private boolean requiresAuthorization(Method method, String pathname) {

        if (method == Method.GET) {
            if (pathname.startsWith("/users/") || pathname.equals("/users") || pathname.equals("/cards") ||
                    pathname.equals("/deck") || pathname.equals("/stats") || pathname.equals("/scoreboard") ||
                    pathname.equals("/tradings")){
                return true;
            }
        } else if (method == Method.POST) {
            if (pathname.equals("/users") || pathname.equals("/sessions") || pathname.equals("/logout") ||
                    pathname.equals("/packages") || pathname.equals("/transactions/packages") || pathname.equals("/tradings") ||
                    pathname.startsWith("/tradings/") || pathname.equals("/battles")) {
                return true;
            }
        } else if (method == Method.PUT) {
            if (pathname.startsWith("/users") || pathname.equals("/deck")) {
                return true;
            }
        } else if (method == Method.DELETE){
            if (pathname.startsWith("/users/") || pathname.startsWith("/tradings/")) {
                return true;
            }
        }
        return false;
    }

}
