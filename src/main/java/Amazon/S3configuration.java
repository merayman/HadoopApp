package Amazon;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class S3configuration {

    static AWSCredentials credentials = new BasicAWSCredentials(
            "AKIAJ3K2RVESIOJZNW3Q",
            "9P0veFKeNJveVzHxto/D9096uY8xTwxYLwJJl8ve"
    );
    static AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

    public static byte[] getGist(String key, String bucketName) throws IOException{
        byte[] bytes = new byte[1920];
        //System.out.println(key);
        InputStream in = s3client.getObject( bucketName , key).getObjectContent();
        in.read(bytes);
        in.close();
        return bytes;
    }

    public static ArrayList<String> getObjectList( String bucketName ){
        ArrayList<String> resultList = new ArrayList<String>();
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os: objects) {
            String str = os.getKey();
            if ( str.contains("tmp/input/TestCase/output/part"))
                resultList.add(str);
        }
        return resultList;
    }


    public static boolean uploadCombinedResults(ArrayList<String> allLines, String bucketName) {
        try {
            File dir = new File(".");
            dir.mkdirs();
            File tmp = new File(dir, "batchResult.txt");
            boolean isCreated = tmp.createNewFile();
            if ( isCreated ) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(tmp.getPath(), true));
                System.out.println("We are here... Printing the content of all files.. ");
                while ( allLines.size() > 0) {
                    System.out.println(allLines.get( allLines.size()-1 ));
                    InputStream in = s3client.getObject( bucketName , allLines.get( allLines.size()-1) ).getObjectContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line += "\n";
                        writer.append(  line );
                    }
                    allLines.remove(allLines.size()-1);
                }
                writer.close();
                s3client.putObject(bucketName, "Results/batchResult.txt", tmp);
                return true;
            }
        } catch (Exception e ) {
            System.out.println("ERROR! " + e.toString());
        }

        return false;
    }
}

