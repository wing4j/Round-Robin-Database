package org.wing4j.rrd.impl;

import com.google.gson.Gson;
import org.junit.Test;
import org.wing4j.rrd.*;

import java.io.FileOutputStream;

/**
 * Created by liucheng on 2017/7/28.
 */
public class DefaultRoundRobinDatabaseTest {

    @Test
    public void testWrite() throws Exception {
        final RoundRobinDatabase database = DefaultRoundRobinDatabase.init(new RoundRobinConfig());
        final RoundRobinConnection connection = database.open("D:/2.rrd", "mo9.success", "mo9.fail", "mo9.request", "mo9.response", "mo9.other");
        connection.addTrigger(new RoundRobinTrigger() {
            @Override
            public String getName() {
                return "mo9.request";
            }

            @Override
            public boolean accept(int time, long data) {
                return data > 2000 * 200 -1;
            }

            @Override
            public void trigger(int time, long data) {
                System.out.println("触发" + time + ","+ data);
            }
        });
        Thread[] threads = new Thread[200];
        for (int i = 0; i < 200; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 2000; j++) {
                        connection.increase("mo9.success");
                    }
                    for (int j = 0; j < 2000; j++) {
                        connection.increase("mo9.fail");
                    }
                    for (int j = 0; j < 2000; j++) {
                        connection.increase("mo9.request");
                    }
                }
            };
        }
        for (Thread thrad : threads) {
            thrad.start();
        }
        Thread.sleep(10 * 1000);
        connection.persistent();
        connection.close();
    }

    @Test
    public void testRead() throws Exception {
        RoundRobinDatabase database = DefaultRoundRobinDatabase.init(new RoundRobinConfig());
        RoundRobinConnection connection = database.open("D:/2.rrd");
        long[][] data = connection.read(connection.getHeader());
        String json = new Gson().toJson(data[0]);
        connection.persistent(FormatType.CSV, 1);
//        System.out.println(json);
//        json = new Gson().toJson(data[1]);
//        System.out.println(json);
//        json = new Gson().toJson(data[2]);
//        System.out.println(json);
//        RoundRobinView view = connection.slice(5 * 60, "mo9.request");
//        long[] data1 = view.read("mo9.request");
////        System.out.println(data1.length);
//        json = new Gson().toJson(data1);
//        System.out.println(json);
//        Thread.sleep(2 * 1000);

//        connection.freezen();
//        connection.addTrigger(new RoundRobinTrigger() {
//            @Override
//            public String getName() {
//                return "mo9.request";
//            }
//
//            @Override
//            public boolean accept(int time, long data) {
//                return true;
//            }
//
//            @Override
//            public void trigger(int time, long data) {
//                System.out.println("触发");
//            }
//        });
//        connection.merge(view, MergeType.ADD);
//        connection.merge(view, MergeType.ADD);
//        connection.merge(view, MergeType.ADD);
//        connection.merge(view, MergeType.ADD);
////        connection.merge(view, (int)(System.currentTimeMillis() % (24 * 60 * 60)), MergeType.ADD);
////        connection.merge(view, (int)(System.currentTimeMillis() % (24 * 60 * 60)), MergeType.ADD);
////        connection.merge(view, (int)(System.currentTimeMillis() % (24 * 60 * 60)), MergeType.ADD);
//        connection.unfreezen();
//        json = new Gson().toJson(connection.slice(24 * 60 * 60, "mo9.request").read("mo9.request"));
//        System.out.println(json);
//        FileOutputStream fos = new FileOutputStream("D:/123.rrd");
//        fos.close();
        connection.close();
    }
}