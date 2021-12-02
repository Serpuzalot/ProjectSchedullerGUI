package com.example.kursovayaGui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.example.kursovayaGui.company.classes.*;
import com.example.kursovayaGui.company.classes.Process;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

public class Controller implements Initializable{
    public Scheduler scheduler ;
    private static volatile boolean shutDown = false;
    private final int ROW_HEIGHT = 25;
    private int coresNumber;
    private int memoryLimit;

    @FXML
    GridPane queue;
    @FXML
    GridPane RejectQueue;
    @FXML
    GridPane MemoryBlock;
    @FXML
    GridPane cpu;
    @FXML
    GridPane finishedQueue;
    @FXML
    TextField coresNum;
    @FXML
    TextField memoryVolume;
    @FXML
    TextArea EventField;

    @FXML
    private void handleStartButtonAction(ActionEvent event){
        if(checkEnterData()){
            this.shutDown = false;
            new Thread(scheduler = new Scheduler(coresNumber,memoryLimit)).start();
            printStatistic();
            defragmentationWatcher();
        }else{
            errorEnterDataPrint();
        }

    }

    private void errorEnterDataPrint(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(()->{
                    EventField.setFont(Font.font(25));
                    EventField.setText("Incorrect data entry!");
                });
            }
        });
        thread.start();
    }

    private boolean checkEnterData(){
        if(coresNumber > 0 && memoryLimit > 0 ){
            return true;
        }
        return false;
    }

    boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void handleStopButtonAction(ActionEvent event){
        scheduler.shutDown();
        deleateAllContent();
        this.shutDown = true;

    }

    private void printStatistic() {
        Thread thread = new Thread(() -> {
            while (!shutDown) {
                Platform.runLater(() -> {
                    cpu.getRowConstraints().remove(1,cpu.getRowCount());
                    cpu.getChildren().remove(1,cpu.getChildren().size());
                    MemoryBlock.getRowConstraints().remove(1,MemoryBlock.getRowCount());
                    MemoryBlock.getChildren().remove(1,MemoryBlock.getChildren().size());
                    queue.getRowConstraints().remove(1,queue.getRowCount());
                    queue.getChildren().remove(1,queue.getChildren().size());
                });
                int columnIndex = 0;
                ArrayList statistic = scheduler.returnStatistic();
                ArrayList<Process> queueList = (ArrayList<Process>) statistic.get(0);
                Platform.runLater(() -> {
                    for (int i = queue.getRowCount() - 1; i < queueList.size(); i++) {
                        RowConstraints row = new RowConstraints(ROW_HEIGHT);
                        queue.getRowConstraints().add(row);
                        Label label = new Label(queueList.get(i).toString());
                        label.setFont(Font.font(11));
                        label.setAlignment(Pos.CENTER_LEFT);
                        queue.add(label, columnIndex, queue.getRowCount() - 1);
                    }
                });
                ArrayList<Process> finishedQueueList = (ArrayList<Process>) statistic.get(1);
                Platform.runLater(() -> {
                    for (int i = finishedQueue.getRowCount() - 1; i < finishedQueueList.size(); i++) {
                        RowConstraints row = new RowConstraints(ROW_HEIGHT);
                        finishedQueue.getRowConstraints().add(row);
                        Label label = new Label(finishedQueueList.get(i).toString());
                        label.setFont(Font.font(11));
                        label.setAlignment(Pos.CENTER_RIGHT);
                        finishedQueue.add(label, columnIndex, finishedQueue.getRowCount() - 1);
                    }
                });

                ArrayList<Process> rejectQueueList = (ArrayList<Process>) statistic.get(2);
                Platform.runLater(() -> {
                    for (int i = RejectQueue.getRowCount() - 1; i < rejectQueueList.size(); i++) {
                        RowConstraints row = new RowConstraints(ROW_HEIGHT);
                        RejectQueue.getRowConstraints().add(row);
                        Label label = new Label(rejectQueueList.get(i).toString());
                        label.setFont(Font.font(11));
                        label.setAlignment(Pos.CENTER_LEFT);
                        RejectQueue.add(label, columnIndex, RejectQueue.getRowCount() - 1);
                    }
                });

                Core[] cores = (Core[]) statistic.get(3);

                Platform.runLater(() -> {

                    for (int i = cpu.getRowCount() - 1; i < cores.length; i++) {
                        RowConstraints row = new RowConstraints(ROW_HEIGHT);
                        cpu.getRowConstraints().add(row);
                        Label label = new Label(cores[i].toString());
                        label.setFont(Font.font(11));
                        label.setAlignment(Pos.CENTER);
                        cpu.add(label, columnIndex, cpu.getRowCount()-1);

                    }
                });

                ArrayList<MemoryBlock> memoryBloksList = (ArrayList<MemoryBlock>) statistic.get(4);

                Platform.runLater(() -> {

                    for (int i = MemoryBlock.getRowCount() - 1; i < memoryBloksList.size(); i++) {
                        RowConstraints row = new RowConstraints(ROW_HEIGHT);
                        MemoryBlock.getRowConstraints().add(row);
                        Label label = new Label(memoryBloksList.get(i).toString());
                        label.setFont(Font.font(11));
                        label.setAlignment(Pos.CENTER);
                        MemoryBlock.add(label, columnIndex, MemoryBlock.getRowCount() - 1);

                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private void defragmentationWatcher(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shutDown){

                    if(MemoryScheduler.getLastDefragmentationStart()!=null){
                        Platform.runLater(()->{
                            EventField.setText("");
                            EventField.setText(MemoryScheduler.getLastDefragmentationStart()+"\n"+MemoryScheduler.getLastDefragmentationEnd());

                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }

    private void deleateAllContent(){
        Platform.runLater(()->{
            cpu.getChildren().remove(1, cpu.getChildren().size());
            cpu.getRowConstraints().remove(1, cpu.getRowCount());
            MemoryBlock.getChildren().remove(1, MemoryBlock.getChildren().size());
            MemoryBlock.getRowConstraints().remove(1, MemoryBlock.getRowCount());
            queue.getChildren().remove(1, queue.getChildren().size());
            queue.getRowConstraints().remove(1, queue.getRowCount());
            RejectQueue.getChildren().remove(0,RejectQueue.getChildren().size());
            RejectQueue.getRowConstraints().remove(0,RejectQueue.getRowCount());
            finishedQueue.getChildren().remove(0,finishedQueue.getChildren().size());
            finishedQueue.getRowConstraints().remove(0,finishedQueue.getRowCount());
            EventField.setText("");
        });

    }

    @FXML
    private void initialDataEnter(ActionEvent event){
        coresNumber =0;
        memoryLimit =0;
        if(tryParseInt(coresNum.getText())){
            coresNumber = Integer.parseInt(coresNum.getText());
        }
        if(tryParseInt(memoryVolume.getText())){
            memoryLimit= Integer.parseInt(memoryVolume.getText());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
      /* while (!shutDown){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int columnIndex = 0;
            ArrayList statistic = scheduler.returnStatistic();
            ArrayList<Process> queueList = (ArrayList<Process>) statistic.get(0);
            for (Process process:queueList) {
                RowConstraints row = new RowConstraints(ROW_HEIGHT);
                queue.getRowConstraints().add(row);
                Label label = new Label(process.toString());
                queue.add(label,columnIndex,queue.getRowCount()-1);
            }
            ArrayList<Process> finishedQueueList = (ArrayList<Process>) statistic.get(1);
            for (Process process:finishedQueueList) {
                RowConstraints row = new RowConstraints(ROW_HEIGHT);
                finishedQueue.getRowConstraints().add(row);
                Label label = new Label(process.toString());
                finishedQueue.add(label,columnIndex,queue.getRowCount()-1);
            }
            ArrayList<Process> rejectQueueList = (ArrayList<Process>) statistic.get(2);
            for (Process process:rejectQueueList) {
                RowConstraints row = new RowConstraints(ROW_HEIGHT);
                RejectQueue.getRowConstraints().add(row);
                Label label = new Label(process.toString());
                RejectQueue.add(label,columnIndex,queue.getRowCount()-1);
            }
            ArrayList<Core> CPUList = (ArrayList<Core>) statistic.get(3);
            for (Core core : CPUList){
                RowConstraints row = new RowConstraints(ROW_HEIGHT);
                cpu.getRowConstraints().add(row);
                Label label = new Label(core.toString());
                cpu.add(label,columnIndex,cpu.getRowCount()-1);
            }
            ArrayList<MemoryBlock> memoryBloksList = (ArrayList<MemoryBlock>) statistic.get(4);
            for(MemoryBlock mb : memoryBloksList){
                RowConstraints row = new RowConstraints(ROW_HEIGHT);
                MemoryBlock.getRowConstraints().add(row);
                Label label = new Label(mb.toString());
                MemoryBlock.add(label,columnIndex,MemoryBlock.getRowCount()-1);
            }

        }
        */
    }

}