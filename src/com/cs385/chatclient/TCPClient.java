package com.cs385.chatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient implements Runnable
{
	private DataOutputStream dOut;
	private DataInputStream dIn;
	private boolean run;
	private int port;
	private InetAddress serverAddr;
	private InputHandler inputHdlr;
	private boolean isGoingtoCrash;
	
	public static final String HELLO_MSG = "com.cs385.chatclient.HELLO_MSG";
	
	public TCPClient(InputHandler inputHdlr)
	{
		this.run = true;
		this.port = 27001;
		this.isGoingtoCrash = false;
		this.inputHdlr = inputHdlr;
		try
		{
			/*
			 * NUIMWireless : 192.168.4.178
			 * Eduroam : 149.157.36.38
			 * Eircom : 192.168.1.2
			 * 
			 */
			this.serverAddr = InetAddress.getByName("192.168.1.2");
			
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String msg)
	{
		try
		{
			this.dOut.writeUTF(msg);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			Socket socket = new Socket(this.serverAddr,this.port);
			this.dOut = new DataOutputStream(socket.getOutputStream());
			
			this.dIn = new DataInputStream(socket.getInputStream());
			
			if(dOut == null)
			{
				this.isGoingtoCrash = true;
			}
			
			String msg;
			while(this.run)
			{
				msg = dIn.readUTF();
				if(msg != null)
				{
					this.inputHdlr.onMessageRecieve(msg);
				}
				msg = null; // reset the message : i dont want to read it several time
			}
			//close the socket when we exit the loop
			socket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean hasConnectionProblem()
	{
		return this.isGoingtoCrash;
	}
}
