package webHandler;

public class StatusMetrics {
    public double cpu;
    public double ram;
    public double disk;

    public StatusMetrics(double cpu, double ram, double disk) {
        this.cpu = cpu;
        this.ram = ram;
        this.disk = disk;
    }
}
