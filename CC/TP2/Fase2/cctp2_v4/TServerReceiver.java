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

public class TServerReceiver extends Thread{
		private HashMap<String,Tabela> tabelas;
		private AgenteUDP agente;
		private DatagramSocket tsocket;
      private String ip;
	public TServerReceiver(HashMap<String,Tabela> tabelas, AgenteUDP agente, DatagramSocket tsocket,String ip){
		this.tabelas = tabelas;
		this.agente = agente;
		this.tsocket = tsocket;
      this.ip = ip;
	}

	public void run()  {
        try{
         System.out.println("TServerReceiver Started");
            byte[] data = new byte[3000];
            AgenteUDP agente = new AgenteUDP(0,tabelas,null);
            boolean handshake = true;
                  while(handshake){
                  DatagramPacket ackSynAckPacket = new DatagramPacket(data, data.length);
                  tsocket.receive(ackSynAckPacket);
                  Pdu ackSynAck = (Pdu)agente.toObject(ackSynAckPacket.getData());
                  System.out.println("TServerReceiver: Pacote recebido: Nseq: "+ ackSynAck.getNseq() + "   NseqAck: " +ackSynAck.getAckNseq()+ "   Flag: " +ackSynAck.getFlag());
                  if(agente.transfereCC.checkSumTest(ackSynAck) == 1 && agente.transfereCC.signatureTest(ackSynAck) == 1){
                  
                  String ackip = ackSynAckPacket.getAddress().toString().substring(1);
                  
                  if(ackSynAck.getFlag() == 3)
                     handshake = false;
                  agente.transfereCC.receiveAckSynAck(ackSynAck,ackip,ackSynAckPacket.getPort());
                  }
               }
          System.out.println("TServerReceiver: Starting Transfer");
         if(tabelas.get(ip).getTransferType()==0){
            
               while(true) {


                  DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                  tsocket.receive(receivePacket);

                  Pdu pacote = (Pdu)agente.toObject(receivePacket.getData());
                  if(agente.transfereCC.checkSumTest(pacote) == 1 && agente.transfereCC.signatureTest(pacote) == 1){
                  String ip = receivePacket.getAddress().toString().substring(1);
                  System.out.println("TServerReceiver: Pacote recebido: Nseq: "+ pacote.getNseq() + "   NseqAck: " +pacote.getAckNseq()+ "   Flag: " +pacote.getFlag());
                  agente.transfereCC.serverDownloadMode(pacote,ip,receivePacket.getPort());
                  }
      		}
         }

         if(tabelas.get(ip).getTransferType()==1){
                  while(true) {
                  DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                  tsocket.receive(receivePacket);

                  Pdu pacote = (Pdu)agente.toObject(receivePacket.getData());
                  if(agente.transfereCC.checkSumTest(pacote) == 1 && agente.transfereCC.signatureTest(pacote) == 1){
                  String ip = receivePacket.getAddress().toString().substring(1);
                  System.out.println("TServerReceiver: Pacote recebido: Nseq: "+ pacote.getNseq() + "   NseqAck:" +pacote.getAckNseq()+ "   Flag:" +pacote.getFlag());
                  agente.transfereCC.serverUploadMode(pacote,ip,receivePacket.getPort());
               }
      	  }
         }
      }
      		catch(Exception e){}
        
      	
    }

}


