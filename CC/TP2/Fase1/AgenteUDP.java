import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;


class AgenteUDP
{
   private TransfereCC transfereCC;

   public AgenteUDP(){
      this.transfereCC = new TransfereCC(this);
   }

   public static void main(String args[]) throws Exception{
      AgenteUDP agente = new AgenteUDP();
      if(args[0].equals("server")){
         agente.listener(); //server
      }
      else if (args[0].equals("download")){ // download filename ipDestino
         agente.init(0, args[1],args[2]);
      }
      else if (args[0].equals("upload")){ // upload filename ipDestino
        agente.init(1, args[1],args[2]);
      }
   }

    public void listener() throws Exception {
      DatagramSocket serverSocket = new DatagramSocket(7777);
      byte[] receiveData = new byte[2000];
      System.out.println(">MODO LOCALHOST: 1 PC");  //alterar linha no transfereCC onde recebe synack
      //System.out.println(">MODO REDE LOCAL: 2 PCs");  //alterar linha no transfereCC onde recebe synack
      while(true){
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        Pdu pacote = (Pdu)toObject(receivePacket.getData());
        String ip = receivePacket.getAddress().toString();
        transfereCC.evaluate(pacote,ip);
      }
    }

    public void init(int flag, String filename, String ipDestino) throws Exception{ 
      byte[] data = new byte[1500];
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName(ipDestino);
      Pdu syn = new Pdu(flag,filename);// flag -> 0: syn download; 1: syn upload

      byte[] temp = new byte[1500];
			temp = toByteArray(syn);
			CRC32 crc32 = new CRC32();
			crc32.update(temp);
			//System.out.println("crc value antes de enviar: "+crc32.getValue());
			syn.setCrc(crc32.getValue());

      data = toByteArray(syn);
      DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7777);
      clientSocket.send(packet);
    }

    public void sendAck(int nseq, int type, String ip)throws Exception{
      byte[] data = new byte[1500];
      DatagramSocket clientSocket = new DatagramSocket();
      String ipSend = ip.substring(1);
      InetAddress IPAddress = InetAddress.getByName(ipSend);
      Pdu ack = new Pdu(nseq,null,type);

      byte[] temp = new byte[1500];
      temp = toByteArray(ack);
      CRC32 crc32 = new CRC32();
      crc32.update(temp);
      //System.out.println("crc value antes de enviar: "+crc32.getValue());
      ack.setCrc(crc32.getValue());

      data = toByteArray(ack);
      DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7777);
      clientSocket.send(packet);
    }

    public void sendSYNAck(int nseq, int type, int transferType, String ip, String filename)throws Exception{
      byte[] data = new byte[1500];
      DatagramSocket clientSocket = new DatagramSocket();
      String ipSend = ip.substring(1);
      InetAddress IPAddress = InetAddress.getByName(ipSend);
      Pdu ack = new Pdu(nseq,null,type,transferType,filename);

      byte[] temp = new byte[1500];
      temp = toByteArray(ack);
      CRC32 crc32 = new CRC32();
      crc32.update(temp);
      //System.out.println("crc value antes de enviar: "+crc32.getValue());
      ack.setCrc(crc32.getValue());

      data = toByteArray(ack);
      DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7777);
      clientSocket.send(packet);
    }

    public void sendFYN(int nseq, String ip)throws Exception{
      byte[] data = new byte[1500];
      DatagramSocket clientSocket = new DatagramSocket();
      String ipSend = ip.substring(1);
      InetAddress IPAddress = InetAddress.getByName(ipSend);
      Pdu ack = new Pdu(nseq,null,6);

      byte[] temp = new byte[1500];
      temp = toByteArray(ack);
      CRC32 crc32 = new CRC32();
      crc32.update(temp);
      //System.out.println("crc value antes de enviar: "+crc32.getValue());
      ack.setCrc(crc32.getValue());

      data = toByteArray(ack);
      DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7777);
      clientSocket.send(packet);
    }

    public void sendPdu(Pdu pdu, String ip) throws Exception{
      byte[] data = new byte[2000];
      DatagramSocket clientSocket = new DatagramSocket();
      String ipSend = ip.substring(1);
      InetAddress IPAddress = InetAddress.getByName(ipSend);

      byte[] temp = new byte[1500];
      temp = toByteArray(pdu);
      CRC32 crc32 = new CRC32();
      crc32.update(temp);
      //System.out.println("crc value antes de enviar: "+crc32.getValue());
      pdu.setCrc(crc32.getValue());

      data = toByteArray(pdu);
      DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7777);
      clientSocket.send(packet);
    }

    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException { // passar byte[] para object ao receber
      Object obj = null;
      ByteArrayInputStream bis = null;
      ObjectInputStream ois = null;
      try {
        bis = new ByteArrayInputStream(bytes);
        ois = new ObjectInputStream(bis);
        obj = ois.readObject();
      } 
      /*catch(EOFException e){
        	
      }*/
      finally {
        if (bis != null) {
          bis.close();
        }
        if (ois != null) {
          ois.close();
        }
      }
      return obj;
    }

    public static byte[] toByteArray(Object obj) throws IOException { // passar object byte[] para enviar
      byte[] bytes = null;
      ByteArrayOutputStream bos = null;
      ObjectOutputStream oos = null;
      try {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        bytes = bos.toByteArray();
      } finally {
        if (oos != null) {
          oos.close();
        }
        if (bos != null) {
          bos.close();
        }
      }
      return bytes;
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

	  public ArrayList<Pdu> parteFicheiro(String filename, int n) {
		  byte[] fileContent = fileToBytes(filename);
		  //divir o fileContent em tamanhos de 1500 (se calhar podemos mudar isto: em vez de fazer isto podemos partir logo direto com o fileToBytes ao ler só uma vez)
		  byte[][] split = splitBytes(fileContent, 1500);
		  //fazer os pdus com as datas no "split"
		  int nseq = n+2;
		  ArrayList<Pdu> pdus = new ArrayList<Pdu>();
		  for(int z = 0; z<(split.length); z++){
			  pdus.add(new Pdu(nseq,split[z], 4));
			  //pdus.add(new Pdu(nseq,null,4));
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
}