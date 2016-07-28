/**
 * Created by qxs on 6/30/2016.
 */
import com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.util.*;

public class ClusterAnalysis {
    public DataPoint baseStation;
    static double Efs=10*0.00000001;//自由空间能量
    static double Emp=13*0.000000000001;//衰减空间能量
    static double d0=Math.sqrt(Efs/Emp);
    static int L=4000;
    static double ETX=50*0.00001;
    static double ERX=50*0.00001;
    static double EDA=5*0.00001;
    ClusterAnalysis(DataPoint baseStation){
        this.baseStation=baseStation;
    }
    public List<Cluster> startAnalysis(List<DataPoint> dataPoints,int ClusterNum){
        List<Cluster> finalClusters=new ArrayList<Cluster>();

        List<Cluster> originalClusters=initialCluster(dataPoints);
        finalClusters=originalClusters;
        while(finalClusters.size()>ClusterNum){
            double min=Double.MAX_VALUE;
            int mergeIndexA=0;
            int mergeIndexB=0;
            for(int i=0;i<finalClusters.size();i++){
                for(int j=0;j<finalClusters.size();j++){
                    if(i!=j){
                        Cluster clusterA=finalClusters.get(i);
                        Cluster clusterB=finalClusters.get(j);

                        List<DataPoint> dataPointsA=clusterA.getDataPoints();
                        List<DataPoint> dataPointsB=clusterB.getDataPoints();
                        double tempDis=0;
                        double tempEnergy=0;
                        for(int m=0;m<dataPointsA.size();m++){
                            for(int n=0;n<dataPointsB.size();n++){

                                tempDis+=getDistance(dataPointsA.get(m),dataPointsB.get(n));
                                tempEnergy+=dataPointsA.get(m).getEnergy()+dataPointsB.get(n).getEnergy();
                               // if(tempDis<min){
                                 //   min=tempDis;
                                   // mergeIndexA=i;
                                   // mergeIndexB=j;
                              //  }
                            }

                        }
                        tempEnergy/=dataPointsA.size()*dataPointsB.size();
                        tempDis/=dataPointsA.size()*dataPointsB.size();
                        double distanceA=clusterA.distanceFactor(dataPointsA,baseStation);
                        double distanceB=clusterB.distanceFactor(dataPointsB,baseStation);
                        tempDis+=((distanceA+distanceB)/2/tempEnergy)*5000;

                        if (tempDis<min){
                            min=tempDis;
                            mergeIndexA=i;
                            mergeIndexB=j;
                        }
                    }
                } //end for j
            }// end for i
            //合并cluster[mergeIndexA]和cluster[mergeIndexB]
            finalClusters=mergeCluster(finalClusters,mergeIndexA,mergeIndexB);
        }//end while

        return finalClusters;
    }
    private List<Cluster> mergeCluster(List<Cluster> clusters,int mergeIndexA,int mergeIndexB){
        if (mergeIndexA != mergeIndexB) {
            // 将cluster[mergeIndexB]中的DataPoint加入到 cluster[mergeIndexA]
            Cluster clusterA = clusters.get(mergeIndexA);
            Cluster clusterB = clusters.get(mergeIndexB);

            List<DataPoint> dpA = clusterA.getDataPoints();
            List<DataPoint> dpB = clusterB.getDataPoints();

            for (DataPoint dp : dpB) {
                DataPoint tempDp = new DataPoint();
                tempDp.setDataPointName(dp.getDataPointName());
                tempDp.setDimension(dp.getDimension());
                tempDp.setEnergy(dp.getEnergy());
                tempDp.setCluster(clusterA);
                dpA.add(tempDp);
            }

            clusterA.setDataPoints(dpA);

            // List<Cluster> clusters中移除cluster[mergeIndexB]
            clusters.remove(mergeIndexB);
        }

        return clusters;
    }

    // 初始化类簇
    private List<Cluster> initialCluster(List<DataPoint> dataPoints){
        List<Cluster> originalClusters=new ArrayList<Cluster>();
        for(int i=0;i<dataPoints.size();i++){
            DataPoint tempDataPoint=dataPoints.get(i);
            List<DataPoint> tempDataPoints=new ArrayList<DataPoint>();
            tempDataPoints.add(tempDataPoint);

            Cluster tempCluster=new Cluster();
            tempCluster.setClusterName("Cluster "+String.valueOf(i));
            tempCluster.setDataPoints(tempDataPoints);

            tempDataPoint.setCluster(tempCluster);
            originalClusters.add(tempCluster);
        }

        return originalClusters;
    }

