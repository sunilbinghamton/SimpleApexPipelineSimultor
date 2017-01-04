
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Container {
	
	//This will contain all registers and its contents.
	public static Map<String,Long> registers= new HashMap<String,Long>();

//This will contain all ex forwarded registers and its contents.
public static Map<String,Long> fwdExRegisters= new HashMap<String,Long>();
//This will contain all ex forwarded registers and its contents.
public static Map<String,Long> fwdMemRegisters= new HashMap<String,Long>();
//This will contain all ex forwarded registers and its contents.
public static Map<String,Long> fwdWBRegisters= new HashMap<String,Long>();
	
	//This will contain instructions according to flow.
	public static TreeMap<Long,String> instructions= new TreeMap<Long,String>();
	
	//This will contain fetched instructions.
	public static TreeMap<Long,String> instructionsFetched= new TreeMap<Long,String>();

	// This will contains all instructions in text file. 
	public static List<String> instructionsList = new ArrayList<String>();
	
	//This will contain instructions which are decoded.
	public static TreeMap<Long,Instruction> instructionsDecoded= new TreeMap<Long,Instruction>();
	
	// This will contain instructions in particular stage in pipeline.
	public static Map<SimBackground.myStages,Long> stageInfo= new HashMap<SimBackground.myStages,Long>();
	
	//This will contain all memory locations and its contents.
	public static Map<Long,Long> memory= new HashMap<Long,Long>();
	
	public static long memory_start=0;
	public static long memory_end=9999;
	public static long memory_current;
	
	// Program counter
	public static long pc=0;
		
	// To point memory location at which instruction resides.
	public static long instr_memory_location=19999;
	


	public static boolean stall=false;
	
	public static boolean halt=false;
	
	public static long PSW;
	
	public static boolean bzInstruction=false;

	public static long bzInstructionPc;

	public static boolean takeBranch=false;

	
	// Program counter
	public static long branchPC=0;
	
	public static long totalCompletedInstruction=0;
	
}