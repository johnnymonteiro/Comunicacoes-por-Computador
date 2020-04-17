import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.CRC32;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;  
import java.io.BufferedWriter;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.*;
import java.util.zip.CRC32;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;

class TransfereCC{
	private AgenteUDP agente;
	public HashMap<String,Tabela> tabelas;

	public TransfereCC(AgenteUDP agente,HashMap<String,Tabela> tabelas){
		this.agente = agente;
		this.tabelas = tabelas;
	}

	public void createTable(int transferType, String ip,String filename,int port,int flagTabela){
		Tabela tabela = new Tabela(transferType,filename,port,flagTabela);
		tabelas.put(ip,tabela);
	}

	public HashMap<String, Tabela> getTabelas(){
		return this.tabelas;
	}

	public boolean existePacoteIndex(int i,String ip){
		if(tabelas.get(ip).getPdus().size()>=i)
			return true;
		return false;
	}

	public int checkSumTest(Pdu p)throws Exception{
		Pdu ptest = new Pdu(p);
		CRC32 crcObject = new CRC32();
        byte[] temp = new byte[2000];
		temp = agente.toByteArray(ptest);
        crcObject.update(temp);
        if(crcObject.getValue() != p.getCrcValue()){
			System.out.println("ERROR: CRC");
			return 0;
        }
        return 1;
	}

	public int signatureTest(Pdu p)throws Exception{
		Pdu ptest = new Pdu(p);
		ptest.setSignature(null);
		ptest.setPublicKey(null);
        byte[] bytes = agente.toByteArray(ptest);;
        Signature sign = Signature.getInstance("SHA256withDSA");

        sign.initVerify(p.getPublicKey());
        sign.update(bytes);
      
      	boolean bool = sign.verify(p.getSignature());
      
      if(bool) {
         System.out.println("Signature verified");  
         return 1; 
      } else {
         System.out.println("Signature failed");
         return 0;
      }

}

