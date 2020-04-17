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

public class TServerSender extends Thread{
		private Tabela tabela;
		private AgenteUDP agente;
		private DatagramSocket tsocket;
      private String ip;
      private Pdu pacote;
      private int tflag;
	public TServerSender(Tabela tabela, AgenteUDP agente,String ip,Pdu pacote ,int tflag){
		this.tabela = tabela;
		this.agente = agente;
		this.tsocket = tsocket;
      this.ip = ip;
      this.tflag = tflag;
      this.pacote = pacote;
	}

	public void run()  {
        try{
                AgenteUDP agente = new AgenteUDP(0,null,null);
            if(tflag == 4){
               byte[] data = new byte[3000];
               tsocket = new DatagramSocket();
               System.out.println("TServerSender: Sending Data packages");
               agente.sendData(tabela,ip,tabela.getPort(),tsocket);
            }
            else{
               byte[] data = new byte[3000];
               tsocket = new DatagramSocket();
               System.out.println("TServerSender: A enviar pacote: Nseq: "+ pacote.getNseq() + "   NseqAck: " +pacote.getAckNseq()+ "   Flag: " +pacote.getFlag());
               tabela.addPdu(pacote);
               agente.sendPdu(pacote,ip,tabela.getPort(),tsocket);
               if(tflag == 0){
               TimerTask t = agente.timout(2000L,ip,pacote,tabela.getPort(),tsocket);
               tabela.addTimeout(pacote,t);
               }
            }
      	}
      		catch(Exception e){}
        
      	
    }
 }