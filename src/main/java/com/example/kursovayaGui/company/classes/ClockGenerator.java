package com.example.kursovayaGui.company.classes;

public class ClockGenerator implements Runnable {
    private static int time = 0;
    private static volatile boolean shutDown = false;

    public static void incTime(int tact){
        time += tact;
    }

    public static void incTime(){
        time++;
    }

    public static int getTime() {
        return time;
    }

    public static void setShutDown(boolean value){
        shutDown = value;
    }

    @Override
    public void run() {
        while (!shutDown){
            incTime();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
