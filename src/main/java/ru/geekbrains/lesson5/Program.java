package ru.geekbrains.lesson5;

public class Program {

    public static void main(String[] args) {
        System.out.println(parseMessage("Перт привет"));

    }
    public static String parseMessage(String massage) {
        String[] strings = massage.strip().split(" ");
        if (strings[0].startsWith("+")) {

            return strings[0].substring(1);
        }

        return massage;
    }
}

