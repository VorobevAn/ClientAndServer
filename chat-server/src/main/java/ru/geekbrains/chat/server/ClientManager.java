package ru.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;

public class ClientManager implements Runnable {

    private Socket socket;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String name;


    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            ClientManagerSingleton.getInstance().add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * Завершение работы всех потоков, закрытие соединения с клиентским сокетом,
     * удаление клиентского сокета из коллекции
     *
     * @param socket         клиентский сокет
     * @param bufferedReader буфер для чтения данных
     * @param bufferedWriter буфер для отправки данных
     */
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление клиента из коллекции
     */
    private void removeClient() {
        ClientManagerSingleton.getInstance().remove(this);
        System.out.println(name + " покинул чат.");
    }

    @Override
    public void run() {
        String massageFromClient;

        // Цикл чтения данных от клиента
        while (socket.isConnected()) {
            try {
                // Чтение данных
                massageFromClient = bufferedReader.readLine();
                if (!sendingPrivateMessages(massageFromClient)){
                    broadcastMessage(massageFromClient);
                }

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    /**
     * Отправка сообщения всем слушателям
     *
     * @param massage сообщение
     */
    private void broadcastMessage(String massage) {
        for (ClientManager clients : ClientManagerSingleton.getInstance()) {
            if (!clients.name.equals(name)) {
                try {
                    clients.bufferedWriter.write(massage);
                    clients.bufferedWriter.newLine();
                    clients.bufferedWriter.flush();


                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }

            }
        }
    }

    /**
     * Расспасивание сообщения
     * @param massage полученное сообщение
     * @return имя получателя
     */
    public String parseMessage(String massage) {
        String[] strings = massage.strip().split(" ");
        if (strings[1].startsWith("+")) {
            return strings[1].substring(1);
        }

        return massage;
    }

    /**
     * Отправка индивидуального сообщения
     * @param massage сообщение
     * @return
     */
    public boolean sendingPrivateMessages(String massage) {
        String mames = parseMessage(massage);
        for (ClientManager client : ClientManagerSingleton.getInstance()) {
            try {
                if (client.name.equals(mames)) {
                    client.bufferedWriter.write(massage);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    return true;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }
        return false;
    }
}
