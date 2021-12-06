package com.example.kursovayaGui.company.classes;

import java.util.ArrayList;
import java.util.Queue;

public class Scheduler implements Runnable {
    ProcessQueue queue;
    RejectQueue rejectQueue;
    CPU cpu;
    MemoryScheduler memoryScheduler;
    ClockGenerator clockGenerator;
    static volatile boolean shutDown;

    public Scheduler(final int cpuCoresNumber,int memoryVolume) {
        new Thread(new ClockGenerator()).start();
        //запуск в отдельном потоке счетчика,отсчитывает секунды со старта программы
        this.queue = new ProcessQueue();
        this.rejectQueue = new RejectQueue();
        this.memoryScheduler = new MemoryScheduler();
        this.shutDown = false;
        CPU.cores = new Core[cpuCoresNumber];
        CPU.initial();
        Configuration.memoryVolume = memoryVolume;
        initial();
    }

    private void initial(){
        MemoryScheduler.initial();
        queue.add("OS",1);

    }

    public void generateProcess(final int N){
        //создание  N-ого колличества процессов
        for (int i =0 ;i<N;i++){
            Process process = new Process();
            MemoryBlock mb = MemoryScheduler.findFreeBlock(process.getMemory());
            //проверяет есть ли свободный блок памяти для процесса
            if(mb.getStart() == 0 ){
                //если нету - добавляет в очередь отказов
                rejectQueue.add(process);
            }else{
                //если есть в очередь на обработку процессором
                queue.add(process,mb);
            }
        }
    }

    @Override
    public void run()  {
        try {
            ProcessQueue.setShutDown(false);
            ClockGenerator.setShutDown(false);
            new Thread(queue).start();//запуск метод run(класс Queue) в отдельном потоке
            new Thread(clockGenerator).start();
            giveCPUWork();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void giveCPUWork() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shutDown){
                    generateProcess(CPU.getCores().length+2);
                    try {
                        Thread.sleep(((Configuration.cpuSleapField*Configuration.maxProcessWorked)*2)/Configuration.coreQuantity/2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                }

        });
        thread.start();
        while (!shutDown){
            int coresFreeNumber = 0;
            for (Core core:cpu.getCores()) {
                if(core.isFree()==true){
                    coresFreeNumber++;
                }
            }
            if (shutDown){
                break;
            }
            for(int i=0;i<coresFreeNumber;i++){
                Process process = queue.getHighPriorityProcess();
                ProcessQueue.changeState(process.getId(),State.Running);
                CPU cpu = new CPU(process);
                new Thread(cpu).start();
            }
            if (shutDown){
                break;
            }
            if(rejectQueue.isNeadDefragmentation()){
                MemoryScheduler.defragmentationStart();
            }
            Thread.sleep(1000);
        }
        ClockGenerator.setShutDown(shutDown);
        ProcessQueue.setShutDown(shutDown);
    }

    public static void setShutDown(boolean value){
        shutDown = value;
    }

    @Override
    public String toString() {
        String result ="Cores:[ ";
        for (Core core: cpu.getCores()){
            result+=core.getProcessID()+"\t"+core.isFree()+"\t";
        }
        result+="]\n";
        result+=queue.toString("queue");
        result+=memoryScheduler.toString();
        return result;
    }

    public ArrayList returnStatistic(){
        ArrayList statistic = new ArrayList();
        statistic.add(queue.getQueue());
        statistic.add(queue.getFinishedQueue());
        statistic.add(rejectQueue.getRejectQueue());
        statistic.add(CPU.getCores());
        statistic.add(MemoryScheduler.getMemoryBlocks());
        return statistic;
    }

}

