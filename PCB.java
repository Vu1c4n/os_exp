public class PCB {
    private int pid;
    private int memoryAddress;
    public int burstTime;
    public int runtime = 0;
    public int memSize;
    public int ioStartTime;
    public int ioEndTime;
    public int ioRuntime = 0;
    public String state;

    public PCB(int pid, int burstTime, int memSize, int ioStartTime, int ioEndTime) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.memSize = memSize;
        this.ioStartTime = ioStartTime;
        this.ioEndTime = ioEndTime;
        this.state = "Ready"; // 初始状态为就绪状态
        this.memoryAddress = -1; // 内存地址初始化为-1
    }

    public int getPid() {
        return pid;
    }

    public int getMemoryAddress() {
        return memoryAddress;
    }

    public void setMemoryAddress(int memoryAddress) {
        this.memoryAddress = memoryAddress;
    }
    
    @Override
    public String toString() {
        return 
            "PID: " + pid +
            ", State: " + state + 
            ", Memory Size: " + memSize + 
            " bytes, Memory Address: " + memoryAddress;
    }
}
