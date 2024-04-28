import java.util.*;

public class PCB {
    private int pid;
    private int burstTime;
    private int memorySize;
    private int ioStartTime;
    private int ioEndTime;
    private String state;
    private int memoryAddress;

    public PCB(int pid, int burstTime, int memorySize, int ioStartTime, int ioEndTime) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.memorySize = memorySize;
        this.ioStartTime = ioStartTime;
        this.ioEndTime = ioEndTime;
        this.state = "Ready"; // 初始状态为就绪状态
        this.memoryAddress = -1; // 内存地址初始化为-1
    }

    public int getPid() {
        return pid;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getMemoryAddress() {
        return memoryAddress;
    }

    public void setMemoryAddress(int memoryAddress) {
        this.memoryAddress = memoryAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return 
            "PID: " + pid +
            ", State: " + state + 
            ", Memory Size: " + memorySize + 
            " bytes, Memory Address: " + memoryAddress;
    }
}
