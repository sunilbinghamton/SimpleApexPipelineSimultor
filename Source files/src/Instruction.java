import java.util.ArrayList;
import java.util.List;

public class Instruction {
	private String src1;
	private String src2;
	private String dest;
	//private String dest2;
	private String storeDest;
	private String storeDest2;
	private long src1Value;
	private long src2Value;
	private long destValue;
	//private long dest2Value;
	private long storeDestValue;
	private long storeDest2Value;

	private boolean completed=false;
	
	public boolean execompleted=false;
	public boolean memcompleted=false;	
	public boolean mycompleted=false;
	
	private SimBackground.myOpcode opcode;
	private boolean dependency=false;
	private List<Long> dependenyOn= new ArrayList<Long>();
	private long pc;
	private boolean srcDestLiteral=false;
	


	
	public String getStoreDest() {
		return storeDest;
	}
	public void setStoreDest(String storeDest) {
		this.storeDest = storeDest;
	}
	public String getStoreDest2() {
		return storeDest2;
	}
	public void setStoreDest2(String storeDest2) {
		this.storeDest2 = storeDest2;
	}
	public long getStoreDestValue() {
		return storeDestValue;
	}
	public void setStoreDestValue(long storeDestValue) {
		this.storeDestValue = storeDestValue;
	}
	public long getStoreDest2Value() {
		return storeDest2Value;
	}
	public void setStoreDest2Value(long storeDest2Value) {
		this.storeDest2Value = storeDest2Value;
	}

	public boolean isSrcDestLiteral() {
		return srcDestLiteral;
	}
	public void setSrcDestLiteral(boolean srcDestLiteral) {
		this.srcDestLiteral = srcDestLiteral;
	}
	
	public long getPc() {
		return pc;
	}
	public void setPc(long pc) {
		this.pc = pc;
	}
	public boolean isDependency() {
		return dependency;
	}
	public void setDependency(boolean dependency) {
		this.dependency = dependency;
	}
	public List<Long> getDependenyOn() {
		return dependenyOn;
	}
	public void addDependenyOn(long e) {
		this.dependenyOn.add(e);
	}
	public long getSrc1Value() {
		return src1Value;
	}
	public void setSrc1Value(long src1Value) {
		this.src1Value = src1Value;
	}
	public long getSrc2Value() {
		return src2Value;
	}
	public void setSrc2Value(long src2Value) {
		this.src2Value = src2Value;
	}
	public long getDestValue() {
		return destValue;
	}
	public void setDestValue(long destValue) {
		this.destValue = destValue;
	}
	public String getSrc1() {
		return src1;
	}
	public void setSrc1(String src1) {
		this.src1 = src1;
	}
	public String getSrc2() {
		return src2;
	}
	public void setSrc2(String src2) {
		this.src2 = src2;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public SimBackground.myOpcode getOpcode() {
		return opcode;
	}
	public void setOpcode(String opcode) {
		this.opcode = SimBackground.myOpcode.getOpcode(opcode);
	}
	
	
}

