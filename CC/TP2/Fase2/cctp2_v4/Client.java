import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

class Client{

     public static void main(String args[]) throws Exception{
      int serv = Integer.parseInt(args[3]);
      HashMap<String,Tabela> tabelas = new HashMap<>();
      boolean estado = true;
      
      try{
          DatagramSocket clientSocket = new DatagramSocket();
          AgenteUDP agente = new AgenteUDP(serv,tabelas,clientSocket);
        if (args[0].equals("download")){ // download filename ipDestino
         agente.init(0, args[1],args[2],clientSocket);

         Listener listener = new Listener(tabelas,agente,clientSocket,estado);

      }
      else if (args[0].equals("upload")){ // upload filename ipDestino
        agente.init(1, args[1],args[2],clientSocket);
        Listener listener = new Listener(tabelas,agente,clientSocket,estado);
      }
      else System.out.println("Command Usage: download/upload filename ip port");
      }
      catch(Exception e){
          System.out.println("Client Socket error");
      }
   }


}