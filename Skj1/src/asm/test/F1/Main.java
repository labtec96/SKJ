package asm.test.F1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Main
{
	public static void main(String[] args) throws IOException, InterruptedException
	{	
		HashMap<Integer, String> mapaAdresow = new HashMap<>();
		Scanner sk = new Scanner(System.in);
		final String clientIpAddres = InetAddress.getByName("localhost").getHostAddress();
		
		if (args.length == 4)
		{
			final String wprowadzajacyIp= InetAddress.getByName(args[0]).getHostAddress();
			final int wprowadzajacyPort = Integer.parseInt(args[1]);
			final long wartoscPoczatkowaLicznika = Integer.parseInt(args[2]);
			final int serverPort = Integer.parseInt(args[3]);
			OdliczanieCzasuWatek odliczanieCzasu = new  OdliczanieCzasuWatek(wartoscPoczatkowaLicznika);
			odliczanieCzasu.start();
			System.out.println("moj serverPort"+  serverPort);
			//Startowanie zegara
			//pobranie aktualnych adresow
			mapaAdresow.put(wprowadzajacyPort, wprowadzajacyIp);
			mapaAdresow.put(serverPort, clientIpAddres);
			MojSerwer server = new MojSerwer(serverPort,odliczanieCzasu, clientIpAddres,mapaAdresow,odliczanieCzasu);
			server.start();
			MojKlient klient =new MojKlient(serverPort, clientIpAddres,wprowadzajacyPort,wprowadzajacyIp,odliczanieCzasu); 
			klient.start();
			klient.join();
			mapaAdresow = klient.mapaAdresow();
			server.setmapaAdersow(mapaAdresow);
			
		}
		if (args.length==0)
		{
			final int serverPort = 5000;
			System.out.println("To wezel pierwszy");
			OdliczanieCzasuWatek odliczanieCzasu = new  OdliczanieCzasuWatek(5000);
			odliczanieCzasu.start();
			mapaAdresow.put(serverPort, clientIpAddres);
			MojSerwer server = new MojSerwer(serverPort,odliczanieCzasu, clientIpAddres,mapaAdresow,odliczanieCzasu);
			server.start();
		}
		if (args.length!=0 && args.length!=4)
		{
			System.out.println("Brak wszystkich parametrow");
			return;
		}    
		sk.next();
	}

	
}
class MojSerwer extends Thread
{
	int serverPort;
	String ServerIp;
	HashMap<Integer, String>mapaAdresow;
	OdliczanieCzasuWatek odliczanieCzasuWatek;
	public MojSerwer(int serverPort, OdliczanieCzasuWatek odliczanieCzasuWatek, String ServerIp, HashMap<Integer, String>mapaAdresow, OdliczanieCzasuWatek odliczanieCzasu)
	{
		this.ServerIp = ServerIp;
		this.serverPort = serverPort;
		this.mapaAdresow = mapaAdresow;
		this.odliczanieCzasuWatek = odliczanieCzasu;
		//System.out.println(mapaAdresow.size());
	}
	

