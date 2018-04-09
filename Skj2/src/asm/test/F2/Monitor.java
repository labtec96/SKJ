package asm.test.F2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Monitor
{

	public static void main(String[] args) throws IOException
	{
		Scanner sk = new Scanner(System.in);
		System.out.println(InetAddress.getLocalHost());
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		String rzadanie = args[2];
		String czegoRzadanie = args[3];
		System.out.println(args.length);
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(ip);
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		if (args.length == 4)
		{
			String sentence = rzadanie + " " + czegoRzadanie;
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sentence.getBytes().length, IPAddress, port);
			clientSocket.send(sendPacket);
			if (czegoRzadanie.equals("counter"))
				System.out.println("Wysłano rzadanie o wartosc counter");
			else if (czegoRzadanie.equals("period"))
				System.out.println("Wyslano rzadanie o wartosc period");
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			buffer.put(receivePacket.getData(),0,receivePacket.getLength());
			buffer.flip();
			Long  receiveSentence = buffer.getLong();
			System.out.println(receiveSentence);
		}
		else if (args.length ==5)
		{
			int nowaWartosc = Integer.parseInt(args[4]);
			String sentence = rzadanie + " " + czegoRzadanie + " " + nowaWartosc;
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sentence.getBytes().length, IPAddress, port);
			clientSocket.send(sendPacket);
			if (czegoRzadanie.equals("counter"))
				System.out.println("Wysłano rzadanie o zmiane counter");
			else if (czegoRzadanie.equals("period"))
				System.out.println("Wyslano rzadanie o zmiane period");
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String receiveSentence =new String(receivePacket.getData(), 0, receivePacket.getLength());
			System.out.println(receiveSentence);
		}
		sk.next();
	}

}