    //计算两个样本点之间的欧几里得距离
    static double getDistance(DataPoint dpA,DataPoint dpB){
        double distance=0;
        double[] dimA = dpA.getDimension();
        double[] dimB = dpB.getDimension();

        if (dimA.length == dimB.length) {
            for (int i = 0; i < dimA.length; i++) {
                double temp=Math.pow((dimA[i]-dimB[i]),2);
                distance=distance+temp;
            }
            distance=Math.pow(distance, 0.5);
        }

        return distance;
    }

    static DataPoint choosehead(Cluster cluster) {
        List<DataPoint> temp = cluster.getDataPoints();
        int len = temp.size();
        int index = 0;
        double max = 0;
        if (temp.size() == 1) return temp.get(0);
        else {
            for (int i = 0; i < len; i++) {
                int distance = 0;
                for (int j = 0; j < len; j++) {
                    if (j != i) {
                        distance += getDistance(temp.get(i), temp.get(j)) * getDistance(temp.get(i), temp.get(j));
                    }
                }
                distance/=temp.size();
                if (max < distance+temp.get(i).getEnergy()) {
                    max = distance +temp.get(i).getEnergy();
                    index = i;
                }

            }
            return temp.get(index);
        }
    }

    static void energychange (Cluster cluster, DataPoint ch, DataPoint bs, int L){  //传输Lbit对于cluster i消耗能量
        List <DataPoint> temp = cluster.getDataPoints();
        double Ech=0;
        int clustersize=temp.size();
//        if ((getDistance(ch,bs))<d0){
//            Ech=L*ERX*(clustersize-1)+L*EDA*clustersize+clustersize*L*(ETX+Efs*Math.pow(getDistance(ch,bs),2));
//            ch.consumeEnergy(Ech);
//        }
//        else {
            Ech=L*ERX*(clustersize-1)+L*EDA*clustersize+clustersize*L*(ETX+Emp*Math.pow(getDistance(ch,bs),4))+5;
            ch.consumeEnergy(Ech);
//        }
        if (temp.size()>1) {
            for (int i = 0; i < clustersize; i++) {
                double consume = L * ETX + L * Efs * getDistance(ch, temp.get(i)) * getDistance(ch, temp.get(i));
                Ech += consume;
                temp.get(i).consumeEnergy(consume);
            }
            Ech -= L * ETX;
            ch.consumeEnergy(-L * ETX);
        }

    }

    static double energyconsume (Cluster cluster, DataPoint ch, DataPoint bs, int L){  //传输Lbit对于cluster i消耗能量
        List <DataPoint> temp = cluster.getDataPoints();
        double Ech=0;
        int clustersize=temp.size();
//        if ((getDistance(ch,bs))<d0){
//            Ech=L*ERX*(clustersize-1)+L*EDA*clustersize+clustersize*L*(ETX+Efs*Math.pow(getDistance(ch,bs),2));
//        }
//        else {
            Ech=L*ERX*(clustersize-1)+L*EDA*clustersize+clustersize*L*(ETX+Emp*Math.pow(getDistance(ch,bs),4))+5;
        //}
        if (temp.size()==1){
            return Ech;
        }
        else {
            for (int i = 0; i < clustersize; i++) {
                double consume = L * ETX + L * Efs * getDistance(ch, temp.get(i)) * getDistance(ch, temp.get(i));
                Ech += consume;
            }
            Ech -= L * ETX;
            return Ech;
        }

    }

    public void energyharvest(List <DataPoint> dataPoints){

    }

    public static boolean flag(List<Cluster> finalclusters){
        boolean temp=true;
        int count=0;
        for (Cluster t:finalclusters){
            List<DataPoint> ps=t.getDataPoints();
            for (DataPoint da:ps){
                if (da.getEnergy()<=0) {count++;}
            }
        }
        if (count>0) {temp=false;}
        return temp;

    }

