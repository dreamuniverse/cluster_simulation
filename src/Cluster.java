import java.util.ArrayList;
import java.util.List;


public class Cluster {
    private List<DataPoint> dataPoints = new ArrayList<DataPoint>(); // 类簇中的样本点
    private String clusterName;
    private DataPoint clusterhead;
    double distanceFactor(List<DataPoint> dataPoints, DataPoint baseStation){
        int distance=0;
        for (int i=0;i<dataPoints.size();i++){
            distance+=ClusterAnalysis.getDistance(dataPoints.get(i),baseStation);
        }
        return distance/dataPoints.size();
    }
    public DataPoint getClusterhead() {return clusterhead;}
    public void setClusterhead(DataPoint clusterhead){this.clusterhead=clusterhead;}
    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

}