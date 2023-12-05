package pl.janczaja;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {

        System.out.println("Start");

        ProblemGenerator problemGenerator = new ProblemGenerator("problem1",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        problemGenerator.test1();
        problemGenerator.test2();

        System.out.println("Stop");
    }
}