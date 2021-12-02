package com.example.kursovayaGui.company.interfaces;

import com.example.kursovayaGui.company.classes.MemoryBlock;
import com.example.kursovayaGui.company.classes.Process;

public interface Queue {

    public int getLastProcessId();

    public void add(Process process);

    public void  add(Process process, MemoryBlock mb);

    public void add(String name,int id);

    public Process getHighPriorityProcess();

}
