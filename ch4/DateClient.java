package ch4;

import java.net.*;
import java.io.*;

class DateClient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 3330); 
            InputStream is = s.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);
            // get a sentence from user
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
            String line = "";
            // System.out.println("Hello");  //d
            while ((line = in.readLine())!=null) {
                w.println(line);  // send to server 
                System.out.println(r.readLine());  // maybe this is non-blocking, error then 
            }
            s.close();
        } catch (IOException e) {
            System.err.println("Error");
        }
    }
}