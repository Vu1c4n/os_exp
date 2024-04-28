import java.util.*;

public class OS {
    private Queue<PCB> readyQueue; // 就绪队列
    private Queue<PCB> blockedQueue; // 阻塞队列
    private PCB runningProcess; // 运行中的进程
    private int currentTime = 0; // 当前时间，用于模拟时间片和I/O操作
    private static final int TIME_SLICE = 5; // 时间片
    private List<PCB> processTable;
    private int memorySize;
    private int memoryUsage;
    private int pidCounter;

    public OS() {
        readyQueue = new LinkedList<>();
        blockedQueue = new LinkedList<>();
        runningProcess = null;
        processTable = new ArrayList<>();
        memorySize = 1024 * 1024; // 1 MB内存大小
        memoryUsage = 0; // 当前内存使用量
        pidCounter = 1; // PID计数器
    }

    public void createProcess(int burstTime, int memorySize, int ioStartTime, int ioEndTime) {
        // 检查内存是否足够
        if (memorySize > this.memorySize - memoryUsage) {
            System.out.println("Not enough memory to create process.");
            return;
        }

        // 创建新的进程
        PCB newProcess = new PCB(pidCounter, burstTime, memorySize, ioStartTime, ioEndTime);
        // 分配内存地址
        newProcess.setMemoryAddress(memoryUsage);
        // 更新内存使用量
        memoryUsage += memorySize;

        // 将新进程加入进程表
        processTable.add(newProcess);
        System.out.println("Process created with PID " + pidCounter);

        // 更新PID计数器
        pidCounter++;
    }

    public void killProcess(int pid) {
        // 查找要终止的进程
        PCB processToKill = null;
        for (PCB process : processTable) {
            if (process.getPid() == pid) {
                processToKill = process;
                break;
            }
        }

        if (processToKill == null) {
            System.out.println("No process found with PID " + pid + ".");
            return;
        }

        // 1 如果该进程在运行中，停止运行
        if (runningProcess != null && runningProcess.getPid() == pid) {
            runningProcess = null;
        }

        // 2 如果该进程在就绪队列中，移除
        readyQueue.remove(processToKill);
        // 3 如果该进程在阻塞队列中，移除
        blockedQueue.remove(processToKill);
        
        // 终止进程
        processTable.remove(processToKill);
        // 释放内存
        memoryUsage -= processToKill.getMemorySize();
        System.out.println("Process with PID " + pid + " terminated.");
    }

    public void showProcesses() {
        System.out.println("All processes:");
        System.out.println("-------- TOP --------");
        for (PCB process : processTable) {
            System.out.println(process);
        }
        System.out.println("-------- BOTTOM --------");
    }

    public void showMemoryUsage() {
        System.out.println("Memory usage: " + memoryUsage + " bytes out of " + memorySize + " bytes");
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the OS Simulator!");
        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the simulator.");
                break;
            } else if (command.startsWith("createproc")) {
                String[] parts = command.split("\\s+");
                if (parts.length != 5) {
                    System.out.println(
                    "Invalid createproc command format. Expected: createproc burstTime memorySize ioStartTime ioEndTime");
                    continue;
                }
                int burstTime = Integer.parseInt(parts[1]);
                int memorySize = Integer.parseInt(parts[2]);
                int ioStartTime = Integer.parseInt(parts[3]);
                int ioEndTime = Integer.parseInt(parts[4]);
                createProcess(burstTime, memorySize, ioStartTime, ioEndTime);
            } else if (command.startsWith("killproc")) {
                String[] parts = command.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Invalid killproc command format. Expected: killproc PID");
                    continue;
                }
                int pid = Integer.parseInt(parts[1]);
                killProcess(pid);
            } else if (command.equalsIgnoreCase("psproc")) {
                showProcesses();
            } else if (command.equalsIgnoreCase("mem")) {
                showMemoryUsage();
            } else {
                System.out.println("Invalid command. Available commands: createproc, killproc, psproc, mem, exit");
            }
        }
        scanner.close();
    }
}