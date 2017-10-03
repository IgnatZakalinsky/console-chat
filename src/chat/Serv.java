package rpg2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * class start server
 */
public class Serv {
    public static volatile ArrayList<String> at = new ArrayList<>(); // история сообщений
    public static volatile boolean change = false; // регистр изменения истории
    public static volatile ArrayList<Socket> as = new ArrayList<>(); // для парралельных подключений
    private final static Object lock = new Object();

    public static void main(String[] args) {
        new ClassServ(); // класс для парралельного оповещение клиентов о изменении истории

        Socket s;
        try {
            ServerSocket ss = new ServerSocket(5050); // open port
            while (true) {

                s = ss.accept();

                synchronized (lock) {
                    as.add(s);
                    new ClassCli(as.get(as.size() - 1)); // класс для парралельной обработки
                }

            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClassCli extends Thread { // класс для парралельной обработки
        Socket s;

        public ClassCli(Socket s) {
            this.s = s;
            start();
        }

        public void run() {
            BufferedReader in;
            boolean q = true;
            String t, sin = "";

            while (q) {

                try {
                    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    sin = in.readLine();
                    t = (new Date()).toString().substring(11, 19) + ": "; // time "xx:yy:zz: "

                    t += sin;

                    synchronized (lock) {
                        Serv.at.add(t); // добавление в историю

                        System.out.println(t);

                        Serv.change = true; // оповещение о изменении
                    }
                } catch (java.io.IOException e) {
                    //e.printStackTrace();
                }

                if ((sin.equals("q")) || (sin == null)) q = false;
            }
        }
    }

    public static class ClassServ extends Thread { // класс для парралельного оповещение клиентов о изменении истории
        public ClassServ() {
            start();
        }

        public void run() {
            BufferedWriter out;
            boolean i;

            while (true) {
                synchronized (lock) {
                    i = change;
                }


                if (i) {

                    synchronized (lock) {

                        for (Socket s : as) { // перебор подключений

                            try {
                                out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                out.write(at.size() + "\n");
                                out.flush();
                                for (String s2 : at) { // отправка всей истории
                                    out.write(s2 + "\n");
                                    out.flush();
                                }
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }

                            //s.close();
                        }
                        change = false;
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
