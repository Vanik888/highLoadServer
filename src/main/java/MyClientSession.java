import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vanik on 23.02.14.
 */
public class MyClientSession implements Runnable {
    private static final String DEFAULT_PATH = "DOCUMENT_ROOT";
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    public MyClientSession(Socket socket) {
        this.socket = socket;
        try {
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("can not get i/o stream");
        }
    }
    public void run() {
        try {
            String header = readHeader();
            String method = readMethod(header);
            if(!method.equals("GET") && !method.equals("HEAD")) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("HTTP/1.1 405 Method Not Allowed\n");
                buffer.append("Server: JavaServer\n");
                buffer.append("Date: " + new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date())+"\n");
                buffer.append("Connection: close\r\n");
                buffer.append("\r\n");
                PrintStream answer = new PrintStream(os, true, "utf-8");
                answer.print(buffer.toString());
            } else {
                String url = findFilePath(header);
                int status = getStatus(DEFAULT_PATH + url);
                long contentLength = getContentLength(url);
                String contentType = getContentType(url);
                String responseHeader = creatingHeader(status, contentLength, contentType);
                PrintStream answer = new PrintStream(os, true, "utf-8");
                System.out.print("Created Response \n" + responseHeader);
                answer.print(responseHeader);
                if(method.equals("GET") && status == 200) {
                    InputStream inputStream = MyClientSession.class.getResourceAsStream(DEFAULT_PATH+url);
                    int count = 0;
                    byte[] bytes = new byte[1024];
                    while((count = inputStream.read(bytes)) != -1) {
                        os.write(bytes);
                    }
                } else if (method.equals("GET") && status == 404) {

                }
                if(method == "HEAD") {

                }

            }
            System.out.print("header = " + header+ "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readMethod(String firstLine) {
        int from = 0;
        int to = firstLine.indexOf(" ");
        return firstLine.substring(from,to);
    }

    private String readHeader() throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String ln = null;
        ln = reader.readLine();
        while (ln != null && ln.length() != 0) {
            System.out.println("ln = " + ln);
            builder.append(ln+ "\n");
            ln = reader.readLine();
        }
        return builder.toString();
    }
    private String findFilePath(String header) {

        String url = getUrl(header);
        url = myUrlDecoder(url);
        url = removeQuery(url);
        url = removeDepricatedSymbols(url);
        int from;
        if (url.charAt(url.length()-1) == '/') {
            url=url+"index.html";
        } else {
            from = url.length()-1;
            int i  = from;
            boolean pointIsFound = false;
            while (i>=0 && !pointIsFound) {
                if (url.charAt(i) == '.'){
                    pointIsFound = true;
                }
                i--;
            }
            if (!pointIsFound)
                url = url+"/index.html";
        }
        System.out.println("FIle url"+url);
        return url;
    }
    private String removeQuery(String url) {
        if (url.indexOf('?') != -1) {
            url = url.substring(0,url.indexOf('?'));
        }
        return url;
    }
    private String myUrlDecoder(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
    private String removeDepricatedSymbols(String url) {
        boolean checked = false;
        int from;
        while (!checked) {
            if((from = url.indexOf("../") )!=-1) {
                url = url.substring(0,from)+url.substring(from+3,url.length());
            } else checked = true;
        }
        return url;
    }
    private String getUrl(String header) {
        int from = header.indexOf(" ")+1;
        int to = header.indexOf("HTTP/1.", from)-1;
        return header.substring(from, to);
    }
    private int getStatus(String url) {
        InputStream inputStream = MyClientSession.class.getResourceAsStream(url);
        return inputStream != null ? 200 : 404;
    }

    private String creatingHeader(int status, long contentLength, String contentType) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.1 " + status + "\n");
        buffer.append("Server: JavaServer\n");
        buffer.append("Date: " + new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date())+"\n");
        buffer.append("Content-Length: " + contentLength +"\n");
        buffer.append("Content-Type: " + contentType +"\n");
        buffer.append("Connection: close\n");
        buffer.append("\n");

        return buffer.toString();
    }
    private String getContentType(String url) {
        int len = url.length()-1;
        int i = len;
        String ct = null;
        boolean ctFinded = false;
        while (i >= 0 && !ctFinded) {
            if (url.charAt(i) == '.') {
                ctFinded = true;
                ct = url.substring(i+1,len+1);
            }
            i--;
        }
        System.out.println("url contentType = " + ct);
        return getFullContentType(ct);
    }
    private String getFullContentType(String end) {
        String contentType = null;
        if(end.equals("css")){
            contentType = "text/css";
        } else
        if(end.equals("gif")){
            contentType = "image/gif";
        } else
        if(end.equals("html")){
            contentType = "text/html";
        } else
        if(end.equals("jpeg")){
            contentType = "image/jpeg";
        } else
        if(end.equals("jpg")){
            contentType = "image/jpeg";
        } else
        if(end.equals("js")){
            contentType = "text/javascript";
        } else
        if(end.equals("png")){
            contentType = "image/png";
        } else
        if(end.equals("swf")){
            contentType = "application/x-shockwave-flash";
        }
        return contentType;
    }
    private long getContentLength(String url) {
        String filePath = "./src/main/resources/"+DEFAULT_PATH+url;
        System.out.println(filePath);
        File requstedFile = new File(filePath);
        return requstedFile.length();
    }
}