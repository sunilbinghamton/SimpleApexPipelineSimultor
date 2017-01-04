import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class SimBackground {

//enum to define all stages
public enum myStages {
		Fetch,
		Decode,
		Execute,
		Execute2,
		Memory,
		WriteBack;
	}

//enum to define opcodes of instructions
public enum myOpcode {
	LOAD,
	STORE,
	JUMP,
	BZ,
	BNZ,
	HALT,
	ADD,
	SUB,
	MUL,
	AND,
	OR,
	EX_OR,
	MOVC,
	BAL;
	
	public static myOpcode getOpcode(String opcode) {
		if("ADD".equalsIgnoreCase(opcode))
			return ADD;
		else if("SUB".equalsIgnoreCase(opcode))
			return SUB;
		else if("MUL".equalsIgnoreCase(opcode))
			return MUL;
		else if("AND".equalsIgnoreCase(opcode))
			return AND;
		else if("HALT".equalsIgnoreCase(opcode))
			return HALT;
		else if("JUMP".equalsIgnoreCase(opcode))
			return JUMP;
		else if("BZ".equalsIgnoreCase(opcode))
			return BZ;
		else if("OR".equalsIgnoreCase(opcode))
			return OR;
		else if("EX-OR".equalsIgnoreCase(opcode))
			return EX_OR;
		else if("MOVC".equalsIgnoreCase(opcode))
			return MOVC;
		else if("LOAD".equalsIgnoreCase(opcode))
			return LOAD;
		else if("STORE".equalsIgnoreCase(opcode))
			return STORE;
		else if("BNZ".equalsIgnoreCase(opcode))
			return BNZ;
		else if("BAL".equalsIgnoreCase(opcode))
			return BAL;
		throw new RuntimeException("Not valid");
	}
};
//function to process file
	public void fileProcess(String fileName) throws IOException{		
		BufferedReader _breader =null;
			try{
				_breader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/"+fileName));
				String temp = _breader.readLine();
				while(temp!=null){
					Container.instructions.put(++Container.instr_memory_location, temp);
					temp = _breader.readLine();
				}
				_breader.close();
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}	
		}
//constructor to initialise	
	SimBackground(String fileName) throws IOException{
		for(long i=0;i<=3999;i++){
			Container.memory.put(i, 0l);
		}
		fileProcess(fileName);
	}
//initilisation of registers
	void initialize(){
		for(int i=0;i<=15;i++){
			Container.registers.put("R"+i, 0l);
			Container.fwdExRegisters.put("R"+i, 0l);
			Container.fwdMemRegisters.put("R"+i, 0l);
			Container.fwdWBRegisters.put("R"+i, 0l);
		}
		Container.registers.put("X", 0l);
		Container.pc=20000;
	}

//function to simulate
	void simulate(long itr) throws Exception{
		try{
		while(itr !=0 ){
			long pcD=0;
			long pcE=0;
			long pcE2=0;
			long pcM=0;
			long pcW=0;

			//Delete last stage info as instruction has been passed from that stage. 
			removeStageInfo(myStages.WriteBack);	
			//Check whether current instruction in decode stage can be move in next stage in this cycle.
			if(getStageInfo(myStages.Fetch)!=null){
				if(getStageInfo(myStages.Decode)!=null) {
					if(isDependencyResolved(getDecodedInstruction(getStageInfo(myStages.Decode)))){
						pcD=getStageInfo(myStages.Fetch);
					}
				}else
					pcD=getStageInfo(myStages.Fetch);
			}if(getStageInfo(myStages.Decode)!=null ){
				Instruction ins =getDecodedInstruction(getStageInfo(myStages.Decode));
				if(isDependencyResolved(ins))
					pcE=getStageInfo(myStages.Decode);
			}
			if(getStageInfo(myStages.Execute)!=null ){
				pcE2=getStageInfo(myStages.Execute);
			}
			if(getStageInfo(myStages.Execute2)!=null)
				pcM=getStageInfo(myStages.Execute2);
			if(getStageInfo(myStages.Memory)!=null)
				pcW=getStageInfo(myStages.Memory);

			if(getFetchInstructionCount() < getInstructionsCount()+1 && isNextInstructionPresent() && !isPipelineStalled()){
					fetch();
			}

			if(pcD != 0)
				decode(pcD);
			if(pcE != 0)
				execute(pcE);
			if(pcE2 != 0)
				execute2(pcE2);
			if(pcM != 0)
				memory(pcM);
			if(pcW != 0){
				writeBack(pcW);
				if(Container.halt){
					removeStageInfo(myStages.WriteBack);	
					display();
					break;
				}
			}
			itr--;
		}
		}catch(Exception e){
		}
	}

	//display function to show output
	public void display() {		
		displayFetchData();
		displayDecodeData();
		displayExecuteData();
		displayExecuteData2();
		displayMemoryData();
		displayWriteBackData();
		
		System.out.println("Values of first 100 registers	\n");
		for(String temp:Container.registers.keySet()){
			if(!temp.equalsIgnoreCase(""))
				System.out.println(temp + "	=> "+Container.registers.get(temp));
		}
		System.out.println("Values from Memory	\n");
		for(long temp=0;temp<4000;temp++){
			if(Container.memory.get(temp)>0)
			System.out.println(temp + "	=> "+Container.memory.get(temp));
		}

	}
	
	//fetch stage fetch and add it to the container
	void fetch(){
		addToFetchInstructionMap(Container.pc,getInstruction());
		insertStageInfo(myStages.Fetch,Container.pc);
		if(getFetchInstructionCount() < getInstructionsCount()+1){
			++Container.pc;
		}
	}
	
//decode instruction find and mark dependency if any
	void decode(long currentCounter){
		String instruction =getFetchedInstruction(currentCounter);
		Instruction ins =decoding(instruction);
		ins.setPc(currentCounter);
		ins.setCompleted(false);

		addToDecodedInstructionsMap(currentCounter,ins);
		insertStageInfo(myStages.Decode,currentCounter);
		if(getStageInfo(myStages.Decode).longValue() == getStageInfo(myStages.Fetch).longValue()) {
			removeStageInfo(myStages.Fetch);	
		}
		if(ins.getOpcode() == myOpcode.BZ || ins.getOpcode() == myOpcode.BNZ){
			setBzInstructionPc(ins);
		}	

	}

	//execute stage to call appropriate function unit
	void execute(long currentCounter){
		Instruction ins =getDecodedInstruction(currentCounter);
		if(ins.getOpcode()==myOpcode.MOVC)
			movcFU(ins);
		else if(ins.getOpcode()==myOpcode.HALT){}
		
		else if(ins.getOpcode()==myOpcode.JUMP){
			jumpFU(ins);
		}else if(ins.getOpcode()==myOpcode.BZ){
			bzFU(ins);
		}
		else if(ins.getOpcode()==myOpcode.LOAD)
			loadFU(ins);
		else if(ins.getOpcode()==myOpcode.STORE)
			storeFU(ins);
		else if(ins.getOpcode()==myOpcode.ADD)
			addFU(ins);
		else if(ins.getOpcode()==myOpcode.SUB)
			subFU(ins);
		else if(ins.getOpcode()==myOpcode.MUL)
			mulFU(ins);
		else if(ins.getOpcode()==myOpcode.AND)
			andFU(ins);
		else if(ins.getOpcode()==myOpcode.OR)
			orFU(ins);
		else if(ins.getOpcode()==myOpcode.EX_OR)
			ex_orFU(ins);
		else if(ins.getOpcode()==myOpcode.BNZ){
			bnzFU(ins);
		}else if(ins.getOpcode()==myOpcode.BAL){
			balFU(ins);
		}
		
		insertStageInfo(myStages.Execute,currentCounter);
		if(getStageInfo(myStages.Execute).longValue() == getStageInfo(myStages.Decode).longValue()){
			removeStageInfo(myStages.Decode);	
		}

		if((ins.getOpcode()==myOpcode.JUMP || ins.getOpcode()==myOpcode.BZ || ins.getOpcode()==myOpcode.BNZ || ins.getOpcode() == myOpcode.BAL)){
		if(Container.takeBranch){	
			removeStageInfo(myStages.Decode);
			removeStageInfo(myStages.Fetch);
		}
	  }
	}
//atomic execution unit
	void execute2(long currentCounter){
		Instruction ins =getDecodedInstruction(currentCounter);
		insertStageInfo(myStages.Execute2,currentCounter);
		if(getStageInfo(myStages.Execute2).longValue() == getStageInfo(myStages.Execute).longValue()){
			removeStageInfo(myStages.Execute);
		}
		setPSWValue(ins.getDestValue());
		ins.mycompleted=true;
		ins.execompleted=true;
		addfwdRegisterValue(ins.getDest(), ins.getDestValue());
	}
	
//perform all memory operations in this function, like store load
	void memory(long currentCounter){
//		long fwrdValue =0;
		Instruction ins =getDecodedInstruction(currentCounter);
		if(ins.getOpcode()==myOpcode.LOAD){
			ins.setDestValue(getMemoryValue(ins.getDestValue()));
			addfwdMemRegisterValue(ins.getDest(), ins.getDestValue());
		}
		//Always use latest value of registers before adding to memory. This supports MEM to MEM LOAD STORE forwarding scenario 3
		else if(ins.getOpcode()==myOpcode.STORE){
//if(Container.fwdMemRegisters.containsKey(ins.getSrc1() )){
//	fwrdValue = getfwdMemRegisterValue(ins.getSrc1());
//	Container.fwdMemRegisters.remove(ins.getSrc1());
//	}
//else{
//	//setDependentRegisterValues(ins);
//	fwrdValue = ins.getSrc1Value();
//	}
			addMemoryValue(ins.getDestValue(), ins.getSrc1Value());
		}
		
		insertStageInfo(myStages.Memory,currentCounter);
		if(getStageInfo(myStages.Memory).longValue() == getStageInfo(myStages.Execute2).longValue()){
			removeStageInfo(myStages.Execute2);	
		}
		ins.memcompleted=true;
	}

	// Write result to register set.
	void writeBack(long currentCounter){

		Instruction ins =getDecodedInstruction(currentCounter);
		if(ins.getOpcode()== myOpcode.BAL){
			addRegisterValue(ins.getDest(), currentCounter+1);
		}
		else if(ins.getOpcode()!=myOpcode.STORE )
			if(ins.getOpcode()!=myOpcode.HALT )
			addRegisterValue(ins.getDest(), ins.getDestValue());

		insertStageInfo(myStages.WriteBack,currentCounter);
		ins.setCompleted(true);
		if((getStageInfo(myStages.WriteBack).longValue() == getStageInfo(myStages.Memory).longValue())){
			removeStageInfo(myStages.Memory);	
		}
		addTotalCompletedInstructions();
		if(ins.getOpcode() == myOpcode.HALT)
			Container.halt=true;

		if( (currentCounter == getBzInstructionPc()-1)){
			if(isBzInstructionHasArrived())
			setPSWValue(ins.getDestValue());
		}		
	}

	//actual decoding of instruction and saving operands and dest
	public  Instruction  decoding(String temp){
		temp= temp.replace(",", "");
		temp= temp.replace("#", "");
		//System.out.println(temp);
		String myArray[]=temp.split(" ");
		Instruction ins = new Instruction();

		ins.setOpcode(myArray[0].trim());
		if(ins.getOpcode() == myOpcode.HALT){
			ins.setDest("");
			return ins;
		}
		
		if(ins.getOpcode() == myOpcode.STORE){
			ins.setSrc1(myArray[1].trim());
			ins.setStoreDest(myArray[2].trim());
			ins.setStoreDest2(myArray[3].trim());
			ins.setDest("");
		}else if(ins.getOpcode() == myOpcode.BZ || ins.getOpcode() == myOpcode.BNZ){
			ins.setDest("");
			ins.setSrc1Value(Long.parseLong(myArray[1].trim())/4);
			setBzInstructionHasArrived(true);
		}else if (ins.getOpcode() == myOpcode.BAL){
			ins.setDest("X");
			ins.setSrc1(myArray[1]);
			ins.setSrc2Value(Long.parseLong(myArray[2]));

		}else if (ins.getOpcode() == myOpcode.JUMP){
			ins.setSrc1(myArray[1].trim());
			ins.setSrc2(myArray[2].trim());
		}else{
			ins.setSrc1(myArray[2].trim());
			ins.setDest(myArray[1].trim());
		}
		if(ins.getOpcode()==myOpcode.ADD || ins.getOpcode()==myOpcode.SUB || ins.getOpcode() == myOpcode.MUL || ins.getOpcode()==myOpcode.AND || ins.getOpcode()==myOpcode.OR || ins.getOpcode()==myOpcode.EX_OR)
			ins.setSrc2(myArray[3]);
		if(ins.getOpcode() == myOpcode.MOVC ){
			ins.setSrc1Value(Long.valueOf(ins.getSrc1()));
			ins.setSrc2Value(0);
		}else if(ins.getOpcode() == myOpcode.JUMP ){
			ins.setSrc1Value(getRegisterValue(ins.getSrc1()));
			ins.setSrc2Value(Long.valueOf(ins.getSrc2()));
			ins.setDest("");
		}
		else if(ins.getOpcode() != myOpcode.BZ && ins.getOpcode() != myOpcode.BNZ){	
			ins.setSrc1Value(getRegisterValue(ins.getSrc1()));
			
			if(Container.registers.containsKey(ins.getSrc2())){
				ins.setSrc2Value(getRegisterValue(ins.getSrc2()));
			}	
			if( ins.getOpcode() != myOpcode.BAL)
				ins.setSrc2(myArray[3].trim());
			
			if(ins.getOpcode() == myOpcode.LOAD){
				if(!Container.registers.containsKey(ins.getSrc2())){
					ins.setSrc2Value(Long.valueOf(ins.getSrc2()));
					ins.setSrcDestLiteral(true);
				}else
					ins.setSrc2Value(getRegisterValue(ins.getSrc2()));
			}

			if(ins.getOpcode() == myOpcode.STORE){
				if(!Container.registers.containsKey(ins.getStoreDest2())){
					ins.setStoreDest2Value(Long.valueOf(ins.getStoreDest2()));
					ins.setSrcDestLiteral(true);
				}else
					ins.setStoreDest2Value(getRegisterValue(ins.getStoreDest2()));
				
				ins.setStoreDestValue(getRegisterValue(ins.getStoreDest()));
			}
		}

		return checkAndMarkDependency(ins);


	}
	//  checks for flow dependency if any.
	Instruction checkAndMarkDependency(Instruction ins){
		//If instruction is bz or bnz,check whether previous instruction has completed its writeback.
				if(ins.getOpcode() == myOpcode.BZ || ins.getOpcode() == myOpcode.BNZ){
							ins.setDependency(true);
							ins.addDependenyOn(getBzInstructionPc()-1);
							setPipelineStalled(true);
							return ins;					
					}
						
		for(Instruction insDecoded : Container.instructionsDecoded.values() ){
			// Here we are checking whether source registers are referred by 
			//another previous instruction as destination register.
			//Except store instruction
			if(!(ins.getOpcode() == myOpcode.STORE)){
				if(ins.getSrc1()!=null && !"".equalsIgnoreCase(ins.getSrc1()) && ins.getSrc2()!=null && !"".equalsIgnoreCase(ins.getSrc2())){
					if(insDecoded.getOpcode() != myOpcode.HALT && 
						(insDecoded.getDest().equalsIgnoreCase(ins.getSrc1()) 
						|| insDecoded.getDest().equalsIgnoreCase(ins.getSrc2()))){
						if(!insDecoded.isCompleted()){
							ins.setDependency(true);
							ins.addDependenyOn(insDecoded.getPc());
							setPipelineStalled(true);
						}	
					}
				}
			}
			// For store instruction
			// Here we are checking whether source registers and destination reisters are referred by 
			//another previous instruction as destination register.

			else if(ins.getOpcode() == myOpcode.STORE){
				if((ins.getDest()!=null && !"".equalsIgnoreCase(ins.getStoreDest()))
						|| (ins.getStoreDest2()!=null && !"".equalsIgnoreCase(ins.getStoreDest2()))
						|| (ins.getSrc1()!=null && !"".equalsIgnoreCase(ins.getSrc1()))){
					if(insDecoded.getOpcode() != myOpcode.HALT && (insDecoded.getDest().equalsIgnoreCase(ins.getStoreDest()) 
							|| insDecoded.getDest().equalsIgnoreCase(ins.getStoreDest2())
							|| insDecoded.getDest().equalsIgnoreCase(ins.getSrc1()))){
//////////////////////// check if instruction is a LOAD and its pc value is 1 or 2 less (consecutive) then don't block STORE (MEM to MEM forwarding)
//						if ( insDecoded.getOpcode() == myOpcode.LOAD && insDecoded.getPc()+4  == getStageInfo(Stages.Fetch)  ||
//								insDecoded.getOpcode() == myOpcode.LOAD && insDecoded.getPc()+8  == getStageInfo(Stages.Fetch) ){
//								ins.setDependency(false);
//								setPipelineStalled(false);
//								}
//								else if(!insDecoded.isCompleted() && !insDecoded.memcompleted && !insDecoded.execompleted){
//								ins.setDependency(true);
//								ins.addDependenyOn(insDecoded.getPc());
//								setPipelineStalled(true);
//								}

						if(!insDecoded.isCompleted()){
							ins.setDependency(true);
							ins.addDependenyOn(insDecoded.getPc());
							setPipelineStalled(true);
						}
					}							
				}
			} 
		}		return ins;
	}


	//Checking whether all dependencies are resolved 
	boolean isDependencyResolved(Instruction ins){
		
		//If instruction is bz or bnz,check whether previous instruction has completed its writeback.
		if(ins.getOpcode() == myOpcode.BZ || ins.getOpcode() == myOpcode.BNZ){
			if(getDecodedInstruction(getBzInstructionPc()-1)!=null){
				if(getDecodedInstruction(getBzInstructionPc()-1).mycompleted){
					removePipelineStall();
					return true;
			  }
			}
			return false;
		}
		if(ins.isDependency()){
			List<Long> list=ins.getDependenyOn();
			boolean firstDependencyResolved=false;
			boolean secondDependencyResolved=false;
			//checking for dependency if any	
			boolean firstDependencyChecked=false;
			for (long pc : list) {
				if(!firstDependencyChecked){
					//firstDependencyResolved = getDecodedInstruction(pc).isCompleted();
					firstDependencyResolved = getDecodedInstruction(pc).execompleted;
					firstDependencyChecked=true;
				}else{
					//secondDependencyResolved = getDecodedInstruction(pc).isCompleted();
					secondDependencyResolved = getDecodedInstruction(pc).execompleted;
				}
			}
			//if no dependency, removing dependency and calling procedure to set values
			if(list.size()==1){
				if(firstDependencyResolved){
					removePipelineStall();			
					//setDependentRegisterValues(ins);
					setForwaredeValues(ins);
					return true;
				}
			}
			else if(firstDependencyResolved && secondDependencyResolved ){
				removePipelineStall();
				//setDependentRegisterValues(ins);
				setForwaredeValues(ins);				
				return true;
			}
			//dependency but not resolved, return false
			return false;
		}
		//if no dependency return true
		return true;
	}

	//Now as we have satisfied dependencies retrieve values of registers.
	//If src or dest is literal keep it as it is.
	void setDependentRegisterValues(Instruction ins){
		ins.setSrc1Value(getRegisterValue(ins.getSrc1()));
		if(!ins.isSrcDestLiteral()){
			if(ins.getOpcode() == myOpcode.STORE){
				ins.setStoreDestValue(getRegisterValue(ins.getStoreDest()));
				ins.setStoreDest2Value(getRegisterValue(ins.getStoreDest2()));
			}else if(Container.registers.containsKey(ins.getSrc2()))
				ins.setSrc2Value(getRegisterValue(ins.getSrc2()));
		}else if(ins.getOpcode() == myOpcode.STORE){
			ins.setStoreDestValue(getRegisterValue(ins.getStoreDest()));
		}
	}

	//add data from forwarded registers to the current registers
	void setForwaredeValues(Instruction ins){
		ins.setSrc1Value(getfwdRegisterValue(ins.getSrc1()));
		if(!ins.isSrcDestLiteral()){
			if(Container.fwdExRegisters.containsKey(ins.getSrc2()))
				ins.setSrc2Value(getfwdRegisterValue(ins.getSrc2()));
		}
	}
	
	
	public void addFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() + i.getSrc2Value();
		i.setDestValue(result);	
	}
	public void subFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() - i.getSrc2Value();
		i.setDestValue(result);	
	}
	public void mulFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() * i.getSrc2Value();
		i.setDestValue(result);	
	}
	public void andFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() & i.getSrc2Value();
		i.setDestValue(result);	
	}		
	public void orFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() | i.getSrc2Value();
		i.setDestValue(result);	
	}
	public void ex_orFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getSrc1Value() ^ i.getSrc2Value();
		i.setDestValue(result);	
	}
	public void movcFU(Instruction i) {
		// TODO Auto-generated method stub
		addFU(i);

	}
	public void movFU(Instruction i) {
		// TODO Auto-generated method stub
		addFU(i);

	}
	public void loadFU(Instruction i) {
		// TODO Auto-generated method stub
		addFU(i);
	}
	public void storeFU(Instruction i) {
		// TODO Auto-generated method stub
		long result = i.getStoreDestValue() + i.getStoreDest2Value();
		i.setDestValue(result);	
	}
	public void jumpFU(Instruction i) {
		// TODO Auto-generated method stub
		Container.takeBranch=true;
		addFU(i);
		Container.pc=i.getDestValue();
		
		if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Decode)))
			Container.instructionsDecoded.remove(getStageInfo(myStages.Decode));
		if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Fetch)))
			Container.instructionsFetched.remove(getStageInfo(myStages.Fetch));


	}
	void bzFU(Instruction i){
		if(getPSWValue() == 0){
			Container.takeBranch=true;
			Container.pc=i.getPc()+i.getSrc1Value();
			if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Decode)))
			Container.instructionsDecoded.remove(getStageInfo(myStages.Decode));
			if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Fetch)))
			Container.instructionsFetched.remove(getStageInfo(myStages.Fetch));
		}
		else{
			Container.takeBranch=false;
		}
	}
	void bnzFU(Instruction i){
		if(getPSWValue() != 0){
			//System.out.println("my psw is  for pc "+i.getPc()+"   "+getPSWValue() );
			Container.takeBranch=true;
			Container.pc=i.getPc()+i.getSrc1Value();
			//System.out.println("new pc is "+Container.pc);
			if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Decode)))
			Container.instructionsDecoded.remove(getStageInfo(myStages.Decode));
			if(getStageInfo(myStages.Fetch)!=null && Container.instructionsFetched.containsKey(getStageInfo(myStages.Fetch)))
			Container.instructionsFetched.remove(getStageInfo(myStages.Fetch));
			//System.out.println("size of fetch is "+ Container.instructionsFetched.size());
		}
		else{
			Container.takeBranch=false;
		}
	}
	public void balFU(Instruction i) {
		// TODO Auto-generated method stub
		jumpFU(i);
	}
	


	//Show contents in Fetch Stage.
	void displayFetchData(){
		if(getStageInfo(myStages.Fetch)!=null){
			if(getStageInfo(myStages.Fetch)!=0){
			long pc= getStageInfo(myStages.Fetch);
			System.out.println("\n\nData of Fetch stage is =>");
			System.out.println("The instruction read from memory: "+getInstructionByPC(pc));
			System.out.println("The PC value: "+pc);
			}
		}
		else
			System.out.println("\n\nNothing to show\n\n");
	}
	
	//Show contents in Decode Stage.
	void displayDecodeData(){
		if(getStageInfo(myStages.Decode)!=null && getStageInfo(myStages.Decode)!=0){
			long pc= getStageInfo(myStages.Decode);
			Instruction ins= getDecodedInstruction(pc);
			System.out.println("\n\nData of Decode stage is =>");
			System.out.println("OPCODE		=	"+ins.getOpcode());
			System.out.println("Source		=	"+ins.getSrc1()+" with value	=	"+ins.getSrc1Value());
			
			if(ins.getOpcode() == myOpcode.LOAD && ins.isSrcDestLiteral()){
				System.out.println("Literal is 		=	"+ins.getSrc2()+" with value	=	"+ins.getSrc2Value());
			}else if(ins.getOpcode() == myOpcode.STORE){
				System.out.println("Source 		=	"+ins.getStoreDest()+" with	value	=	 "+ins.getStoreDestValue());
				if(ins.isSrcDestLiteral())
					System.out.println("Literal		=	 "+ins.getStoreDest2()+" with value		=	 "+ins.getStoreDest2Value());
				else
					System.out.println("Source2 	=	"+ins.getStoreDest2()+" with value		=	"+ins.getStoreDest2Value());
			}
			 if(ins.getOpcode() != myOpcode.STORE)
				 System.out.println("Destination	=	"+ins.getDest());
	
			System.out.println("PC		=	"+pc);
		}
		else
			System.out.println("\n\nNothing to show\n\n");
	}
	
	//Show contents in Execute Stage.
	void displayExecuteData(){
		if(getStageInfo(myStages.Execute)!=null && getStageInfo(myStages.Execute)!=0){
			long pc= getStageInfo(myStages.Execute);
			Instruction ins= getDecodedInstruction(pc);
			System.out.println("\n\nData of Execution stage is =>");
			System.out.println("OPCODE	=	"+ins.getOpcode());
			
			if(ins.getOpcode() != myOpcode.STORE)
				 System.out.println("Destination	=	"+ins.getDest());
			else
				System.out.println("Source	=	"+ins.getSrc1()+" with value	=	"+ins.getSrc1Value());
			System.out.println("PC	=	"+pc);
		}
		else
			System.out.println("\n\nNothing to show\n\n");
	}
