package org.example;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class FileHandler {

    public final Socket clientSocket;

    private int clientId;

    BufferedReader reader;


    public String getClientId(){
        return Integer.toString(clientId);
    }


    public FileHandler(Socket socket, int clientId) throws IOException {
        this.clientSocket = socket;
        this.clientId = clientId;
        InputStream inputStream = clientSocket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public String receiveMessage() throws IOException {
        String msg = reader.readLine().trim();
        return msg;
    }


    public void receiveFile(String saveFilePath) throws IOException {
        // 클라이언트로부터 데이터 수신
        InputStream is = clientSocket.getInputStream();
        DataInputStream dis = new DataInputStream(is);

        // 파일 전송 준비 메시지 수신
//        byte[] msgBuffer = new byte[1024];
//        int messageLength = dis.read(msgBuffer);
//        String message = new String(msgBuffer, 0, messageLength, "UTF-8");

        String message = receiveMessage();

        // 클라이언트로부터 "READY_TO_SEND_FILE" 메시지가 도착하면 파일 수신 시작
        if (message.equals("READY_TO_SEND_FILE")) {
            System.out.println(Main.logMessage("Client" + clientId  +" is ready to send file"));

            writeMessage(clientSocket,"ack");

            // 파일 크기 수신
            long fileSize = dis.readLong();
            System.out.println(Main.logMessage("Client" + clientId  + " Receiving file size : " + fileSize + " bytes"));

            // 파일 저장 경로 설정
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long receivedBytes = 0;

            // 파일 크기만큼 데이터를 수신
            while (receivedBytes < fileSize) {
                bytesRead = is.read(buffer, 0, (int) Math.min(buffer.length, fileSize - receivedBytes));
                if (bytesRead == -1) {
                    break;
                }
                fos.write(buffer, 0, bytesRead);
                receivedBytes += bytesRead;
            }

            // 파일 저장 종료
            fos.close();
            System.out.println(Main.logMessage("Client" + clientId  + " File received and saved to: " + saveFilePath));

            // InputStream에서 남은 데이터를 비우는 과정 (만약 남아 있을 경우)
            while (is.available() > 0) {
                is.read(buffer);  // 남은 데이터를 모두 소비하여 비움
            }
        } else {
            System.out.println(Main.logMessage("Client" + clientId  + " Unexpected message received: " + message));
        }
    }



    public void sendUpdatePt(String fileName) throws IOException {
        writeMessage(clientSocket,fileName); // client.recvFile.s.recv(1024)
        String ack = receiveMessage();
        if(ack.equals("ack")){
            File file = new File(Main.filePath + fileName);

            writeFile(clientSocket, file);
        }

    }


    public void sendClientId(int id) throws IOException {
        writeMessage(clientSocket, Integer.toString(id));
    }

    public void sendEnd() throws IOException {
        writeMessage(clientSocket,"end");
    }

    public void writeMessage(Socket clientSocket, String msg) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        outputStream.write(msg.getBytes());
        outputStream.flush();
    }

    public void writeFile(Socket clientSocket, File file) throws IOException {

        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        dos.writeLong(file.length());   // 파일크기 보내기

        System.out.println(Main.logMessage("Client" + clientId  +" file size : " + file.length()));

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }

        dos.flush();
    }

    public void getEnd() throws IOException {
        String msg = reader.readLine().trim();
        System.out.println("Client" + clientId  +" Program End : " + msg);
    }
}