	public void tabelaToFicheiro(Tabela tabela,String filename){
        try{
            FileWriter fw = new FileWriter(new File(filename));
			fw.write("Numero de sequencia inicial: "+tabela.getNseqInicial()+"\n");
            fw.write("\nTipo de transferencia: "+tabela.getTransferType()+"\n");
            fw.write("\nNome do ficheiro: "+tabela.getFileName()+"\n");
            fw.write("\nPacotes completos: "+tabela.getCompleted()+"\n");
            fw.write("\nPacotes Enviados:\n\n");
            for(Pdu p : tabela.getPdus()){
            	
            	fw.write("Nº Sequencia: "+(p.getNseq())+"\n");
            	fw.write("Nº Sequencia Ack: "+(p.getAckNseq())+"\n");
            	if(p.getFlag()==4){
            		String s = new String(p.getData());
            	
            	fw.write("Data: "+ s+"\n");
            }
            	fw.write("Flag: "+(p.getFlag())+"\n");
            	fw.write("CheckSum: "+(p.getCrcValue())+"\n");
            	fw.write("\n\n");
            
        	}
        	fw.write("\n\n\n\n");
        	fw.write("\nPacotes Recebidos:\n\n");
        	            for(Pdu p : tabela.getReceivedPdus()){
            	
            	fw.write("Nº Sequencia: "+(p.getNseq())+"\n");
            	fw.write("Nº Sequencia Ack: "+(p.getAckNseq())+"\n");
            	if(p.getFlag()==4){
            		String s = new String(p.getData());
            	
            	fw.write("Data: "+ s+"\n");
            }
            	fw.write("Flag: "+(p.getFlag())+"\n");
            	fw.write("CheckSum: "+(p.getCrcValue())+"\n");
            	fw.write("\n\n");
            
        	}
        	fw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void receiveSyn(Pdu pacote, String ip,int port)throws Exception{
    				 
    		if(pacote.getFlag() == 1){// SYN Upload 
				System.out.println("Received: Syn Upload (nseq= "+pacote.getNseq()+")");

				createTable(1,ip,pacote.getFileName(),port,0);//cria tabela para receber
				tabelas.get(ip).addReceivedPdu(pacote);
				try{
					DatagramSocket tsocket = new DatagramSocket();
					Pdu synAck = new Pdu(tabelas.get(ip).getNseqInicial(),pacote.getNseq()+1,2);
					tabelas.get(ip).addPdu(synAck);
					agente.sendPdu(synAck,ip,port,tsocket);
					TServerReceiver tserver = new TServerReceiver(tabelas,agente,tsocket,ip);
					tserver.start();
					TimerTask t = agente.timout(2000L,ip,synAck,port,tsocket);
          			tabelas.get(ip).addTimeout(synAck,t);
				}
				catch(Exception e){
					System.out.println("Thread Server socket failed!");
				}
			}


			else if(pacote.getFlag() ==0){	// SYN DOWNLOAD
					System.out.println("Received: Syn Download (nseq= "+pacote.getNseq()+")");
					createTable(0,ip,pacote.getFileName(),port,1); //cria tabela com pacotes gerados
					tabelas.get(ip).addReceivedPdu(pacote);
				try{
					DatagramSocket tsocket = new DatagramSocket();
					Pdu synAck = new Pdu(tabelas.get(ip).getNseqInicial(),pacote.getNseq()+1,2);
					tabelas.get(ip).addPdu(synAck);
					agente.sendPdu(synAck,ip,port,tsocket);
					TServerReceiver tserver = new TServerReceiver(tabelas,agente,tsocket,ip);
					tserver.start();
					TimerTask t = agente.timout(2000L,ip,synAck,port,tsocket);
          			tabelas.get(ip).addTimeout(synAck,t);
				}
				catch(Exception e){
					System.out.println("Thread Server socket failed!");
				}
					
    	}
	}


	public void receiveSynAck(Pdu pacote, String ip,int port)throws Exception{
		tabelas.get(ip).cancelTimout(pacote.getAckNseq()-1);
		tabelas.get(ip).updateNextNseq();
		System.out.println("Received: SynAck (nseq= "+pacote.getNseq()+")");
		tabelas.get(ip).addReceivedPdu(pacote);
		tabelas.get(ip).incrementCompleted();
		tabelas.get(ip).incrementCompleted();
		if(tabelas.get(ip).getTransferType()== 1){
			System.out.println("Starting upload transfer...");
			tabelas.get(ip).setPort(port);
			Pdu ack = new Pdu(pacote.getAckNseq(),-1,3);
			tabelas.get(ip).addPdu(ack);
			agente.sendPdu(ack,ip,port,agente.senderSocket);
			agente.sendData(tabelas.get(ip),ip,port,agente.senderSocket);

		}
		else if(tabelas.get(ip).getTransferType() == 0){

			Pdu ack = new Pdu(pacote.getAckNseq(),pacote.getNseq()+1,3);
			tabelas.get(ip).addPdu(ack);
			agente.sendPdu(ack,ip,port,agente.senderSocket);
			tabelas.get(ip).setPort(port);
			}
	}

	public void receiveAckSynAck(Pdu pacote, String ip,int port)throws Exception{
		//Ack do SynAck
			if(pacote.getFlag() == 3){
				tabelas.get(ip).addReceivedPdu(pacote);
				tabelas.get(ip).incrementCompleted();
				tabelas.get(ip).cancelTimout(tabelas.get(ip).getNextNSeq());
				System.out.println("Received: ACKSynAck (HANDSHAKE DONE) (nseq= "+pacote.getNseq()+")");
				if(tabelas.get(ip).getTransferType()==0){
					System.out.println("Starting download transfer...");
					TServerSender tserverSender = new TServerSender(tabelas.get(ip),agente,ip,null,4);
					tserverSender.start();

				}
			}
	}


	public void receiveDataClient(Pdu pacote, String ip,int port)throws Exception{
				System.out.println("Received: DATA (nseq= "+pacote.getNseq()+")...");
				tabelas.get(ip).addReceivedPdu(pacote);
				//tabelas.get(ip).addReceivedPdu(pacote);
				//tabelas.get(ip).addPdu(pacote);
				Random rand = new Random();
				int n = rand.nextInt(100);
				//if(n>50){
					tabelas.get(ip).updateNextNseq();
					Pdu dataAck = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,5);
					tabelas.get(ip).addPdu(dataAck);
					agente.sendPdu(dataAck,ip,tabelas.get(ip).getPort(),agente.senderSocket);
	}

	public void receiveDataServer(Pdu pacote, String ip,int port)throws Exception{
				System.out.println("Received: DATA (nseq= "+pacote.getNseq()+")...");
				tabelas.get(ip).addReceivedPdu(pacote);
				Random rand = new Random();
				int n = rand.nextInt(100);
				//if(n>50){
				tabelas.get(ip).updateNextNseq();
					Pdu dataAck = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,5);
					
					TServerSender sender = new TServerSender (tabelas.get(ip),agente,ip,dataAck,1);
					sender.start();

	}


