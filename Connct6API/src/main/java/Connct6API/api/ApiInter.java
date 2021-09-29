package Connct6API.api;

public interface ApiInter {
	String letsConnect(String ip, int port, String col);

	String drawAndWait(String draw);

	String getBoard(String ask);

}