    public static void main(String[] args){
        ArrayList<DataPoint> dpoints = new ArrayList<DataPoint>();
        double[][] matrix=new double[100][2];
        double energyinit=1000;
//        for (int i=0;i<100;i++){                        //初始化各个节点，初始能量0.05，坐标随机分布
//            matrix[i][0]= Math.random()*100;
//            matrix[i][1]=Math.random()*100;
//            dpoints.add(new DataPoint(matrix[i],energyinit,"point "+i));
//        }
//        Iterator<DataPoint> iterator = dpoints.iterator();
//        while (iterator.hasNext()) {
//            DataPoint point = iterator.next();
//            for(double d: point.getDimension()) {
//                System.out.print(d+" ");
//            }
//            System.out.println();
//        }
        FileInputStream inputStream = null;
        Scanner sc = null;
        int ii = 0;
        try {
            inputStream = new FileInputStream("D:\\program\\java work\\cluster_simulation\\src\\data");
            sc = new Scanner(inputStream);
            while (sc.hasNext()) {
                dpoints.add(new DataPoint(new double[]{sc.nextDouble(), sc.nextDouble()},energyinit,"point "+ii++));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for(DataPoint temp:dpoints){
//            System.out.println(temp.getEnergy()+temp.getDataPointName());
//        }

//        Iterator<DataPoint> iterator = dpoints.iterator();
//        while (iterator.hasNext()) {
//            DataPoint point = iterator.next();
//            for(double d: point.getDimension()) {
//                System.out.print(d+" ");
//            }
//            System.out.println();
//        }

    /*    double[] a={2,3};
        double[] b={2,4};
        double[] c={1,4};
        double[] d={1,3};
        double[] e={2,2};
        double[] f={3,2};

        double[] g={8,7};
        double[] h={8,6};
        double[] i={7,7};
        double[] j={7,6};
        double[] k={8,5};

//      double[] l={100,2};//孤立点


        double[] m={8,20};
        double[] n={8,19};
        double[] o={7,18};
        double[] p={7,17};
        double[] q={8,20};

        dpoints.add(new DataPoint(a,"a"));
        dpoints.add(new DataPoint(b,"b"));
        dpoints.add(new DataPoint(c,"c"));
        dpoints.add(new DataPoint(d,"d"));
        dpoints.add(new DataPoint(e,"e"));
        dpoints.add(new DataPoint(f,"f"));

        dpoints.add(new DataPoint(g,"g"));
        dpoints.add(new DataPoint(h,"h"));
        dpoints.add(new DataPoint(i,"i"));
        dpoints.add(new DataPoint(j,"j"));
        dpoints.add(new DataPoint(k,"k"));

//      dataPoints.add(new DataPoint(l,"l"));

        dpoints.add(new DataPoint(m,"m"));
        dpoints.add(new DataPoint(n,"n"));
        dpoints.add(new DataPoint(o,"o"));
        dpoints.add(new DataPoint(p,"p"));
        dpoints.add(new DataPoint(q,"q"));*/

        int clusterNum; //类簇数
        double [] dimension={50,50};   //中心节点坐标
        double energycompare=Double.MAX_VALUE;
        int clusterfinal=0;
        DataPoint baseStation=new DataPoint(dimension,10000,"baseStation");
        ClusterAnalysis ca=new ClusterAnalysis(baseStation);
        for(clusterNum=40;clusterNum>5;clusterNum--){
            List<Cluster> clusters=ca.startAnalysis(dpoints,clusterNum);
            double temp=0;
            for (Cluster i:clusters){
                DataPoint chi=choosehead(i);
                temp+=energyconsume(i,chi,baseStation,L);
//                System.out.println(energyconsume(i,chi,baseStation,L)+" "+i.getDataPoints().size()+" "+clusterNum);
            }
            if (temp<energycompare) {energycompare=temp;
            clusterfinal=clusterNum;}
          System.out.println(clusterNum+" "+temp+" "+clusterfinal+" "+energycompare);
        }
//        System.out.println(clusterfinal+" sign");
        List<Cluster> finalclusters=ca.startAnalysis(dpoints, clusterfinal);
        int round=0;   //进行的轮数，可以衡量时间
        System.out.println("group"+finalclusters.size());
        for (Cluster cl:finalclusters){
            DataPoint chi=choosehead(cl);
            cl.setClusterhead(chi);
            double [] temp=chi.getDimension();
            for (double i:temp){
//            System.out.print(i+" ");
}
//            System.out.println();

        }
        for(Cluster cl:finalclusters){
            System.out.println("------"+cl.getClusterName()+"------");
            List<DataPoint> tempDps=cl.getDataPoints();
            for(DataPoint tempdp:tempDps){
                System.out.println(tempdp.getDataPointName());
                double []tempdimension=tempdp.getDimension();
                for (double i:tempdimension) {
//                    System.out.print(i+" ");

                }
//                System.out.println();
            }
        }
        while(flag(finalclusters)){
            double Eres=0;
            for (Cluster cl:finalclusters){
                energychange(cl,cl.getClusterhead(),baseStation,L);
                if (cl.getClusterhead().getEnergy()<energyinit/2){
                    cl.setClusterhead(choosehead(cl));
                }
                List<DataPoint> temp=cl.getDataPoints();
                for(DataPoint da: temp){
                    Eres+=da.getEnergy();
                }
            }
            round++;
//            System.out.println("round"+round+" "+"Energytotal"+Eres);
        }

        for (Cluster cl:finalclusters) {

            List<DataPoint> temp = cl.getDataPoints();
            for (DataPoint da : temp) {
//                System.out.println(da.getDataPointName()+" "+da.getEnergy());
                round++;
            }
        }





    }
}
