package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    public static final int client_count = 1;

//    public static final String filePath = "/mnt/parameters/";

    public static final String filePath = "D:\\INL\\RnD\\middle-controller\\middle-controller\\middle-controller\\parameters\\";

    public static int client_id = 1;

    // id list

    public static void main(String[] args) throws IOException {

         MasterHandler masterHandler = null;
         List<FileHandler> fileHandlerList = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(9090)) {

            System.out.println(logMessage("Server started. Waiting for connections..."));


            for(int i =0; i<client_count+1;i++){
                Socket clientSocket = serverSocket.accept();

                if(i==0){
                    masterHandler = new MasterHandler(clientSocket,client_count) ;
                }
                else{
                    FileHandler fileHandler = new FileHandler(clientSocket,client_id);
                    fileHandlerList.add(fileHandler);

                    // 클라이언트별로 고유번호 할당하기
                    fileHandler.sendClientId(client_id);
                    client_id++;
                }
                System.out.println(logMessage("Accepted Socket: " + clientSocket.getInetAddress()));
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        int exit_code = 0;
        while(true){
            // 시작 메시지가 온 것을 확인

            while(true){
                String msg =masterHandler.getMessage();

                if(msg.equals("start")){    // mp.deploy_weight
                    for(FileHandler fileHandler : fileHandlerList){
                        fileHandler.sendUpdatePt("global_model.pt");   // cp.getUpdatePT
                    }
                    break;
                }

                else if(msg.equals("end")){
                    for(FileHandler fileHandler : fileHandlerList){
                        fileHandler.sendEnd();
                    }
                    exit_code = 1;
                    break;
                }
            }

            if(exit_code == 1) {
                for(FileHandler fileHandler : fileHandlerList){
                    fileHandler.getEnd();
                }
            }

            // 클라이언트로부터 학습 .pt 파일 받기
            for(FileHandler fileHandler : fileHandlerList){
                String saveFilePath = filePath + "client_model_"+fileHandler.getClientId() + ".pt";
                fileHandler.receiveFile(saveFilePath);
                System.out.println(logMessage("File received and saved to " + saveFilePath));
            }


            // 볼륨에 새로운 가중치 파일이 있다고 MP에게 알린다. (mp.average_weight)
            masterHandler.alertMP("uploaded weight file");

            // MP에서 가중치 평균화 끝내는 것을 기다린다. mp.average_weight
            masterHandler.getCompleteMessage();

        }

    }

    public static String logMessage(String message){
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String time = formatter.format(new Date());  // 현재 시간을 mm:ss로 포맷
        String msg = "[" + time + "] " + message;
        return msg;
    }
}