	public void receiveDataAck(Pdu pacote, String ip,int port)throws Exception{
		if(pacote.getFlag() == 5){ //Ack de data
			tabelas.get(ip).addReceivedPdu(pacote);

		System.out.println("Received: DATA ACK (nseq= "+pacote.getNseq()+ "   Stop timeout de: "+ (pacote.getAckNseq()-1)+")");
		//incrementa o nº dos pacotes completos (na fase 2, em vez disto, meter aqui a controlo de erros com os nseq)

		tabelas.get(ip).cancelTimout(pacote.getAckNseq()-1);
		tabelas.get(ip).incrementCompleted();

		//se for o ultimo ack de data na transferencia ele envia o FYN
		if(tabelas.get(ip).getCompleted()==tabelas.get(ip).getNumberOfPdus()){

			System.out.println("Transferencia Completa");
			if(tabelas.get(ip).getTransferType()==0){
				tabelas.get(ip).updateNextNseq();
				Pdu fyn = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,6);
				TServerSender sender = new TServerSender (tabelas.get(ip),agente,ip,fyn,0);
				sender.start();
			}
			if(tabelas.get(ip).getTransferType()==1){
				tabelas.get(ip).updateNextNseq();
				Pdu fyn = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,6);
				tabelas.get(ip).addPdu(fyn);
				agente.sendPdu(fyn,ip,tabelas.get(ip).getPort(),agente.senderSocket);
				TimerTask t = agente.timout(2000L,ip,fyn,tabelas.get(ip).getPort(),agente.senderSocket);
          		tabelas.get(ip).addTimeout(fyn,t);
			}
		}
			//agente.send(pacote.getNseq()+1,6,ip,pacote.getFileName());
			}
	}

	public void receiveFyn(Pdu pacote, String ip,int port)throws Exception{
			tabelas.get(ip).addReceivedPdu(pacote);
			
			if(tabelas.get(ip).getTransferType()==0){
				tabelas.get(ip).updateNextNseq();
				Pdu ack = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,7);
				tabelas.get(ip).addPdu(ack);
				agente.sendPdu(ack,ip,tabelas.get(ip).getPort(),agente.senderSocket);
				TimerTask t = agente.timout(2000L,ip,ack,tabelas.get(ip).getPort(),agente.senderSocket);
          		tabelas.get(ip).addTimeout(ack,t);

			}
			if(tabelas.get(ip).getTransferType()==1){
				tabelas.get(ip).updateNextNseq();
				Pdu ack = new Pdu(tabelas.get(ip).getNextNSeq(),pacote.getNseq()+1,7);
			TServerSender sender = new TServerSender (tabelas.get(ip),agente,ip,ack,0);
			sender.start();


			}
	}

	public void receiveFynAck(Pdu pacote, String ip,int port)throws Exception{

			tabelas.get(ip).cancelTimout(pacote.getAckNseq()-1);
			tabelas.get(ip).addReceivedPdu(pacote);
			/////////////////////////
			if(tabelas.get(ip).getTransferType()==0){
			tabelas.get(ip).updateNextNseq();
			Pdu ack = new Pdu(tabelas.get(ip).getNextNSeq(),-1,8);
			TServerSender sender = new TServerSender (tabelas.get(ip),agente,ip,ack,1);
			sender.start();
			for(Pdu p :tabelas.get(ip).getReceivedPdus()){
			System.out.println("Pacote Recebido: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
		}

				for(Pdu p :tabelas.get(ip).getPdus()){
			System.out.println("Pacote Enviado: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
			tabelaToFicheiro(tabelas.get(ip),"ServerTable.txt");
		}

			}
			if(tabelas.get(ip).getTransferType()==1){
				tabelas.get(ip).updateNextNseq();
				Pdu ack = new Pdu(tabelas.get(ip).getNextNSeq(),-1,8);
				tabelas.get(ip).addPdu(ack);
				agente.sendPdu(ack,ip,tabelas.get(ip).getPort(),agente.senderSocket);
				for(Pdu p :tabelas.get(ip).getReceivedPdus()){
			System.out.println("Pacote Recebido: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
		}

				for(Pdu p :tabelas.get(ip).getPdus()){
			System.out.println("Pacote Enviado: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
		}
			tabelaToFicheiro(tabelas.get(ip),"ClientTable.txt");
		    System.exit(0);
			}

	}			
			

	public void receiveAckFynAck(Pdu pacote, String ip,int port)throws Exception{

			tabelas.get(ip).cancelTimout(tabelas.get(ip).getNextNSeq());
			tabelas.get(ip).addReceivedPdu(pacote);
			for(Pdu p :tabelas.get(ip).getReceivedPdus()){
			System.out.println("Pacote Recebido: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
		}

				for(Pdu p :tabelas.get(ip).getPdus()){
			System.out.println("Pacote Enviado: "+p.getNseq()+"   "+p.getAckNseq()+"    "+p.getFlag());
		}
		if(tabelas.get(ip).getTransferType()==0){
			tabelaToFicheiro(tabelas.get(ip),"ClientTable.txt");
			tabelas.get(ip).construirFicheiro();
			System.exit(0);
		}
		if(tabelas.get(ip).getTransferType()==1){
			tabelas.get(ip).construirFicheiro();
			tabelaToFicheiro(tabelas.get(ip),"ServerTable.txt");
		}
			

	}


	public void serverDownloadMode(Pdu pacote, String ip,int port)throws Exception{
		if(pacote.getFlag() == 5){
			receiveDataAck(pacote,ip,port);
		}
		if(pacote.getFlag() ==7){
			receiveFynAck(pacote,ip,port);
		}
	}

	public void serverUploadMode(Pdu pacote, String ip,int port)throws Exception{
		
		if(pacote.getFlag() == 4){
			receiveDataServer(pacote,ip,port);
		}
		if(pacote.getFlag() ==6){
			receiveFyn(pacote,ip,port);
		}
		if(pacote.getFlag() == 8){
			receiveAckFynAck(pacote,ip,port);
		}
	}

	public void evalClientPdu(Pdu pacote, String ip,int port)throws Exception{
		if(checkSumTest(pacote)==1){
			if(pacote.getFlag() == 2)
				receiveSynAck(pacote,ip,port);
			if(pacote.getFlag() == 4)
				receiveDataClient(pacote,ip,port);
			if(pacote.getFlag() == 5)
				receiveDataAck(pacote,ip,port);
			if(pacote.getFlag() == 6)
				receiveFyn(pacote,ip,port);
			if(pacote.getFlag() == 7)
				receiveFynAck(pacote,ip,port);
			if(pacote.getFlag() == 8)
				receiveAckFynAck(pacote,ip,port);
			
		}
	}


}