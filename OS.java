import java.util.*;
import java.util.concurrent.*;

public class OS {
    private Processor processor = new Processor();
    private Shell shell = new Shell();

    private ConcurrentLinkedQueue<PCB> readyQue; // 就绪队列
    private CopyOnWriteArraySet<PCB> blockQue; // 阻塞队列
    private PCB runningProcess; // 运行中的进程
    private static final int TIME_SLICE = 1; // 时间片(unit: ms)
    private List<PCB> pTab;
    private int memSize;
    private int memUse;
    private int pidCounter;

    public OS() {
        readyQue = new ConcurrentLinkedQueue<>();
        blockQue = new CopyOnWriteArraySet<>();
        runningProcess = null;
        pTab = new ArrayList<>();
        memSize = 1024 * 1024; // 1 MB内存大小
        memUse = 0; // 当前内存使用量
        pidCounter = 1; // PID计数器
    }

    public void createProcess(int burstTime, int memSize, int ioStartTime, int ioEndTime) {
        // 检查内存是否足够
        if (memSize > this.memSize - memUse) {
            System.out.println("Not enough memory to create process.");
            return;
        }

        // 创建新的进程
        PCB newProcess = new PCB(pidCounter, burstTime, memSize, ioStartTime, ioEndTime);
        // 分配内存地址
        newProcess.setMemoryAddress(memUse);
        // 更新内存使用量
        memUse += memSize;

        // 将新进程加入进程表
        pTab.add(newProcess);
        readyQue.add(newProcess);
        System.out.println("Process created with PID " + pidCounter);

        // 更新PID计数器
        pidCounter++;
    }

    public void killProcess(int pid) {
        // 查找要终止的进程
        PCB pToKill = null;
        for (PCB process : pTab) {
            if (process.getPid() == pid) {
                pToKill = process;
                break;
            }
        }

        if (pToKill == null) {
            System.out.println("No process found with PID " + pid + ".");
            return;
        }

        // 1 如果该进程在运行中，停止运行
        if (runningProcess != null && runningProcess.getPid() == pid) {
            runningProcess = null;
        }

        // 2 如果该进程在就绪队列中，移除
        readyQue.remove(pToKill);
        // 3 如果该进程在阻塞队列中，移除
        blockQue.remove(pToKill);
        
        // 4 从进程表中移除
        pTab.remove(pToKill);

        // 5.释放内存
        memUse -= pToKill.memSize;
        System.out.println("Process with PID " + pid + " terminated.");
    }

    public void showProcesses() {
        System.out.println("All processes:");
        System.out.println("-------- TOP --------");
        for (PCB process : pTab) {
            System.out.println(process);
        }
        System.out.println("-------- BOTTOM --------");
    }

    public void showmemUse() {
        System.out.println("Memory usage: " + memUse + " bytes out of " + memSize + " bytes");
    }

    public void run() {
        Thread shellThread = new Thread(shell);
        Thread processorThread = new Thread(processor);
        shellThread.start();
        processorThread.start();
    }

    /**
     * 线程-命令行
     */
    class Shell implements Runnable{
        @Override
        public void run(){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the OS Simulator! Type to use the shell!");
            while (true) {
                String command = scanner.nextLine().trim();
                if(command.equals("")){
                    continue;
                }
                else if (command.startsWith("createproc")) {
                    String[] parts = command.split("\\s+");
                    if (parts.length != 5) {
                        System.out.println(
                        "Invalid createproc command format. Expected: createproc burstTime memSize ioStartTime ioEndTime");
                        continue;
                    }
                    int burstTime = Integer.parseInt(parts[1]);
                    int memSize = Integer.parseInt(parts[2]);
                    int ioStartTime = Integer.parseInt(parts[3]);
                    int ioEndTime = Integer.parseInt(parts[4]);
                    createProcess(burstTime, memSize, ioStartTime, ioEndTime);
                }
                else if (command.startsWith("killproc")) {
                    String[] parts = command.split("\\s+");
                    if (parts.length != 2) {
                        System.out.println("Invalid killproc command format. Expected: killproc PID");
                        continue;
                    }
                    int pid = Integer.parseInt(parts[1]);
                    killProcess(pid);
                }
                else if (command.equalsIgnoreCase("psproc")) {
                    showProcesses();
                }
                else if (command.equalsIgnoreCase("mem")) {
                    showmemUse();
                }
                else {
                    System.out.println("Invalid command. Available commands: createproc, killproc, psproc, mem");
                }
            }
        }
    }
    
    /**
     * 线程-处理机
     */
    class Processor implements Runnable{
        public void getReady(PCB p){
            readyQue.add(p);
            p.state = "Ready";
        }
        public void getBlock(PCB p){
            blockQue.add(p);
            p.state = "Block";
        }
        public void getRunning(PCB p){
            runningProcess = p;
            p.state = "Running";
        }
        @Override
        public void run(){
            while(true){
                // 每次固定运行一个时间片
                try{
                    Thread.sleep(TIME_SLICE);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }

                // 从就绪列表中选取任务执行
                if(!readyQue.isEmpty()) {
                    PCB cur = readyQue.poll();
                    // 执行任务
                    getRunning(cur);
                    cur.runtime += TIME_SLICE;
                    if(cur.runtime >= cur.burstTime){ // 任务已完成
                        // 移除进程表
                        pTab.remove(cur);
                        // 释放内存
                        memUse -= cur.memSize;
                    }
                    else if(cur.ioStartTime!=-1 && cur.runtime==cur.ioStartTime){ // 发生IO阻塞
                        getBlock(cur);
                    }
                    else { // 任务未完成
                        getReady(cur);
                    }
                }

                // 遍历阻塞列表, 更新已进行的IO等待时间
                for(PCB p : blockQue){
                    p.runtime += TIME_SLICE;
                    if(p.runtime==p.ioEndTime){ // 如果io已完成
                        blockQue.remove(p);
                        getReady(p);
                    }
                }
                runningProcess = null;
            }
        }
    }
}