import java.util.Random;
import java.io.Serializable;
import java.util.zip.CRC32;

class Pdu implements Serializable{
	private int nseq;
	private byte[] data;
	private int flag;
	private String filename;
	private int transferType;
	private long crc;

	public Pdu(int nseq, byte[] data,int flag){ //data pdu
		this.nseq = nseq;
		this.data = data;
		this.flag = flag;
		this.filename = "";
		this.crc = -1;
		this.transferType = -1;
	}

	public Pdu(int nseq, byte[] data,int flag, int transferType, String filename){ //synack
		this.nseq = nseq;
		this.data = data;
		this.flag = flag;
		this.filename = filename;
		this.transferType = transferType;
		this.crc = -1;
	}

	public Pdu(int flag, String filename){ //syn download ou upload
		
		if(flag == 0 || flag == 1){ // SYN download ou upload
			Random rand = new Random();
			this.nseq = rand.nextInt(42949);
			this.flag = flag;
			this.data = null;
			this.filename = filename;
			this.crc = -1;
			this.transferType = flag;
		}

	}
	public int getFlag(){
		return flag;
	}
	public int getNseq(){
		return nseq;
	}
	public String getFileName(){
		return filename;
	}
	public int getTransferType(){
		return transferType;
	}

	public long getCrc(){
		return crc;
	}
	public void setCrc(long crc){
		this.crc = crc;
	}
	public Pdu pacoteSemCrc(){
		Pdu pduOriginalSemCrc = new Pdu(this.nseq, this.data, this.flag, this.transferType, this.filename);
		return pduOriginalSemCrc;
	}
}