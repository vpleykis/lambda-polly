package com.ice2systems.voice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.VoiceId;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class Text2Voice {

	private static String bucketName = "<voice output bucket here>";
	
	private final AmazonPolly polly;
	private AmazonS3 s3;
	private Context context;
	
	public Text2Voice(final AmazonS3 s3, final AmazonPolly polly) {
		this.polly = polly;
		this.s3 = s3;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	private InputStream synthesize(String text, OutputFormat format) throws IOException {
    SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
        .withOutputFormat(format)
        .withVoiceId(VoiceId.Joanna)
        .withTextType(TextType.Ssml)
        .withText("<speak><prosody rate='fast'>" + text + "</prosody></speak>");
    
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthesizeSpeechRequest);

		return synthRes.getAudioStream();
	}
	
	public void synthesizeVoice(final String name, final String text) throws IOException {
		InputStream speechStream = synthesize(text, OutputFormat.Pcm);

		if(speechStream!=null) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int read = 0;
			byte[] bytes = new byte[1024];
	
			while ((read = speechStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			outputStream.close();
	
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(Long.valueOf(outputStream.size()));
			
			context.getLogger().log(String.format("Stream length=%d name=%s", outputStream.size(), name));
			
			s3.putObject(new PutObjectRequest(bucketName, name, new ByteArrayInputStream(outputStream.toByteArray()), metadata));
		}
		else {
			context.getLogger().log(String.format("Unable to generate voice for name=%s", name));
		}
	}
	
}
