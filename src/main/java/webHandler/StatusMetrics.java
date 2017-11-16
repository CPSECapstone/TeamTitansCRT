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

    public double getCpu() {
        return cpu;
    }

    public double getRam() {
        return ram;
    }

    public double getDisk() {
        return disk;
    }
}
