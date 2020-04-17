import java.util.HashMap;
import java.util.ArrayList;
import java.util.zip.CRC32;

class TransfereCC{
	private AgenteUDP agente;
	private HashMap<String,Tabela> tabelas;

	public TransfereCC(AgenteUDP agente){
		this.agente = agente;
		tabelas = new HashMap<>();
	}

	public void addTable(int nseq, String ip,int type, String filename, ArrayList<Pdu> pdus, int completed){
		tabelas.put(ip,new Tabela(nseq,type,filename,pdus,completed));
	}

	public void addTable(int nseq, String ip,int type, String filename){
		tabelas.put(ip,new Tabela(nseq,type,filename));
	}

	public HashMap<String, Tabela> getTabelas(){
		return this.tabelas;
	}

	public void evaluate(Pdu pacote,String ip)throws Exception{
		byte[] bytes = null;
		CRC32 crc32 = new CRC32();

		//calcula o crc32 do pacote original sem o crc que vem la metido 
		crc32.update(agente.toByteArray(pacote.pacoteSemCrc()));
		if(pacote.getCrc()==crc32.getValue()){
			//System.out.println("crc value ao receber: "+crc32.getValue());
			if(pacote.getFlag() == 0 || pacote.getFlag()==1){ // SYN
				System.out.println("Received: SYN (nseq= "+pacote.getNseq()+")");
				if(pacote.getFlag() == 1) 
					addTable(pacote.getNseq(),ip,pacote.getFlag(), pacote.getFileName());
				else if(pacote.getFlag()==0)
					addTable(pacote.getNseq(),ip,pacote.getFlag(), pacote.getFileName(),agente.parteFicheiro(pacote.getFileName(),pacote.getNseq()), 0);
				agente.sendSYNAck(pacote.getNseq()+1, 2, pacote.getFlag(),ip,pacote.getFileName()); // SYnAck
			}
			else if(pacote.getFlag() == 2){ // SYNAck
				System.out.println("Received: SYNACK");
				if(pacote.getTransferType()==1){
					System.out.println("Starting upload transfer...");
					addTable(pacote.getNseq(),ip,pacote.getTransferType(), pacote.getFileName(),agente.parteFicheiro(pacote.getFileName(),pacote.getNseq()),0);
					agente.sendAck(pacote.getNseq()+1, 3,ip); // ack
					agente.sendPdu(tabelas.get(ip).getPdu(0),ip);
				}
				else if(pacote.getTransferType()==0){
					//IMPORTANTE: na situaçao de rede local (2pcs), usa-se esta linha comentada abaixo em vez da seguinte que nao está comentada. tens de usar a que nao está comentada quando tas a testar as cenas num só pc, para nao dar erro com as tables no download
					//addTable(pacote.getNseq(),ip,pacote.getTransferType(), tabelas.get(ip).getFileName()); 
					addTable(pacote.getNseq(),ip,pacote.getTransferType(), pacote.getFileName(),agente.parteFicheiro(tabelas.get(ip).getFileName(),pacote.getNseq()),0);
					agente.sendAck(pacote.getNseq()+1, 3,ip); // ack
				}
			}
			else if(pacote.getFlag() == 3){//Ack do SynAck
				System.out.println("Received: ACK (HANDSHAKE DONE)");
				if(tabelas.get(ip).getType()==0){
					System.out.println("Starting download transfer...");
					agente.sendPdu(tabelas.get(ip).getPdu(0),ip);
				}
			}
			else if(pacote.getFlag() == 4){ //Data transfer
				//bytes = agente.toByteArray(pacote);
				System.out.println("Received: DATA (nseq= "+pacote.getNseq()+")...");
				/*for (int i = 0; i < bytes.length; i++) { // so pra ver o pacote (aqui é que metemos o controlo de erros no pacote que recebeu, na fase2)
       	        System.out.print(bytes[i]);
       	     	} System.out.println(" ")*/
				agente.sendAck(pacote.getNseq()+1, 5,ip);
			}
			else if(pacote.getFlag() == 5){ //Ack de data
				System.out.println("Received: DATA ACK");
				//incrementa o nº dos pacotes completos (na fase 2, em vez disto, meter aqui a controlo de erros com os nseq)
				tabelas.get(ip).incrementCompleted();
				//se for o ultimo ack de data na transferencia ele envia o FYN
				if(tabelas.get(ip).getCompleted()==tabelas.get(ip).getNumberOfPdus())
					agente.sendFYN(pacote.getNseq()+1,ip);
				//else, envia o proximo pacote de data
				else 
					agente.sendPdu(tabelas.get(ip).getPdu(pacote.getNseq()-2-tabelas.get(ip).getNseq()),ip);
			}
			else if(pacote.getFlag() == 6){ //FYN
				System.out.println("Received: FYN (Transfer Complete)");
				agente.sendAck(pacote.getNseq()+1, 7,ip);
			}
			else if(pacote.getFlag() == 7){ //FYN ACK
				System.out.println("Received: FYN ACK");
				agente.sendAck(pacote.getNseq()+1, 8,ip);
			}
			else if(pacote.getFlag() == 8){ //Ack do FYN ACK
				System.out.println("Received: ACK (Connection Ended)");
				agente.sendAck(pacote.getNseq()+1, 9,ip);
			}
			return;
		}
	else System.out.println("Erro no checksum");
	}
}