//show the content of ex2
	void displayExecuteData2(){
		if(getStageInfo(myStages.Execute2)!=null && getStageInfo(myStages.Execute2)!=0){
			long pc= getStageInfo(myStages.Execute2);
			Instruction ins= getDecodedInstruction(pc);
			System.out.println("\n\nData of Execution2 stage is =>");
			System.out.println("OPCODE		=	"+ins.getOpcode());
	
			if(ins.getOpcode() != myOpcode.STORE)
				 System.out.println("Destination		=	"+ins.getDest());
			else
				System.out.println("Source		=	"+ins.getSrc1()+" with value	=	"+ins.getSrc1Value());
			
			System.out.println("PC		=	"+pc);
			
			System.out.println("Result of Execution 2	=	"+ins.getDestValue());
		}
		else
			System.out.println("\n\nNothing to show\n\n");
	}
		
	//Show contents in Execute Stage.
	void displayMemoryData(){
		if(getStageInfo(myStages.Memory)!=null && getStageInfo(myStages.Memory)!=0){
			long pc= getStageInfo(myStages.Memory);
			Instruction ins= getDecodedInstruction(pc);
			System.out.println("\n\nData of Memory stage is =>");
			System.out.println("OPCODE	=	"+ins.getOpcode());
			if(ins.getOpcode() != myOpcode.STORE)
				 System.out.println("Destination		=	"+ins.getDest());
			else
				System.out.println("Source	=	" + ins.getSrc1() + "with value 	"+ins.getSrc1Value());	
			System.out.println("PC		=	"+pc);
			
			//Add new variable to hold memory calcualted in ex stage so we can show it here.
			if(ins.getOpcode() == myOpcode.LOAD )
				System.out.println("Data from Memory		=	"+ins.getDestValue());
			else if(ins.getOpcode() != myOpcode.STORE )
				System.out.println("Result of operation		=	"+ins.getDestValue());
		}
		else
			System.out.println("\n\nNothing to show\n\n");
	}
	
	
	//Show contents in Execute Stage.
	void displayWriteBackData(){
		if(getStageInfo(myStages.WriteBack) !=null){
			if(getStageInfo(myStages.WriteBack)!=0){
			long currentPC= getStageInfo(myStages.WriteBack);
			Instruction temp= getDecodedInstruction(currentPC);
			System.out.println("\n\nData of Write Back stage is =>");
			if(temp.getOpcode() != myOpcode.STORE ){
				System.out.println("OPCODE		=	"+temp.getOpcode());
				System.out.println("Destination	=	"+temp.getDest());
				System.out.println("PSW flag	=	"+ Container.PSW);
				System.out.println("PC		=	"+ currentPC);
				if(temp.getOpcode() == myOpcode.LOAD )
					System.out.println("Data in memory		=	"+ temp.getDestValue());
				else 
					System.out.println("Result in WB	=	"+ temp.getDestValue());
			}
		}
		}else
			System.out.println("\n\nNothing to show\n\n");
		
	}

	// Add instruction to fetched instructions map.
		void addToDecodedInstructionsMap(long pc,Instruction ins){
			Container.instructionsDecoded.put(pc, ins);
		}

		// Retrieve decoded instruction from instructionsDecoded map.
		public Instruction getDecodedInstruction(long pc){
			return Container.instructionsDecoded.get(pc);	
		}
		public boolean isInstructionDecoded(long pc){
			if(Container.instructionsDecoded.get(pc)!=null)
				return true;

			return false;
		}

		// Add instruction to fetched instructions map.
		public void addToFetchInstructionMap(long pc,String instruction){
			Container.instructionsFetched.put(pc, instruction);
		}

		// Retrieve fetched instruction from instructionsFetched map.
		public String getFetchedInstruction(long pc){
			return Container.instructionsFetched.get(pc);
		}

		// Retrieve total fetched instruction size.
		public long getFetchInstructionCount(){
			return Container.instructionsFetched.size();
		}

		// Retrieve total instruction size.
		public long getInstructionsCount(){
			return Container.instructions.size();
		}

		// Retrieve register value.
		public long getRegisterValue(String reg){
			if(Container.registers.containsKey(reg))
				return Container.registers.get(reg);
			
			return 0;
		}

		//Write result computed in execution unit to destination registers.
		public void addRegisterValue(String reg,long value){
			Container.registers.put(reg,value);
		}

		public void addfwdRegisterValue(String reg,long value){
			Container.fwdExRegisters.put(reg,value);
		}
		public long getfwdRegisterValue(String reg){
			if(Container.fwdExRegisters.containsKey(reg))
				return Container.fwdExRegisters.get(reg);
			return 0;
		}
		public void addfwdMemRegisterValue(String reg,long value){
			Container.fwdMemRegisters.put(reg,value);
		}
		public long getfwdMemRegisterValue(String reg){
			if(Container.fwdMemRegisters.containsKey(reg))
				return Container.fwdMemRegisters.get(reg);
			return 0;
		}
		
		
		// Retrieve register value.
		public long getMemoryValue(long memory){
			return Container.memory.get(memory);
		}

		//Write result computed in execution unit to destination registers.
		public long addMemoryValue(long memory,long value){
			return Container.memory.put(memory,value);
		}

		//get next memory location to have instruction on that memory.
		public long getNextMemory(){
			return ++Container.memory_current;
		}
		//Delete last stage info as instruction has been passed from that stage. 
		public void removeStageInfo(myStages stage){
			Container.stageInfo.remove(stage);
		}
		// Insert stage and instruction in it. So we can keep track of it. 
		public void insertStageInfo(myStages stage,long pc){
			Container.stageInfo.put(stage, pc);
		}
		//Retrieve instruction in specified stage.
		public Long getStageInfo(myStages stage){
			return Container.stageInfo.get(stage);
		}

		//Retrieve instruction from file.
		public String getInstruction(){
			return Container.instructions.get(Container.pc);
		}
		
		boolean isNextInstructionPresent(){
			if(Container.instructions.get(Container.pc)!=null)
				return true;
			
			return false;
		}

		//Retrieve instruction from file.
		public String getInstructionByPC(long pc){
			return Container.instructions.get(pc);
		}
	
		//Remove stall from pipeline.
		public void removePipelineStall(){
			Container.stall=false;
		}
		//Checks if is there any stall in pipeline.
		public boolean isPipelineStalled(){
			return Container.stall;
		}
		//Stall pipeline
		public void setPipelineStalled(boolean stall){
			Container.stall=true;
		}
		
		public void addTotalCompletedInstructions(){
			Container.totalCompletedInstruction++;
		}

		public long getTotalCompletedInstructions(){
			return Container.totalCompletedInstruction;
		}
		
		boolean isBzInstructionHasArrived(){
			return Container.bzInstruction;
		}
		void  setBzInstructionHasArrived(boolean arrived){
			 Container.bzInstruction=arrived;
		}
		
		void setBzInstructionPc(Instruction ins){
			Container.bzInstructionPc = ins.getPc();
		}
		long getBzInstructionPc(){
			return Container.bzInstructionPc ;
		}
		void setPSWValue(long psw){
			Container.PSW=psw;
		}
		long getPSWValue(){
			return Container.PSW;
		}		
}





