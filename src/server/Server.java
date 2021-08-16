package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {
    public static void main(String[] args) {
        {
            ArrayList<User> users = new ArrayList<>();
            ArrayList<String> usersName = new ArrayList<>();
            try {
                ServerSocket serverSocket = new ServerSocket(8188); // Создаём серверный сокет
                System.out.println("Сервер запущен");
                while (true){ // Бесконечный цикл для ожидания родключения клиентов
                    Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                    System.out.println("Клиент подключился");
                    User currentUser = new User(socket);
                    users.add(currentUser);
                    DataInputStream in = new DataInputStream(currentUser.getSocket().getInputStream()); // Поток ввода
                    ObjectOutputStream oos = new ObjectOutputStream(currentUser.getSocket().getOutputStream()); // Поток вывода
                    currentUser.setOos(oos);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                currentUser.getOos().writeObject("Добро пожаловать на сервер");
                                currentUser.getOos().writeObject("Введите ваше имя: ");
                                String userName = in.readUTF(); // Ожидаем имя от клиента
                                currentUser.setUserName(userName);
                                usersName.add(currentUser.getUserName()); // Добавляем имя пользователя в коллекцию
                                for (User user : users) {
                                    user.getOos().writeObject(currentUser.getUserName()+" присоединился к беседе");
                                    user.getOos().writeObject(usersName);
                                }
                                while (true){
                                    String request = in.readUTF(); // Ждём сообщение от пользователя
                                    System.out.println(currentUser.getUserName()+": "+request);
                                    for (User user : users) {
                                        if(users.indexOf(user) == users.indexOf(currentUser)) continue;
                                        user.getOos().writeObject(currentUser.getUserName()+": "+request);
                                    }
                                }
                            }catch (IOException e){
                                users.remove(currentUser);
                                usersName.remove(currentUser.getUserName());
                                for (User user : users) {
                                    try {
                                        user.getOos().writeObject(currentUser.getUserName()+" покинул чат");
                                        user.getOos().writeObject(usersName);
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                   thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}