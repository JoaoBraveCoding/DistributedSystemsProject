package pt.upa.broker;

import pt.upa.broker.ws.cli.BrokerClient;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
    if (args.length < 2) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s uddiURL name%n", BrokerClientApplication.class.getName());
      return;
    }

    System.out.println(BrokerClientApplication.class.getSimpleName() + " starting...");    
    
    BrokerClient client = new BrokerClient(args[0], args[1]);
    
    System.out.println("Remote call ...");
    //String result = client.ping("Client");
    String result = client.requestTransport("Coimbra","Lisboa", 49);
    System.out.println(result);
  }
}