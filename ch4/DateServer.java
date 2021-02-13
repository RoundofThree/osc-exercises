package ch4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// javac -d . DateServer.java
// java ch4.DateServer 

// multithreaded server 
/*
class DateServer {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(3330); 
            while (true) {
                Socket s = ss.accept();
                System.out.println("A client connected!");
                Thread t = new Thread(new CharCountPrinter(s));
                t.start();
            }
            // ss.close();  // unreachable in theory 
        } catch (IOException e) {} 
    }
}
*/

class DateServer {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool(); 
        try {
            ServerSocket ss = new ServerSocket(3330);
            while (true) {
                Socket s = ss.accept();
                System.out.println("A client connected!");
                pool.execute(new CharCountPrinter(s));
            }
            // pool.shutdown();
        } catch (IOException e) {
            pool.shutdown();
        }
    }
}

// count the number of digits and chars in a message
class CharCountPrinter implements Runnable {
    private Socket client;
    public CharCountPrinter(Socket client) {
        this.client = client;
    }
    public void run() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = "";
            while ((line = r.readLine())!= "") {
                if (line == null) break;
                int ret = 0;
                for (char c : line.toCharArray()) {
                    if (c != ' ') ++ret;
                }
                // send to socket
                PrintWriter w = new PrintWriter(client.getOutputStream(), true);
                w.println(ret); 
            }
        } catch (IOException e) {}
    }
}