package org.example;



import java.io.*;
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
        try (InputStream inputStream = clientSocket.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath)) {

            // 버퍼를 이용해 데이터를 4096 바이트씩 읽음
            byte[] buffer = new byte[4096];
            int bytesRead;

            // 파일 데이터를 수신하고 저장
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.flush();
            System.out.println("File received and saved to: " + saveFilePath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IOException("파일 수신 중 오류 발생", e);
        }
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

    public void writeFile(Socket clientSocket, File file) throws FileNotFoundException {


        // 파일이 존재하는지 확인
        if (!file.exists()) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + file.getName());
        }

        // 파일을 읽기 위한 InputStream 생성
        try (InputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = clientSocket.getOutputStream()) {

            // 버퍼를 이용해 파일 데이터를 읽고 전송
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // 파일 내용을 소켓으로 전송
            }

            outputStream.flush(); // 모든 데이터 전송 후 flush
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getEnd() throws IOException {
        String msg = reader.readLine().trim();
        System.out.println("Program End : " + msg);
    }
}
