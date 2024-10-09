package org.example;



import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class FileHandler {
    public final Socket clientSocket;
    BufferedReader reader;


    public FileHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        InputStream inputStream = clientSocket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public String getCompleteMessage() throws IOException {
        String msg = reader.readLine().trim();
        return msg;
    }


    public void sendUpdatePt(String msg) throws IOException {
        write(clientSocket, msg);
    }

    public void sendClientId(int id) throws IOException {
        write(clientSocket, Integer.toString(id));
    }

    public void sendEnd() throws IOException {
        write(clientSocket,"end");
    }


//    public void run() {
//        try {
//            InputStream inputStream = clientSocket.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//
//            while (true) {
//                write(clientSocket, Integer.toString(MasterHandler.round));
//                System.out.println("[FileHandler] : 클라이언트에게 라운드 수 정보를 줍니다. : " + MasterHandler.round);
//
//                String msg = reader.readLine().trim();
//                System.out.println("[FileHandler] : 클라이언트로부터 온 데이터 : " + msg);
//
//                if (msg.equals("complete")) {
//                    break;
//                }
//            }
//
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    public void write(Socket clientSocket, String msg) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        outputStream.write(msg.getBytes());
        outputStream.flush();
    }

    public void getEnd() throws IOException {
        String msg = reader.readLine().trim();
        System.out.println("Progam End : " + msg);
    }
}
