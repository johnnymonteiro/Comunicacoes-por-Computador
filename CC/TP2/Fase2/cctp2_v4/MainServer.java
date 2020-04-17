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

public class MainServer{


   public static void main(String args[]) throws Exception{
            int sSocket = Integer.parseInt(args[0]);
            DatagramSocket serverSocket = new DatagramSocket(sSocket);
            byte[] data = new byte[3000];

            HashMap<String,Tabela> tabelas = new HashMap<>();
            AgenteUDP agente = new AgenteUDP(sSocket,tabelas,null);
            while(true)
               { 
 
                  DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                  serverSocket.receive(receivePacket);
                  

                  Pdu pacote = (Pdu)agente.toObject(receivePacket.getData());
                   System.out.println("MainServer: Pacote recebido: Nseq: "+ pacote.getNseq() + "   AckNseq: " +pacote.getAckNseq()+ "   Flag: " +pacote.getFlag());
                  if(agente.transfereCC.checkSumTest(pacote) == 1 && agente.transfereCC.signatureTest(pacote) == 1){
                 
                  String ip = receivePacket.getAddress().toString().substring(1);
                  agente.transfereCC.receiveSyn(pacote,ip,receivePacket.getPort());
                  }
                  }
         }
}


