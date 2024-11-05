package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
//    public static final int client_count = 2;
    public static final int client_count = Integer.parseInt(System.getenv("CLIENT_COUNT"));

//    public static final String filePath = "D:\\INL\\RnD\\middle-controller\\middle-controller\\middle-controller\\parameters\\";

    public static final String filePath = System.getenv("FILE_PATH");
    public static int client_id = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        MasterHandler masterHandler = null;
        List<FileHandler> fileHandlerList = new ArrayList<>();

        ServerSocket serverSocket = new ServerSocket((9090));
        System.out.println(logMessage("Server started. Waiting for connections..."));

        for (int i = 0; i < client_count + 1; i++) {
            Socket socket = serverSocket.accept();
            String clientIp = socket.getInetAddress().getHostAddress();

            if (clientIp.equals("127.0.0.1")) {
                masterHandler = new MasterHandler(socket, client_count);
                System.out.println(logMessage("Server Accepted Socket: " + socket.getInetAddress() + " as Master"));
            } else {
                FileHandler fileHandler = new FileHandler(socket, client_id);
                fileHandlerList.add(fileHandler);
                // 클라이언트별로 고유번호 할당하기
                fileHandler.sendClientId(client_id);
                System.out.println(logMessage("Server Accepted Socket: " + socket.getInetAddress() + "\t Set Client id : " + client_id));
                client_id++;
            }
        }

        int round = 0;
        int exit_code = 0;
        while (true) {
            round++;

            // 시작 메시지가 온 것을 확인
            while (true) {
                String msg = masterHandler.getMessage();

                if (msg.equals("start")) {    // mp.deploy_weight
                    System.out.println(logMessage("\tRound " + round + " start"));
                    System.out.println(logMessage("Master Deploy weights to clients"));

                    // 클라이언트에 파일을 동시에 전송
                    sendUpdateToClients(fileHandlerList, "global_model.pt");
                    break;
                } else if (msg.equals("end")) {
                    for (FileHandler fileHandler : fileHandlerList) {
                        fileHandler.sendEnd();
                    }
                    exit_code = 1;
                    break;
                }
            }

            if (exit_code == 1) {
                return;
            }

            System.out.println(logMessage("Master Wait receiving client_models"));

            // 클라이언트로부터 학습 .pt 파일을 동시에 받기
            receiveFilesFromClients(fileHandlerList, filePath);

            // 볼륨에 새로운 가중치 파일이 있다고 MP에게 알린다. (mp.average_weight)
            masterHandler.alertMP("Master uploaded weight file");

            // MP에서 가중치 평균화 끝내는 것을 기다린다. mp.average_weight
            masterHandler.getCompleteMessage();
        }
    }

    public static String logMessage(String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS");
        String time = formatter.format(new Date());  // 현재 시간을 mm:ss로 포맷
        return "[" + time + "] " + message;
    }

    public static void sendUpdateToClients(List<FileHandler> fileHandlerList, String fileName) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(fileHandlerList.size());
        CountDownLatch latch = new CountDownLatch(fileHandlerList.size());

        for (FileHandler fileHandler : fileHandlerList) {
            executor.submit(() -> {
                try {
                    fileHandler.sendUpdatePt(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 클라이언트에 파일 전송이 완료될 때까지 대기
        executor.shutdown();
    }

    public static void receiveFilesFromClients(List<FileHandler> fileHandlerList, String filePath) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(fileHandlerList.size());
        CountDownLatch latch = new CountDownLatch(fileHandlerList.size());

        for (FileHandler fileHandler : fileHandlerList) {
            executor.submit(() -> {
                try {
                    String saveFilePath = filePath + "client_model_" + fileHandler.getClientId() + ".pt";
                    fileHandler.receiveFile(saveFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 클라이언트로부터 파일 수신이 완료될 때까지 대기
        executor.shutdown();
    }
}
