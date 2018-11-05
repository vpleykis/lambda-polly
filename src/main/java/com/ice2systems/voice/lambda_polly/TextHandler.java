package com.ice2systems.voice.lambda_polly;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ice2systems.voice.Text2Voice;

public class TextHandler implements RequestHandler<SNSEvent, Object> {

	private final Text2Voice t2v;
	private final AmazonS3 s3;
	private final AmazonPolly polly;
	
	public TextHandler() {
		s3 = AmazonS3ClientBuilder.standard().build();
		polly = AmazonPollyClientBuilder.standard().build();
		t2v = new Text2Voice(s3, polly);		
	}
	
	public Object handleRequest(SNSEvent event, Context context) {
		SNS sns = event.getRecords().get(0).getSNS();
		String message = sns.getMessage();
		String subject = sns.getSubject();
		
		context.getLogger().log(String.format("subj=%s", subject));
		
		try {
			t2v.setContext(context);
			t2v.synthesizeVoice(subject, message);
		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log(String.format("Error getting subject=%s from message=%s", subject, message));
		}
		
		return null;
	}

}
