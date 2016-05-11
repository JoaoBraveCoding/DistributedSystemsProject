package pt.upa.broker;

import java.util.Scanner;

import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.TransportView;

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
    Scanner sc = new Scanner(System.in);
    String input = "";
    String result = "";
    String id = "";
    while(true){
      input = sc.nextLine();
      if(input.equals("quit")){
        break;
      }
      if(input.equals("ping")){
        result = client.ping("Client");
        System.out.println(result);
      } else if(input.equals("request")){
        id = client.requestTransport("Coimbra","Lisboa", 49);
        System.out.println(result);
      } else if(input.equals("view")){
        if(id.equals("")){
          System.out.println("You must first steal a boat, mate!");
        } else {
          System.out.println("Viewing tranport: "+id);
          TransportView tv = client.viewTransport(id);
          System.out.println(tv.getOrigin());
          System.out.println(tv.getDestination());
          System.out.println(tv.getPrice());
          System.out.println(tv.getState().toString());
        }
      }
      // while(sc.hasNextLine()) System.out.println(sc.nextLine());
    }
    sc.close();
    //String result = client.ping("Client");
    //String result = client.requestTransport("Coimbra","Lisboa", 49);
    //System.out.println(result);
  }
}