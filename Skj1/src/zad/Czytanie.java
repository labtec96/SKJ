package zad;

import java.util.Scanner;

class Czytanie implements Runnable 
{
	
    volatile boolean keepRunning = true;

    public void run() 
    {
        System.out.println("Starting to loop.");
        Scanner sk = new Scanner(System.in);
        while (true) 
        {
        	if (sk.hasNext())
            {
        		if (sk.next().equals("ten"))
        		{
        		}
            }
        }
    }
}