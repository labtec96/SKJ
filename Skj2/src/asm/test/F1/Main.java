package asm.test.F1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main
{
	public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException
	{
		Scanner sk = new Scanner(System.in);
		final long poczatkowaWartoscLicznika = Long.parseLong(args[0]);
		final long coIleSynchronizacja = Long.parseLong(args[1])*1000;
		final int port = Integer.parseInt(args[2]);
		System.out.println("Pracuje na porcie " + port);
		InetAddress localHost = Inet4Address.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
		String ipAddress = localHost.getHostAddress();
		System.out.println(ipAddress);
		String subnetMask = Integer.toString(networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
		String ipAddresBinary = ZmianaNaBinarny(ipAddress.split("\\."));
		String broadCastBinary = AdresBroadCast(ipAddresBinary.replace(".", ""), Integer.parseInt(subnetMask));
		String broadCast = broadCastBinary(broadCastBinary.split("\\."));
		OdliczanieCzasuWatek czas = new OdliczanieCzasuWatek(poczatkowaWartoscLicznika,coIleSynchronizacja);
		czas.start();
		UDPServer server = new UDPServer(port, czas,coIleSynchronizacja);
		server.start();
		System.out.println("Co ile synchronizacja "+coIleSynchronizacja);
		UDPClient client = new UDPClient(broadCast,czas,coIleSynchronizacja, port);
		client.start();
		sk.next();
		
	}
	public static String ZmianaNaBinarny(String[] ipAdresBinaryTab )
	{
		String ipAddresBinary ="";
		for (String string : ipAdresBinaryTab)
		{
			String edited="";
			if ((Integer.toBinaryString(Integer.parseInt(string))+".").length()!=9)
			{
				int liczbaZerDoDopisania =9-(Integer.toBinaryString(Integer.parseInt(string))+".").length();
				for (int i = 0; i < liczbaZerDoDopisania; i++)
				{
					edited+="0";
				}
					edited+=Integer.toBinaryString(Integer.parseInt(string))+".";
			}
			else
				edited=(Integer.toBinaryString(Integer.parseInt(string))+".");
			ipAddresBinary += edited;
		}
		return ipAddresBinary;
	}
	public static String AdresBroadCast(String ip, int maska)
	{
		String broadCast="";
		for (int i=0; i<maska; i++)
		{
			broadCast+=ip.charAt(i);
		}
		for (int i=broadCast.length(); i<32; i++)
		{
			broadCast+="1";
		}
		String tmp ="";
		for (int i=1; i<=4; i++)
		{
		
			tmp += broadCast.substring((i-1)*8, i*8)+".";
		}
		return tmp;
	}
	public static String broadCastBinary(String[] binaryBroad)
	{
		String broadCast="";
		for (int i = 0; i <=3; i++)
		{
			broadCast+=Integer.parseInt(binaryBroad[i], 2)+".";
		}
		return broadCast.substring(0, broadCast.length()-1);
	}
}


class UDPServer extends Thread
{
	final int port;
	OdliczanieCzasuWatek czas;
	final long coIleSynchronizacja;
	public UDPServer(int port, OdliczanieCzasuWatek czas, long coIleSynchronizacja)
	{
		this.port = port;
		this.czas = czas;
		this.coIleSynchronizacja = coIleSynchronizacja;
	}

	@Override
	public void run()
	{
		try
		{
			DatagramSocket serverSocket = new DatagramSocket(port);
			serverSocket.setBroadcast(true);
			byte[] receiveData = new byte[1024];
			byte[] sendData;

			while (true)
			{
				
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String sentence =new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println("Ktos sie polaczyl do serwera ");
				System.out.println(sentence);
				String[] rzadania =sentence.split(" ");
				if (rzadania[0].equals("get"))
				{
					if(rzadania[1].equals("counter"))
					{
						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
						buffer.putLong(czas.getWatoscLicznika());
						sendData = buffer.array();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						serverSocket.disconnect();
					}
					if (rzadania[1].equals("period"))
					{
						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
						buffer.putLong(czas.getCoIleSynchronizacja()/1000);
						sendData = buffer.array();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						serverSocket.disconnect();
					}
				}
				else if (rzadania[0].equals("sett"))
				{
					long nowaWartosc = Long.parseLong(rzadania[2]);
					if(rzadania[1].equals("counter"))
					{
						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						czas.setWatoscLicznika(nowaWartosc);
						sendData = "Potwierdzona zmiana czasu".getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						serverSocket.disconnect();
					}
					if (rzadania[1].equals("period"))
					{
						InetAddress IPAddress = receivePacket.getAddress();
						int port = receivePacket.getPort();
						czas.setCoIleSynchronizacja(nowaWartosc*1000);;
						sendData = "Potwierdzona zmiana czasu Synchronizacji".getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
						serverSocket.disconnect();
					}
				}
			}
		} catch (Exception e)
		{
			System.err.println(e);
		}
	}

}

class UDPClient extends Thread
{
	String broadCast;
	OdliczanieCzasuWatek czas;
	long coIleSynchronizacja;
	int port;
	public UDPClient(String broadCast, OdliczanieCzasuWatek czas, long coIleSynchronizacja, int port)
	{
		this.czas = czas;
		this.broadCast = broadCast;
		this.coIleSynchronizacja = coIleSynchronizacja;
		this.port = port;
	}

	@Override
	public void run()
	{
		try
		{
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(broadCast);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			while (true)
			{
				long sumaCzasu = 0;
				int iloscAgentow = 0;
				for (int i = 0; i < 20; i++)
				{
					if (10000 + i != port)
					{
						System.out.println("wysłano na port: " + 10000 + i);
						String sentence = "get counter";
						sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sentence.getBytes().length, IPAddress,
								10000 + i);
						clientSocket.send(sendPacket);
					}
				}
				clientSocket.setSoTimeout(40);
				boolean dopoki=true;
				try
				{
					while(dopoki)
					{
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						clientSocket.receive(receivePacket);
						System.out.println(receivePacket.getPort());
						ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
						buffer.put(receivePacket.getData(),0,receivePacket.getLength());
						buffer.flip();
						Long  modifiedSentence = buffer.getLong();
						sumaCzasu+=modifiedSentence;
						iloscAgentow++;
					}
				} catch (SocketTimeoutException e)
				{
					dopoki = false;
				}
				czas.ustawienieCzasu(sumaCzasu, iloscAgentow);
				sleep(czas.getCoIleSynchronizacja());
			
			}
		} catch (Exception e)
		{

		}
	}
}

