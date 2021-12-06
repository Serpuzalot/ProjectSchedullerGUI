package com.example.kursovayaGui.company.classes;

import java.util.ArrayList;

public class MemoryScheduler {
    private static ArrayList<MemoryBlock> memoryBlocks;
    private static String lastDefragmentationStart;
    private static String lastDefragmentationEnd;
    public static void initial(){
        memoryBlocks = new ArrayList<>();
        //добавление двух изначальных блоков памяти
        //1- под ОС
        //2- под свободное пространство
        MemoryBlock block = new MemoryBlock(0,Configuration.osMemoryVolume,1);
        MemoryBlock freeSpace = new MemoryBlock(Configuration.osMemoryVolume,Configuration.memoryVolume,0);
        memoryBlocks.add(block);
        memoryBlocks.add(freeSpace);
        lastDefragmentationEnd="";
        lastDefragmentationStart="";

    }

    public static boolean findMostSuitableAvailableMemoryBlock(int memoryVolume,int processID,MemoryBlock nededBlock){
        //ищет наиболее подходящий по размеру блок памяти
        //по варианту наиболее подходящий
        //исчет блок который меньше всего,приэтом вмещает в себя процесс
        memoryBlocks.sort(MemoryBlock.byEnd);
        for(var block : memoryBlocks){
            synchronized (memoryBlocks){
                int blockMemory = block.getEnd() - block.getStart();
                int nededBlockMemory = nededBlock.getEnd() - nededBlock.getStart();
                if(nededBlockMemory > blockMemory && blockMemory >= memoryVolume && block.getProcessId() <=0 ){
                    nededBlock = block;
                }
            }
        }
        ocupyMemoryBlock(nededBlock,memoryVolume,processID);
        //занимает выбранный блок памяти процессом
        return true;
    }

    public static MemoryBlock findFreeBlock(int memoryVolume){
        //ищет первый свободнй блок памяти
        synchronized (memoryBlocks){
            for (var mb : memoryBlocks){
                if(mb.getEnd() - mb.getStart() >= memoryVolume && mb.getProcessId() <=0){
                    return mb;
                }

            }
        }
        return new MemoryBlock(0,0,Integer.MIN_VALUE);
    }

    private static void ocupyMemoryBlock(MemoryBlock block , int memoryVolume , int processID){
        //занимает блок памяти
        //проверяет если помещения процесса в блок памяти,в блоке оставется еще место
        //то создается вместо одного блока,два
        for(var el : memoryBlocks){
            if(block.equals(el)){
                if(block.getStart()+memoryVolume == block.getEnd()){
                    block.setProcessId(processID);
                    break;
                }else if(block.getStart() + memoryVolume < block.getEnd()){
                    MemoryBlock mb = new MemoryBlock(block.getStart(), memoryVolume + block.getStart(),processID);
                    memoryBlocks.add(mb);
                    block.setStart(mb.getEnd());
                    break;
                }
            }
        }
    }

    public static void releaseMemoryBlock(int id){
        //освобождение от процесса блока памяти
        synchronized (memoryBlocks) {
            for (var block : memoryBlocks) {
                if (block.getProcessId() == id) {
                    block.setProcessId(-1);
                    break;
                }

            }
        }
    }

    public static void defragmentationStart(){
        lastDefragmentationStart = "Defragmentation start at:"+ClockGenerator.getTime();
        memoryBlocks.sort(MemoryBlock.byEnd);
        ArrayList<MemoryBlock> newMemoryBlocks = new ArrayList<>();
        synchronized (memoryBlocks){
            int blockSiz=0;
            for (int i=0;i<memoryBlocks.size();i++){
                if(i == 0){
                    if(memoryBlocks.get(i).getProcessId() > 0){
                        blockSiz = memoryBlocks.get(i).getEnd() - memoryBlocks.get(i).getStart();
                        newMemoryBlocks.add(new MemoryBlock(0,blockSiz,memoryBlocks.get(i).getProcessId()));

                    }
                    if(memoryBlocks.get(i+1).getProcessId()>0){
                        blockSiz = memoryBlocks.get(i+1).getEnd() - memoryBlocks.get(i+1).getStart();
                        newMemoryBlocks.add(new MemoryBlock(newMemoryBlocks.get(0).getEnd(), newMemoryBlocks.get(0).getEnd() + blockSiz,memoryBlocks.get(i+1).getProcessId()));
                        i++;
                    }
                    continue;
                }
                if(memoryBlocks.get(i).getProcessId() > 0){
                    blockSiz = memoryBlocks.get(i).getEnd() - memoryBlocks.get(i).getStart();
                    newMemoryBlocks.add(new MemoryBlock(newMemoryBlocks.get(newMemoryBlocks.size()-1).getEnd(), newMemoryBlocks.get(newMemoryBlocks.size()-1).getEnd() + blockSiz,memoryBlocks.get(i).getProcessId()));
                }
            }
            newMemoryBlocks.add(new MemoryBlock(newMemoryBlocks.get(newMemoryBlocks.size()-1).getEnd(),Configuration.memoryVolume,0));
            memoryBlocks = newMemoryBlocks;
            memoryBlocks.sort(MemoryBlock.byEnd);
            lastDefragmentationEnd="Defragmentation end at:"+ClockGenerator.getTime();
        }

    }

    public static ArrayList getMemoryBlocks(){
        memoryBlocks.sort(MemoryBlock.byEnd);
        return memoryBlocks;
    }

    public static String getLastDefragmentationStart() {
        return lastDefragmentationStart;
    }

    public static String getLastDefragmentationEnd() {
        return lastDefragmentationEnd;
    }

    @Override
    public String toString(){
        String result = "Memory Blocks:\n{\n";
        for (var el : memoryBlocks){
            result += el.toString() + "\n";
        }
        result += "}\n";
        return result;

    }
}
