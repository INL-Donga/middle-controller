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

    public String getCompleteMessage() throws IOException {
        String msg = reader.readLine().trim();
        return msg;
    }

    public void receiveFile(String saveFilePath) throws IOException {
        // 파일 저장 경로 설정

        FileOutputStream fos = new FileOutputStream(saveFilePath);

        // 클라이언트로부터 데이터 수신
        InputStream is = clientSocket.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fos.close();
    }


    public void sendUpdatePt(String fileName) throws IOException {
        writeMessage(clientSocket,fileName); // client.recvFile.s.recv(1024)

        File file = new File(Main.filePath + fileName);

        writeFile(clientSocket, file);

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

        System.out.println(Main.logMessage("file size : " + Long.toString(file.length())));

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
        System.out.println("Program End : " + msg);
    }
}