class Forward {
	private static List<Long> dependList = new ArrayList<Long>();
	private static long dependPC;
	private static long currentDestValue;
	private static String dependSrc1 = "";
	private static String dependSrc2 = "";
	private static String currentDest = "";
	private static Instruction dependInst, currentInst ;
	
	// Scenario 1: If instruction in decode state is dependent on results from MEM state
	public static void forwardScenario1(long currentPC){
		dependPC = currentPC+2;
		if (Container.instructionsFetched.containsKey(dependPC)){
			dependInst = Container.instructionsDecoded.get(dependPC);
			currentInst = Container.instructionsDecoded.get(currentPC);
			dependList = dependInst.getDependenyOn();
			dependSrc1 = dependInst.getSrc1();
			dependSrc2 = dependInst.getSrc2();
			currentDest = currentInst.getDest();
			currentDestValue = currentInst.getDestValue();
		
		
			if (dependList.size() > 0 ){
				for (long pc : dependList){
					if (pc == currentPC){
						if (dependSrc1.equals(currentDest))
							Container.registers.put(dependSrc1,currentDestValue);
						if (dependSrc2.equals(currentDest))
							Container.registers.put(dependSrc2,currentDestValue);
				 //Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
						Container.instructionsDecoded.get(currentPC).setCompleted(true);
						Container.stall=false;
						break;
					}
				}
				
			}
			
			if (dependList.size() > 0 ){
				for (long pc : dependList){
					if (pc == currentPC){
						if (dependSrc1.equals(currentDest)){
							Container.registers.put(dependSrc1,currentDestValue);
							dependInst.setSrc1Value(currentDestValue);
							}
				 //Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
						Container.instructionsDecoded.get(currentPC).setCompleted(true);
						Container.stall=false;
						break;
					}
				}
				
			}	
			
			if (dependList.size() > 0){
				for (long pc : dependList){
					if (pc == currentPC){
						if (dependSrc2.equals(currentDest))
							Container.registers.put(dependSrc2,currentDestValue);
				 //Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
						Container.instructionsDecoded.get(currentPC).setCompleted(true);
						Container.stall=false;
						break;
					}
				}
				
			}
	 }
		
	}	
	
