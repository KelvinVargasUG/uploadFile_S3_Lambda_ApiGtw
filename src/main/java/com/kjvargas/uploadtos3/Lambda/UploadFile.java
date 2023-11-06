package com.kjvargas.uploadtos3.Lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

import com.kjvargas.uploadtos3.ApiResponse.ApiResponse;
import org.apache.commons.fileupload.MultipartStream;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UploadFile implements RequestHandler<Map<String, Object>, ApiResponse> {

    @Override
    public ApiResponse handleRequest(Map<String, Object> input, Context context) {

        Regions clientRegion = Regions.DEFAULT_REGION;
        String bucketName = "nombre bucker";

        String fileObjKeyName = "";

        Map<String, String> responseBody = new HashMap<>();

        ApiResponse apiResponse = null;

        String contentType = "";

        try {
            byte[] bytes = Base64.getDecoder().decode(input.get("body").toString().getBytes());
            Map<String, String> requesHeader = (Map<String, String>) input.get("headers");
            if (requesHeader != null) {
                contentType = requesHeader.get("Content-Type");
            }

            String[] boundaryArray = contentType.split("=");
            byte[] boundary = boundaryArray[1].getBytes();
            ByteArrayInputStream content = new ByteArrayInputStream(bytes);
            MultipartStream multipartStream = new MultipartStream(content, boundary, bytes.length, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                String header = multipartStream.readHeaders();
                if (header != null) {
                    String[] headerLines = header.split("\n");
                    for (String line : headerLines) {
                        if (line.toLowerCase().contains("content-type")) {
                            int colonIndex = line.indexOf(":");
                            if (colonIndex != -1) {
                                contentType = line.substring(colonIndex + 1).trim();
                                System.out.println("Content-TypeM: " + contentType);
                            }
                        }
                    }
                }
                fileObjKeyName = getFileName(header, "filename");
                multipartStream.readBodyData(out);
                nextPart = multipartStream.readBoundary();
            }
            InputStream filesInputStream = new ByteArrayInputStream(out.toByteArray());
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(out.toByteArray().length);
            metadata.setContentType(contentType);

            s3Client.putObject(bucketName, fileObjKeyName, filesInputStream, metadata);

            responseBody.put("Message", "File uploaded successfully");

            String resBodString = new JSONObject(responseBody).toJSONString();
            apiResponse = new ApiResponse(resBodString, 200);
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            apiResponse = new ApiResponse(error.toJSONString(), 400);
        }

        return apiResponse;
    }


    private String getFileName(String str, String field) {
        String result = "";
        int index = str.indexOf(field);

        if (index >= 0) {
            int first = str.indexOf("\"", index);
            int second = str.indexOf("\"", first + 1);
            result = str.substring(first + 1, second);
        }
        return result;
    }
}