	@Override
	public void run()
	{
		ServerSocket welcomeSocket;
		try
		{
			welcomeSocket = new ServerSocket(serverPort);
			while(true)
			{
					//System.out.println("rozmiar" + mapaAdresow.size());
                	Socket clientSocket = welcomeSocket.accept();
                	System.out.println("Kto sie polaczyl");
                	InputStream sis = clientSocket.getInputStream();
            		OutputStream sos = clientSocket.getOutputStream();
            		
            		InputStreamReader isr = new InputStreamReader(sis);
            		OutputStreamWriter osw = new OutputStreamWriter(sos);
            		
            		BufferedReader br = new BufferedReader(isr);
            		BufferedWriter bw = new BufferedWriter(osw);
            		String clientRequest = br.readLine();
            		if (clientRequest.equals("CLK"))
            		{
            			bw.write(Long.toString(odliczanieCzasuWatek.wartoscLicznika()));
                		bw.newLine();
                		bw.flush();
            		}
            		if (clientRequest.equals("NET"))
            		{
            			bw.write(Integer.toString(mapaAdresow.size()));
                		bw.newLine();
                		bw.flush();
                		
                			for (Map.Entry<Integer,String> entry : mapaAdresow.entrySet()) 
                			{
                				  Integer port = entry.getKey();
                				  String ip = entry.getValue();
                				  bw.write(Integer.toString(port) + " "+ ip);
                				  bw.newLine();
                				  bw.flush();
                			}
            		}
            		if (clientRequest.equals("ADR"))
            		{
            			//System.out.println("Odbieram " );
            			String[] line = br.readLine().split(" ");
    					System.out.println("Odbieram "  + line[0] +" "+ line[1]);
    					mapaAdresow.put(Integer.parseInt(line[0]), line[1]);
            		}
            		if (clientRequest.equals("SYN"))
            		{
            			Long sumaCzasu =0l;
            			for (Map.Entry<Integer,String> entry : mapaAdresow.entrySet()) 
            			{
            				Integer port = entry.getKey();
            				String ip = entry.getValue();
            				if (port != serverPort)
            				{
	            				Socket clientSocketsyn = new Socket(ip, port);
	            				InputStream sissyn = clientSocketsyn.getInputStream();
	            				OutputStream sossyn = clientSocketsyn.getOutputStream();
	
	            				InputStreamReader isrsyn = new InputStreamReader(sissyn);
	            				OutputStreamWriter oswsyn = new OutputStreamWriter(sossyn);
	
	            				BufferedReader brsyn = new BufferedReader(isrsyn);
	            				BufferedWriter bwsyn = new BufferedWriter(oswsyn);
	            				bwsyn.write("CLK");
	            				bwsyn.newLine();
	            				bwsyn.flush();
	            				//System.out.println("Od portu" + port);
	            				long czas = Long.parseLong(brsyn.readLine());
	            				//System.out.println("przeszlo");
	            				sumaCzasu+=czas;
	            				clientSocketsyn.close();
            				}
            			}
            			odliczanieCzasuWatek.ustawienieCzasu(sumaCzasu, mapaAdresow.size());
            		}
            		clientSocket.close();
            		
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setmapaAdersow( HashMap<Integer, String> mapaAdresow)
	{
		this.mapaAdresow = mapaAdresow;
	}
}
class MojKlient extends Thread
{
	public boolean lock = true;
	final String clientIpAddres;
	final int clientPort;
	final int wprowadzajacyPort;
	final String wprowadzajacyIp;
	HashMap<Integer, String> mapaAdresow = new HashMap<>();
	long sumaCzasu =0;
	OdliczanieCzasuWatek odliczanieCzasu;
	public MojKlient(int clientPort, String clientIpAddres,int wprowadzajacyPort, String wprowadzajacyIp, OdliczanieCzasuWatek odliczanieCzasu)
	{
		this.clientIpAddres = clientIpAddres;
		this.clientPort = clientPort;
		this.wprowadzajacyPort = wprowadzajacyPort;
		this.wprowadzajacyIp = wprowadzajacyIp;
		this.odliczanieCzasu = odliczanieCzasu;
		mapaAdresow.put(wprowadzajacyPort, wprowadzajacyIp);
		mapaAdresow.put(clientPort, clientIpAddres);
	}

	@Override
	public void run()
	{
		try
		{
				Socket clientSocket = new Socket(wprowadzajacyIp, wprowadzajacyPort);
				InputStream sis = clientSocket.getInputStream();
				OutputStream sos = clientSocket.getOutputStream();

				InputStreamReader isr = new InputStreamReader(sis);
				OutputStreamWriter osw = new OutputStreamWriter(sos);

				BufferedReader br = new BufferedReader(isr);
				BufferedWriter bw = new BufferedWriter(osw);

				bw.write("NET");
				bw.newLine();
				bw.flush();
				int iloscAgentowWsieci = Integer.parseInt(br.readLine());
			//	System.out.println(iloscAgentowWsieci);
				for (int i = 0; i < iloscAgentowWsieci; i++)
				{
					String[] line = br.readLine().split(" ");
					System.out.println("Dodaje do mapy w kliencie wartosci" +line[0] + " " + line[1]);
					mapaAdresow.put(Integer.parseInt(line[0]), line[1]);
				}
				clientSocket.close();
				for (Map.Entry<Integer,String> entry : mapaAdresow.entrySet()) 
    			{
					Integer port = entry.getKey();
    				String ip = entry.getValue();
    				if (port!=clientPort)
    				{
						clientSocket = new Socket(ip, port);
						sis = clientSocket.getInputStream();
						sos = clientSocket.getOutputStream();
	
						isr = new InputStreamReader(sis);
						osw = new OutputStreamWriter(sos);
	
						br = new BufferedReader(isr);
						bw = new BufferedWriter(osw);
	    				bw.write("ADR");
	    				bw.newLine();
	    				bw.flush();
	    				bw.write(Integer.toString(clientPort) + " " + clientIpAddres);
	    				bw.newLine();
	    				bw.flush();
	    				clientSocket.close();
    				}
    			}	
				clientSocket.close();
				for (Map.Entry<Integer,String> entry : mapaAdresow.entrySet()) 
    			{
					//System.out.println(mapaAdresow.size());
					Integer port = entry.getKey();
    				String ip = entry.getValue();
    				if (port!=clientPort)
    				{
						clientSocket = new Socket(ip, port);
						sis = clientSocket.getInputStream();
						sos = clientSocket.getOutputStream();
	
						isr = new InputStreamReader(sis);
						osw = new OutputStreamWriter(sos);
	
						br = new BufferedReader(isr);
						bw = new BufferedWriter(osw);
						bw.write("CLK");			
	    				bw.newLine();
	    				bw.flush();
	    				Long czas = Long.parseLong(br.readLine());
	    				sumaCzasu += czas;
	    				clientSocket.close();
    				}
    			}	
				odliczanieCzasu.ustawienieCzasu(sumaCzasu, mapaAdresow.size());
				for (Map.Entry<Integer,String> entry : mapaAdresow.entrySet()) 
				{
					Integer port = entry.getKey();
					String ip = entry.getValue();
					if (port !=clientPort)
					{
						Socket clientSocketSyn = new Socket(ip,port);
						
	                	InputStream sisSyn = clientSocketSyn.getInputStream();
	            		OutputStream sosSyn = clientSocketSyn.getOutputStream();
	            		
	            		InputStreamReader isrSyn = new InputStreamReader(sisSyn);
	            		OutputStreamWriter oswSyn = new OutputStreamWriter(sosSyn);
	            		
	            		BufferedReader brSyn = new BufferedReader(isrSyn);
	            		BufferedWriter bwSyn = new BufferedWriter(oswSyn);
	            		bwSyn.write("SYN");
	            		bwSyn.newLine();
	            		bwSyn.flush();
	            		sleep(50);
	            		clientSocketSyn.close();
					}
				}
	} catch (IOException | InterruptedException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
//	public Long czasPoSynchronizacji()
//	{
//		return czasPoSynchronizacji;
//	}
	public HashMap<Integer, String> mapaAdresow ()
	{
		return mapaAdresow;
	}
}

class OdliczanieCzasuWatek extends Thread
{
	long wartoscLicznika;
	
	public OdliczanieCzasuWatek(long wartoscPoczatkowa)
	{
		this.wartoscLicznika=wartoscPoczatkowa;
	}
	public void run()
	{
		try
		{
			while (true)
			{
				Thread.sleep(1);
				this.wartoscLicznika = wartoscLicznika+1;
				//System.out.println(wartoscLicznika);
			}
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void ustawienieCzasu(long czasDoDodania, int iloscCzasow)
	{
		this.wartoscLicznika = (czasDoDodania+this.wartoscLicznika)/(iloscCzasow);
	}
	public long wartoscLicznika()
	{
		return wartoscLicznika;
	}
}




