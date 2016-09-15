package ivonaSpeechCapability;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class IvonaCredentials implements AWSCredentialsProvider{

	public IvonaCredentials(String mSecretKey, String mAccessKey) {
		super();
		this.mSecretKey = mSecretKey;
		this.mAccessKey = mAccessKey;
	}

	private String mSecretKey;
	private String mAccessKey;

	@Override
	public AWSCredentials getCredentials() {
		AWSCredentials awsCredentials = new AWSCredentials() {

			@Override
			public String getAWSSecretKey() {
				// TODO Auto-generated method stub
				return mSecretKey;
			}

			@Override
			public String getAWSAccessKeyId() {
				// TODO Auto-generated method stub
				return mAccessKey;
			};
		};
		return awsCredentials;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	} 
}