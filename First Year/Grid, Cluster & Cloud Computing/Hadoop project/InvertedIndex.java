 import java.io.*;
 import java.util.*;

 import javax.naming.Context;

 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.lib.input.*;
 import org.apache.hadoop.mapreduce.lib.output.*;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.io.Text;

 public class InvertedIndex {

     public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
         private Set<String> stopWords = new HashSet<>();
         private Text word = new Text();
         private Text info = new Text();
         private long lineNumber = 0;

         @Override
         protected void setup(Context context) throws IOException {
             Configuration conf = context.getConfiguration();
             Path stopWordsPath = new Path(conf.get("stopwords.path"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(stopWordsPath.getFileSystem(conf).open(stopWordsPath)));
             String line;
             while ((line = reader.readLine()) != null) {
                 stopWords.add(line.trim().toLowerCase());
             }
             reader.close();
         }

         @Override
         public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
             String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
             lineNumber++;
             String line = value.toString().toLowerCase();
             String[] tokens = line.split("\\W+");

             for (String token : tokens) {
                 if (!token.isEmpty() && !stopWords.contains(token)) {
                     word.set(token);
                     info.set(fileName + "@" + lineNumber);
                     context.write(word, info);
                 }
             }
         }
     }

     public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
         public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
             Map<String, Set<String>> fileLines = new HashMap<>();

             for (Text val : values) {
                 String[] parts = val.toString().split("@");
                 String file = parts[0];
                 String line = parts[1];

                 fileLines.putIfAbsent(file, new HashSet<>());
                 fileLines.get(file).add(line);
             }

             StringBuilder output = new StringBuilder();
             for (Map.Entry<String, Set<String>> entry : fileLines.entrySet()) {
                 output.append("(").append(entry.getKey());
                 for (String ln : entry.getValue()) {
                     output.append(", line").append(ln);
                 }
                 output.append(") ");
             }

             context.write(key, new Text(output.toString().trim()));
         }
     }

     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
         conf.set("stopwords.path", args[2]);
         Job job = Job.getInstance(conf, "inverted index");

         job.setJarByClass(InvertedIndex.class);
         job.setMapperClass(InvertedIndexMapper.class);
         job.setReducerClass(InvertedIndexReducer.class);

         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);

         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));

         System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
 }
