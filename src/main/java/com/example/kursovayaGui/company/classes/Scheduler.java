package com.example.kursovayaGui.company.classes;

import java.util.ArrayList;

public class Scheduler implements Runnable {
    ProcessQueue queue;
    RejectQueue rejectQueue;
    CPU cpu;
    MemoryScheduler memoryScheduler;
    volatile boolean shutDown;

    public Scheduler(final int cpuCoresNumber,int memoryVolume) {
        new Thread(new ClockGenerator()).start();
        this.queue = new ProcessQueue();
        new Thread(queue).start();
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
        generateProcess(7);

    }

    public void generateProcess(final int N){
        for (int i =0 ;i<N;i++){
            Process process = new Process();
            MemoryBlock mb = MemoryScheduler.findFreeBlock(process.getMemory());
            if(mb.getStart() == 0 ){
                rejectQueue.add(process);
            }else{
                queue.add(process,mb);
            }
        }
    }

    @Override
    public void run()  {
        try {
            giveCPUWork();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void giveCPUWork() throws InterruptedException {
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
            Thread.sleep(20000);
            if (shutDown){
                break;
            }
            generateProcess(CPU.getCores().length);
            if(rejectQueue.isNeadDefragmentation()){
                MemoryScheduler.defragmentationStart();
            }
        }
        for (Core core:CPU.getCores()) {
            core.setProcessID(0);
            core.isFree(true);
        }
        ClockGenerator.shutDown();
        ProcessQueue.shutDown();
    }

    public void shutDown(){
        this.shutDown = true;
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

