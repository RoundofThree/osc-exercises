package ch4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class NamingServer {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            ServerSocket ss = new ServerSocket(6052);
            while (true) {
                Socket s = ss.accept();
                pool.execute(new IpPrinter(s));
            }
            // ss.close();
        } catch (IOException e) {
            System.err.println("Error");
        } finally {
            pool.shutdown();
        }
    }
}

class IpPrinter implements Runnable {
    private Socket c;
    public IpPrinter(Socket s) {
        this.c = s;
    }
    public void run() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
            PrintWriter w = new PrintWriter(c.getOutputStream(), true); // do not buffer
            String l = r.readLine();
            if (l == null) return;
            try {
                String ret = InetAddress.getByName(l).getHostAddress();
                w.println(ret);
            } catch (UnknownHostException e) {
                w.println("Unable to resolve host <"+ l + ">");
            }

        } catch (IOException e) {}
    }
}