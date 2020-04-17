import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.*;
import java.util.Arrays;

import java.util.ArrayList;
import java.nio.file.*;
import java.io.*;
import java.net.*;
import java.io.Serializable;
class Tabela{
	private int nseqInicial;
	private int transferType;
	private ArrayList <Pdu> receivedPdus;
	private ArrayList<Pdu> pdus;
	private ArrayList<TimerTask> timers;
	private String filename;
	private int completed;
	private int nextNseq;
	private int i = 0;
	private int port;

	public Tabela(int transferType, String filename,int port,int tabelaFlag){ //cria tabela para enviar 
		this.transferType = transferType;
		Random rand = new Random();
		this.nseqInicial = rand.nextInt(42949);
		this.nextNseq = nseqInicial;
		this.receivedPdus = new ArrayList<>();
		if(tabelaFlag==0)
		this.pdus = new ArrayList<Pdu>();
		if(tabelaFlag==1)
		this.pdus = parteFicheiro(filename,nseqInicial);
		this.timers = new ArrayList<>();
		int k = pdus.size()+1000000;
		for(i=0;i<=k;i++){
			timers.add(null);
		}
		this.i=0;
		this.filename = filename;
		this.port = port;
		this.completed = 0;
	}

	public void updateNextNseq(){
		this.nextNseq ++;
	}

	public void addPdu(Pdu pacote){
		pdus.add(pacote);
		Collections.sort(pdus);
	}

	public void addTimeout(Pdu p,TimerTask t){
		
		timers.set(pdus.indexOf(p),t);
		i++;
	}

	public void cancelTimout(int nseq){
		timers.get(nseq-nseqInicial).cancel();
	}

	public int countTimers(){
		int i;
		int k = 0;
		for(i = 0; i<timers.size();i++){
			if (timers.get(i)!=null)
			k++;
		}
		return k;
	}

	public int getTransferType(){
		return this.transferType;
	}
	public ArrayList<TimerTask> getTimers(){
		return this.timers;
	}

	public void addReceivedPdu(Pdu p){
		receivedPdus.add(p);
		Collections.sort(receivedPdus);
	}

	public ArrayList<Pdu> getReceivedPdus(){
		return this.receivedPdus;
	}

	public String getFileName(){
		return this.filename;
	}

	public int getNextNSeq(){
		return nextNseq;
	}

	public ArrayList<Pdu> getPdus(){
		return pdus;
	}

	public Pdu getPdu(int seq){
		return this.pdus.get(seq -nseqInicial);
	}

	public int getNseqInicial(){
		return this.nseqInicial;
	}

	public int getPort(){
		return this.port;
	}

	public void setPort(int port){
		this.port = port;
	}
	public int getCompleted(){
		return this.completed;
	}

	public void incrementCompleted(){
		this.completed++;
	}

	public int getNumberOfPdus(){
		return this.pdus.size();
	}

    public void construirFicheiro(){
        byte[] bytes;
        File file = new File("ficheiroRecebido.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
               for(int i=0; i<receivedPdus.size(); i++){
               	if(receivedPdus.get(i).getFlag()==4){
                bytes = receivedPdus.get(i).getData();
                fos.write(bytes);
            	}
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

		public ArrayList<Pdu> parteFicheiro(String filename, int n) {
		byte[] fileContent = fileToBytes(filename);
		//divir o fileContent em tamanhos de 1500 (se calhar podemos mudar isto: em vez de fazer isto podemos partir logo direto com o fileToBytes ao ler só uma vez)
		byte[][] split = splitBytes(fileContent, 1500);
		//fazer os pdus com as datas no "split"
		int nseq = 0;
		if(transferType== 0)
		nseq = n+1;
		if(transferType==1)
		nseq = n+2;
		ArrayList<Pdu> pdus = new ArrayList<Pdu>();
		for(int z = 0; z<(split.length); z++){
			Pdu newpdu = new Pdu(nseq,split[z], 4);
			System.out.println("PDU creadted: "+newpdu.getNseq());
			pdus.add(newpdu);
			
			nseq++;
		}
		return pdus;
	}

    public static byte[] fileToBytes(String filename) {
         File file = new File(filename);
         byte[] b = new byte[(int) file.length()];
         try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            /*for (int i = 0; i < b.length; i++) { // só para confirmar que leu a file para o array b
                System.out.print((char)b[i]);
            }*/
            return b;
          } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
          }
          catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
          }
          return b;
       }

       public byte[][] splitBytes(byte[] data, int chunkSize){
  		int length = data.length;
  		byte[][] dest = new byte[(length + chunkSize - 1)/chunkSize][];
  		int destIndex = 0;
  		int stopIndex = 0;

  		for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize){
    		stopIndex += chunkSize;
    		dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
  		}

  		if (stopIndex < length)
    		dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);

  		return dest;
		}
}