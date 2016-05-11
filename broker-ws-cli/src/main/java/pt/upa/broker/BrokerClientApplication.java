package pt.upa.broker;

import java.util.Scanner;

import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;

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
      System.out.print("$");
      input = sc.nextLine();
      if(input.equals("quit")){
        break;
      }
      if(input.equals("ping")){
        result = client.ping("Client");
        System.out.println(result);
      } else if(input.equals("list")){
        System.out.println("\nSize of list: " + client.listTransports().size());
      } else if(input.equals("clear")){
        id ="";
        client.clearTransports();
      } else if(input.equals("request1")){
        int randomPrice = (int) (Math.random() * ( 100 - 10 ));
        System.out.println("\nRequesting pirate trip from Coimbra to Lisboa. Bounty be: " + randomPrice);
        id = client.requestTransport("Coimbra","Lisboa", randomPrice);
        System.out.println("Request result: Transport id -" + id);
      } else if(input.equals("request2")){
        int randomPrice = (int) (Math.random() * ( 100 - 10 ));
        try{
          System.out.println("\nRequesting pirate trip from Porto to Beja. Bounty be: " + randomPrice);
          id = client.requestTransport("Porto","Beja", randomPrice);
          System.out.println("Request result: Transport id -" + id);
        } catch (UnavailableTransportFault_Exception e){
          System.out.println("\nCannot do that kind of trip..\nException: " + e.getMessage());
        }
      } else if(input.equals("view")){
        if(id.equals("")){
          System.out.println("\nYou must first steal a ship, mate!");
        } else {
          System.out.println("\nViewing tranport: "+id);
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