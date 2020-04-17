import java.util.Random;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

class Pdu implements Serializable,Comparable<Pdu>{
	private int nseq;
	private int ackNseq;
	private byte[] data;
	private int flag;
	private String filename;
	private long crcValue;
	private byte[] signature;
	private PublicKey publickey;

	public Pdu(int nseq, byte[] data,int flag){ //data pdu
		this.nseq = nseq;
		this.data = data;
		this.crcValue = -1;
		this.flag = flag;
		this.signature = null;
		this.publickey = null;
	}

	public Pdu(Pdu p){
		this.nseq = p.getNseq();
		this.data = p.getData();
		this.ackNseq = p.getAckNseq();
		this.flag = p.getFlag();
		this.filename = p.getFileName();
		this.signature = p.getSignature();
		this.publickey = p.getPublicKey();
		this.crcValue = -1;
	}


	public Pdu(int nseq,int ackNseq,int flag){ //,SynAck,AckSynAckdownload ou upload
			this.nseq = nseq;
			this.ackNseq = ackNseq;
			this.flag = flag;
			this.crcValue = -1;
	}

	public Pdu(int nseq,int ackNseq,int flag, String filename){ //syn
			this.nseq = nseq;
			this.ackNseq = ackNseq;
			this.flag = flag;
			this.filename = filename;
			this.crcValue = -1;
		    this.signature = null;
			this.publickey = null;
	}
	public int getFlag(){
		return flag;
	}

	public PublicKey getPublicKey(){
		return this.publickey;
	}

	public void setPublicKey(PublicKey p){
		this.publickey = p;
	}

	@Override
    public int compareTo(Pdu p)
    {
        return this.nseq - p.getNseq();
    }

    public byte[] getSignature(){
    	return this.signature;
    }

	public int getNseq(){
		return nseq;
	}
	public int getAckNseq(){
		return ackNseq;
	}
	public void setSignature(byte[] signature){
		this.signature = signature;
	}

	public void setData(byte[] data){
		this.data = data;
	}
	public byte[] getData(){
		return data;
	}
	public long getCrcValue(){
		return crcValue;
	}
	public void setCrcValue(long crcValue){
		this.crcValue = crcValue;
	}

	public String getFileName(){
		return filename;
	}



}