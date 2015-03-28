package OdlAddon;

public class Link {
	private int id;
	private String src;
	private String dest;
	public Link(int id, String src, String dest) {
		super();
		this.id = id;
		this.src = src;
		this.dest = dest;
	}
	public int getId() {
		return id;
	}
	public String getSrc() {
		return src;
	}
	public String getDest() {
		return dest;
	}



}
