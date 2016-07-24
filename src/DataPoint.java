/**
 * Created by qxs on 6/30/2016.
 */
public class DataPoint {
    String dataPointName; // 样本点名
    Cluster cluster; // 样本点所属类簇
    private double[] dimension; // 样本点的维度
    private double energy;


    public DataPoint(){

    }

    public DataPoint(double[] dimension,double energy,String dataPointName){
        this.dataPointName=dataPointName;
        this.dimension=dimension;
        this.energy=energy;
    }

    public double[] getDimension() {
        return dimension;
    }
    public double getEnergy(){
        return energy;
    }
    public void consumeEnergy(double energy){this.energy-=energy;}

    public void setDimension(double[] dimension) {
        this.dimension = dimension;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getDataPointName() {
        return dataPointName;
    }

    public void setDataPointName(String dataPointName) {
        this.dataPointName = dataPointName;
    }
}