	// Scenario 3: LOAD can forward the value to <rsrc1> of STORE instruction from MEM, which is coming out of EX-Stage into MEM-Stage.
	public static void forwardScenario3(long currentPC){
		dependPC = currentPC+1;
		dependInst = Container.instructionsDecoded.get(dependPC);
		currentInst = Container.instructionsDecoded.get(currentPC);
		dependSrc1 = dependInst.getSrc1();
		currentDest = currentInst.getDest();
		currentDestValue = currentInst.getDestValue();
	
	
		if (dependSrc1.equals(currentDest)){
			Container.registers.put(dependSrc1,currentDestValue);
			dependInst.setSrc1Value(currentDestValue);
			}
		
		//Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
		Container.instructionsDecoded.get(currentPC).setCompleted(true);
		
	}	
	
	
	// Scenario 4: LOAD can forward the value to <rsrc1> of STORE instruction from WB, which is coming out of EX-Stage into MEM-Stage.
	public static void forwardScenario4(long currentPC){
		dependPC = currentPC+2;
		dependInst = Container.instructionsDecoded.get(dependPC);
		currentInst = Container.instructionsDecoded.get(currentPC);
		dependSrc1 = dependInst.getSrc1();
		currentDest = currentInst.getDest();
		currentDestValue = currentInst.getDestValue();
	
	
		if (dependSrc1.equals(currentDest)){
			Container.registers.put(dependSrc1,currentDestValue);
			dependInst.setSrc1Value(currentDestValue);
			}
		
		//Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
		Container.instructionsDecoded.get(currentPC).setCompleted(true);
		
	}	
	
