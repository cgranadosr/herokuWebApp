package com.herokuPOC.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;
import com.herokuPOC.entity.FileContainer;
import com.herokuPOC.entity.Record;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.ejb.EJB;
import org.primefaces.event.FileUploadEvent;


public class AWSStorageFacadeTemp {
    
   
   @EJB
   private RecordFacade recordFacade; 
   private FileUploadFacade fileuploadFacade;
   
   public AWSStorageFacadeTemp() {
    
  }
    
    
    
    public boolean upload(FileUploadEvent fUEvent) throws IOException {
        String fileObjKeyName = null;
        String bucketName = null;
        Regions clientRegion = null;
        AmazonS3 s3Client = null;
        
        try {
        //
        String pattern = "yyyyMMdd"; 
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern); 
        String date = simpleDateFormat. format(new Date()); 
        System. out. println(date);
           
        String fileNameIn = fUEvent.getFile().getFileName();
        InputStream in = fUEvent.getFile().getInputstream();
        File tempFile = File.createTempFile("temp", "txt");
        tempFile.deleteOnExit();
           try (FileOutputStream out = new FileOutputStream(tempFile)) {
               copyStream (in, out);
           }        
        
        // Upload a file as a new object with ContentType and title specified.
        bucketName = System.getenv("S3_BUCKET_NAME");
        System.out.println("bucketName: " + bucketName);
        clientRegion = Regions.EU_WEST_1;
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
		.withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
            System.out.println(fileNameIn);
            //fileObjKeyName = fileNameIn.replace("\\","_");
            //fileObjKeyName = fileNameIn.replace('/', '_');
            //fileObjKeyName = fileNameIn.replace(' ', '_');
            PutObjectRequest request = new PutObjectRequest(bucketName, date+"/"+fileNameIn, tempFile);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("x-amz-meta-title", "someTitle");
            request.setMetadata(metadata);
            s3Client.putObject(request);
            
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
       
       return true;
   }
   public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
   
   public List<RecordFacade> getRecordsFromFile(String date,String fileNameId) throws IOException{
       List<RecordFacade> listToDb = new ArrayList<RecordFacade>();
       
       S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        String fileObjKeyName = null;
        String bucketName = null;
        Regions clientRegion = null;
        AmazonS3 s3Client = null;
        
        bucketName = System.getenv("S3_BUCKET_NAME");
        System.out.println("bucketName: " + bucketName);
        clientRegion = Regions.EU_WEST_1;
        try {
            s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
		.withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
				
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, date+"/"+fileNameId));
            //fileuploadFacade.findFileByNameHeader(date+"/"+fileNameId, date);
            listToDb = getRecordList(fileNameId,fullObject.getObjectContent());
         } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                fullObject.close();
            }                      
        }
        return listToDb;
   }
   
   // TO DO -> this function should return an Array of File Records
    private List<RecordFacade> getRecordList(String fileName, InputStream input) throws IOException {
        List<RecordFacade> listToDb = new ArrayList<RecordFacade>();
        Record recordToDb = new Record();
        // Read the text input stream one line at a time and display each line.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            String[] arrOfStr = line.split("|", 5); 
            recordToDb = new Record();
            //
            //recordToDb.setFileContainer(fileName);
            recordToDb.setErr_type(arrOfStr[0]);
            recordToDb.setErr_msg(arrOfStr[1]);
            recordToDb.setContact_flag(arrOfStr[2]);
            recordToDb.setAccount_flag(arrOfStr[3]);
            recordToDb.setOwner_flag(arrOfStr[4]);
            recordToDb.setSfcontact_id(arrOfStr[5]);
            recordToDb.setContact_org(arrOfStr[6]);
            recordToDb.setSalutation(arrOfStr[7]);
            recordToDb.setFirstname(arrOfStr[8]);
            recordToDb.setMidname(arrOfStr[9]);
            recordToDb.setLastname(arrOfStr[10]);
        }
        
        return listToDb;
    }
}