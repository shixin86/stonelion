package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.io.Text;


public class Main extends Configured implements Tool{
    public static void main(String[] args) throws Exception {
        Job job = new Job();
        
        job.setJarByClass(Main.class);
        
        job.setMapperClass(GetMutualFriendsMapper.class);
        job.setReducerClass(GetMutualFriendsReducer.class);
        
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        System.exit(job.waitForCompletion(true)?0:1);
    }

    public int run(String[] arg0) throws Exception {
        return 0;
    }
}