	// Scenario 5: If instruction in decode state is dependent on Instruction in execution state
	public static void forwardScenario5(long currentPC){
		dependPC = currentPC+1;
		if (Container.instructionsFetched.containsKey(dependPC)){
			dependInst = Container.instructionsDecoded.get(dependPC);
			currentInst = Container.instructionsDecoded.get(currentPC);
			dependList = dependInst.getDependenyOn();
			dependSrc1 = dependInst.getSrc1();
			dependSrc2 = dependInst.getSrc2();
			currentDest = currentInst.getDest();
			currentDestValue = currentInst.getDestValue();
		
		
			if (dependList.size() > 0 ){
				for (long pc : dependList){
					if (pc == currentPC){
						if (dependSrc1.equals(currentDest))
							Container.registers.put(dependSrc1,currentDestValue);
						if (dependSrc2.equals(currentDest))
							Container.registers.put(dependSrc2,currentDestValue);
				//Now set current instruction to be same as complete so that dependency can be decided by isDependencyResolved function
						Container.instructionsDecoded.get(currentPC).setCompleted(true);
						break;
					}
				}
				
			}
		}
	}
	
	// Specific case of scenario 5: Used by MOV instruction to make the value available to register as soon as its executed in EX stage
	public static void forwardScenario6(long currentPC){
		currentInst = Container.instructionsDecoded.get(currentPC);
		currentDest = currentInst.getDest();
		currentDestValue = currentInst.getDestValue();
		Container.registers.put(currentDest,currentDestValue);
		Container.instructionsDecoded.get(currentPC).setCompleted(true);
		}


}

