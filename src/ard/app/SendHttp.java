package ard.app;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SendHttp {
	
	private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
	static int sendCount = 0;
	static long lastSendTimeinMills = 0l;
	
	public static void sendPost(String _msg) {
		long now = System.currentTimeMillis();
		long diff = now-lastSendTimeinMills;
		long min = TimeUnit.MILLISECONDS.toMinutes(diff);
	//	if( min > AppConstants.MAIL_DURARATION_GAP_MINUTES) {
			if(sendCount > AppConstants.MAX_NOTIFICATION_COUNT) {
				System.out.println("Max Notification count exceeded");
				return;
			}
			
	//	}

			
			 String payload = "{" +  "Message: " + _msg + "," +  "Subject: Fluid Monitoring Alert" +  "}";

        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.SEND_MAIL_API_URL))
                .POST(BodyPublishers.ofString(_msg))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "text/html")
                .build();


        HttpResponse<Void> response=null;
		try {
			 response = httpClient.send(request, BodyHandlers.discarding());
			    System.out.println(response.statusCode());
		} catch (Exception e) {
			System.out.println("Error in MSG Send..."+e.getMessage());
			e.printStackTrace();
		}

        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());
        lastSendTimeinMills = System.currentTimeMillis();
        sendCount++;

    }

 
}