class OdliczanieCzasuWatek extends Thread
{
	long wartoscLicznika;
	long coIleSynchronizacja;
	public OdliczanieCzasuWatek(long wartoscPoczatkowa,long coIleSynchronizacja)
	{
		this.wartoscLicznika = wartoscPoczatkowa;
		this.coIleSynchronizacja = coIleSynchronizacja;
	}

	public void run()
	{
		try
		{
			while (true)
			{
				Thread.sleep(1);
				this.wartoscLicznika = wartoscLicznika + 1;
				// System.out.println(wartoscLicznika);
			}
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	synchronized public void ustawienieCzasu(long czasDoDodania, int iloscCzasow)
	{
		System.out.println("Ustawiam nowy czas na " + (czasDoDodania + this.wartoscLicznika) / (iloscCzasow+1));
		this.wartoscLicznika = (czasDoDodania + this.wartoscLicznika) / (iloscCzasow+1);
	}
	
	synchronized public void setWatoscLicznika(long nowaWartoscLicznika)
	{
		System.out.println("Ustawiam nowy czas na " + nowaWartoscLicznika);
		this.wartoscLicznika = nowaWartoscLicznika;
	}
	
	synchronized public long getWatoscLicznika()
	{
		return wartoscLicznika;
	}

	public synchronized long getCoIleSynchronizacja()
	{
		return coIleSynchronizacja;
	}

	public synchronized void setCoIleSynchronizacja(long coIleSynchronizacja)
	{
		System.out.println("Ustawiam nowy czas synchronizacji na " + coIleSynchronizacja);
		this.coIleSynchronizacja = coIleSynchronizacja;
	}
	

}