package com.example.kursovayaGui.company.classes;

public class Process {

    private int id;
    private String name;
    private int priority;
    private int memory;
    private int timeIn;
    private int burstTime;
    State state;

    public Process(){
        this.memory = Utils.getRandomInteger(10,Configuration.memoryVolume/3);
        this.priority = Utils.getRandomInteger(1,Configuration.maxPriority);
        this.timeIn = ClockGenerator.getTime();
        this.burstTime = Utils.getRandomInteger(1,Configuration.maxProcessWorked);
        this.state = State.Ready;
    }

    public Process(int id,int memory,int priority,int timeIn,int burstTime,State state){
        this.id = id;
        this.name = "P"+this.id;
        this.memory = memory;
        this.priority = priority;
        this.timeIn = timeIn;
        this.burstTime = burstTime;
        this.state = state;
    }

    public Process(String name,int id){
        this.id = id;
        this.name = "OS";
        this.memory = Configuration.osMemoryVolume;
        this.priority = 0;
        this.timeIn = ClockGenerator.getTime();
        this.burstTime = 0;
        this.state = State.Ready;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getMemory() {
        return memory;
    }

    public int getTimeIn() {
        return timeIn;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return
                " { name[" + name + "] " +
                ", priority[" + priority+"] " +
                ", memory[" + memory+"] " +
                ", timeIn[" + timeIn +"] "+
                ", burstTime[" + burstTime +"] "+
                ", state[" + state + "]"+
                '}';
    }
}
