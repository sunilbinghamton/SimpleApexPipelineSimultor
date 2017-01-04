import java.io.Console;

public class Simulator {
	
	public static void main(String[] args) throws NumberFormatException, Exception {
		SimBackground helper=new SimBackground(args[0]);		
		String input = null;
		Console console = System.console();
		while(true){
			input = console.readLine("Please enter input : ");
			if(input.toLowerCase().equalsIgnoreCase("initialize"))
				helper.initialize();
			else if(input.toLowerCase().contains("simulate")){
				String arr[]=input.split(" ");
				helper.simulate(Long.parseLong(arr[1]));

				if(Container.halt || !helper.isNextInstructionPresent()){
					break;
				}
			}else if(input.toLowerCase().equalsIgnoreCase("display"))
				helper.display();
			else{
				System.out.println("Invalid input.");
				break;
			}
		}   
	}
}
