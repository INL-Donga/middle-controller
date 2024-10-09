package org.example;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class MasterHandler {
    private final Socket clientSocket;
    public static int round;
    private int client_count;
    BufferedReader reader;


    public MasterHandler(Socket socket,  int client_count) throws IOException {
        this.clientSocket = socket;
        this.client_count = client_count;
        InputStream inputStream = clientSocket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }


    // MP에게 클라이언트로부터 학습이 완료되었고 볼륨에 저장되어있다는 것을 알림
    public void alertMP(String msg) throws IOException {
        write(clientSocket,msg);
    }


    // 클라이언트에게 새로운 가중치 파일이 있다는 것을 알린다.
    public void alertCP(Socket socket,String msg) throws IOException {
        write(socket, msg);
    }

    public String getCompleteMessage() throws IOException {
        String msg = reader.readLine().trim();
        return msg;
    }

    public String getMessage() throws IOException {
        String msg = reader.readLine().trim();

        if(msg.equals("start")){
            return "start";
        }
        else if(msg.equals("end")){
            return "end";
        }

        return "error";
    }


    public void write(Socket clientSocket, String msg) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        outputStream.write(msg.getBytes());
        outputStream.flush();
    }

}
