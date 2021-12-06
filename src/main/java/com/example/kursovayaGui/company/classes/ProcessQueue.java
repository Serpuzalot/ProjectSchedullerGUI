package com.example.kursovayaGui.company.classes;

import com.example.kursovayaGui.company.interfaces.Queue;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;

public class ProcessQueue implements Queue,Runnable {
    private int lastProcessId;
    private static ArrayList<Process> queue;
    private static ArrayList<Process> finishedQueue;
    private static boolean shutDown;

    public ProcessQueue() {
        queue = new ArrayList<>();
        finishedQueue = new ArrayList<>();
        this.lastProcessId = 2 ;
        shutDown = false;
    }

    public int getLastProcessId() {
        return lastProcessId;
    }

    public void add(Process process){
        Process fullProcess = new Process(getLastProcessId(),process.getMemory(),process.getPriority(),process.getTimeIn(),process.getBurstTime(),process.getState());
        queue.add(fullProcess);
        lastProcessId++;
    }

    public void  add(Process process,MemoryBlock mb){
        Process fullProcess = new Process(getLastProcessId(),process.getMemory(),process.getPriority(),process.getTimeIn(),process.getBurstTime(),process.getState());
        MemoryScheduler.findMostSuitableAvailableMemoryBlock(fullProcess.getMemory(),fullProcess.getId(),mb);
        queue.add(fullProcess);
        lastProcessId++;
    }

    public void add(String name,int id){

        Process process = new Process(name,id);
        this.queue.add(process);

    }

    public Process getHighPriorityProcess(){
        //получает процесс с наивысшим приоритетом и статусом Готов
        //наивысший приоритет 1
        Process process = new Process();
        for(int i =0;i<queue.size();i++){
            synchronized (queue){
                if(queue.get(i).getId() !=1){
                    if(queue.get(i).getState() == State.Ready){
                        process = queue.get(i);
                        break;
                    }
                }
            }
        }
        for (var el:queue) {
            synchronized (queue){
                if(el.getId()!=1){
                    if(el.getPriority() < process.getPriority() && el.getState() == State.Ready){
                        process = el;
                    }
                }
            }
        }
        return process;
    }

    public synchronized static void changeState(int id,State newState){
        for (Process process: queue) {
            synchronized (queue){
                if(process.getId() == id){
                    process.setState(newState);
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        while (!shutDown){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            addToFinishedQueue();
            for (Process process: queue) {
                synchronized (queue){
                    if(process.getState() == State.Ready){
                        if(ClockGenerator.getTime() - process.getTimeIn() > Configuration.cpuSleapField % 10 &&  process.getPriority() > 2){
                            process.setPriority(Math.round(process.getPriority()/2));
                        }
                    }
                }
            }
        }
    }

    private void addToFinishedQueue(){
        for (int i = 0 ;i<queue.size();i++) {
            synchronized (queue){
                if(queue.get(i).getState() == State.Finished){
                    finishedQueue.add(queue.get(i));
                    queue.remove(i);
                }
            }
        }


    }

    public static void setShutDown(boolean value){
        shutDown = value;
    }

    public String toString(String whatsQueue) {
        String result = whatsQueue + ":\n{\n";

        for (int i =0;i<queue.size();i++){
            result += queue.get(i).toString() + "\n";
        }
        result += "}\n";
        return result;
    }

    public ArrayList<Process> getQueue(){
        return queue;
    }

    public ArrayList<Process> getFinishedQueue(){
        return finishedQueue;
    }
}
