import java.util.ArrayList;

class Tabela{
	private int nseq;
	private int type;
	private ArrayList<Pdu> pdus;
	private String filename;
	private int completed;

	public Tabela(int nseq, int type, String filename){
		pdus = new ArrayList<Pdu>();
		this.type = type;
		this.nseq = nseq;
		this.filename = filename;
	}

	public Tabela(int nseq, int type, String filename, ArrayList<Pdu> pdus, int completed){
		this.pdus = pdus;
		this.type = type;
		this.nseq = nseq;
		this.filename = filename;
		this.completed = completed;
	}

	public int getType(){
		return this.type;
	}

	public String getFileName(){
		return this.filename;
	}

	public Pdu getPdu(int n){
		return this.pdus.get(n);
	}

	public int getNseq(){
		return this.nseq;
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
}