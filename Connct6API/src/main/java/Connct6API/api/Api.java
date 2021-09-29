package Connct6API.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Api implements ApiInter {

//	public static void main(String[] args) {
//		Api api = new Api();
//		System.out.println(api.getValid("a3:i13"));
//	}

	// static numbers
	final static private int EMPTY = 0;
	final static private int BLACK = 1;
	final static private int WHITE = 2;
	final static private int RED = 3;

	private InputStream input;
	private OutputStream output;
	private static Board board;
	private Socket socket = null;
	private int color = 0;
	private int opponent = 0;

	@Override
	public String letsConnect(String ip, int port, String col) {

		board = new Board();

		// connect
		try {
			socket = new Socket(ip, port);
			System.out.println("Socket connected to ip: " + ip + " port: " + port);

			socket.setTcpNoDelay(true); // no delay protocol
			
			output = socket.getOutputStream(); // to server
			input = socket.getInputStream(); // from server
		}
		catch (UnknownHostException e) {
			System.err.println("IP not determined");
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			System.err.println("Invalid port values");
			e.printStackTrace();
		}
		catch (SocketException e1) {
			System.err.println("Socket Exception");
			e1.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("IOException");
			e.printStackTrace();
		}

		// get color
		if (col.toLowerCase().compareTo("white") == 0) {
			color = WHITE;
			opponent = BLACK;
		} else if (col.toLowerCase().compareTo("black") == 0) {
			color = BLACK;
			opponent = WHITE;
		} else {
			System.err.println("wrong color input");
			System.exit(1);
		}

		// get red stones from server
		byte[] red = new byte[15];
		byte[] num_string = new byte[4];

		int num_int = 0;

		try {
			input.read(num_string, 0, 4);
			num_int = byteToInt(num_string);
			red = new byte[num_int];
			input.read(red, 0, num_int);
		} catch (IOException e) {
			System.err.println("IOException: read red stones");
			e.printStackTrace();
		}

		System.out.println("[connect] Got red stones: " + new String(red));

		String[] red_stones = new String(red).split(":");

		for (String stone : red_stones) {
			board.putStone(stone, RED);
		}

		return new String(red);
	}

	@Override
	public String drawAndWait(String draw) {								// here - check valid input?
		
		System.out.println("[drawAndWait] draw: " + draw);

		// if empty string --> just wait without draw
		if (draw.compareTo("") != 0) {
			draw(draw);
		}

		String result = waitStones();

		board.printBoard();

		return result;
	}

	@Override
	public String getBoard(String ask) {									// here - return value unification?
		int col = board.getColor(ask);
		String color = "ERROR";
		switch (col) {
			case EMPTY:
				color = "EMPTY";
				break;
			case WHITE:
				color = "WHITE";
				break;
			case BLACK:
				color = "BLACK";
				break;
			case RED:
				color = "RED";
				break;
		}
		return color;
	}

	private void draw(String draw) {

		String drawValid = getValid(draw);
		String error = null;

		String[] stones = drawValid.split(":");
		
		if(stones.length == 1) {
			if(stones[0].toLowerCase().compareTo("k10") != 0) {
				error = "BADINPUT";
			}
		}
		else if(stones.length != 2) {
			error = "BADINPUT";
		}

		for (String stone : stones) {
			String err = board.putStone(stone, color);
			if(err != null) {
				error =  err;
				break;
			}
		}
		String message = "";
		if(error != null) {
			message = error + "$" + drawValid;
		}
		else {
			message = drawValid;
		}

		// send input or error message
		byte[] send = message.getBytes();
		int length_send = send.length;

		try {
			output.write(intToByte(length_send)); // size of message
			output.write(send); // message
			output.flush();
		} catch (IOException e) {
			System.err.println("IOException - draw");
			e.printStackTrace();
		}
	}

	private String waitStones() {
		byte[] getBytes;
		byte[] string_size = new byte[4];
		int num_int = 0;
		String result = null;

		try {
			input.read(string_size, 0, 4);
			num_int = byteToInt(string_size);
			getBytes = new byte[num_int];
			input.read(getBytes, 0, num_int);
			result = new String(getBytes, 0, num_int);
		} catch (IOException e) {
			System.err.println("IOException - wait");
			e.printStackTrace();
		}

		System.out.println("[waitStones] result: " + result);

		String[] stones = result.split(":");

		for (String stone : stones) {
			board.putStone(stone, opponent);
		}

		return result;
	}

	private int byteToInt(byte[] bytes) {
		return ((bytes[3] & 0xFF) << 24) | ((bytes[2] & 0xFF) << 16) | ((bytes[1] & 0xFF) << 8)
				| ((bytes[0] & 0xFF) << 0);
	}

	private byte[] intToByte(int intValue) {
		byte[] byteArray = new byte[4];
		byteArray[3] = (byte) (intValue >> 24);
		byteArray[2] = (byte) (intValue >> 16);
		byteArray[1] = (byte) (intValue >> 8);
		byteArray[0] = (byte) (intValue);
		return byteArray;
	}

	/*
	 * changes 1 digit to 2 digits ex) a3 -> a03
	 */
	private String getValid(String in) {
		String[] stones = in.split(":");

		for (int i=0; i< stones.length; i++) {
			stones[i] =  Character.toUpperCase(stones[i].charAt(0)) + stones[i].substring(1);
			if (stones[i].length() == 2) {
				stones[i] = stones[i].charAt(0) + "0" + stones[i].charAt(1);
			}
		}

		String out = String.join(":", stones);
		
		return out;
	}

}
