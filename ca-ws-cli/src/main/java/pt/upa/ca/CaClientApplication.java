package pt.upa.ca;

import pt.upa.ca.ws.cli.CaClient;

public class CaClientApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
    if (args.length < 2) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s uddiURL name%n", CaClientApplication.class.getName());
      return;
    }

    System.out.println(CaClientApplication.class.getSimpleName() + " starting...");    
    
    CaClient client = new CaClient(args[0], args[1]);
    
    System.out.println("Remote call ...");
    String result = client.ping("Client");
    System.out.println(result);
  }
}