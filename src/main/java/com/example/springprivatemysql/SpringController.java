package com.example.springprivatemysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
// ADD BOUNCYCASTLE
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import javax.net.ssl.SSLContext;
// END BOUNCYCASTLE

@RestController
@EnableWebMvc
public class SpringController {

	private String getLocalDateTime()
	{
		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
		  LocalDateTime now = LocalDateTime.now();
		  return dtf.format(now);
	}
	
	//3 application.properties configurations
	@Value("${rdsSecretName}")
	private String rdsSecretName;

	@Value("${region}")
	private String region;

	@Value("${database}")
	private String database;

	@Value("${secretusername}")
	private String secretUsername;
	
	@Value("${secretpassword}")
	private String secretpassword;

	@RequestMapping(path = "/test", method = RequestMethod.GET)
	public String test() {
		return "This is a test at " + getLocalDateTime();
	}


	@RequestMapping(path = "/getsecretname", method = RequestMethod.GET)
	public String getsecretname() {
		String response = "secretname="+rdsSecretName;
		return response;
	}

	@RequestMapping(path = "/getsecret", method = RequestMethod.GET)
	public String getsecret() {
		String response = "secretname="+rdsSecretName +":";
		response += "(Secret may not render properly due to the formatting of the password):";
		try
		{
			RDSSecret rdsSecret = getSecretObject(this.rdsSecretName, this.region);
			ObjectMapper Obj = new ObjectMapper();
			String secretJson = Obj.writeValueAsString(rdsSecret);
			response += secretJson;
		}
		catch (Exception exc)
		{
			response += exc.getMessage()+":"+exc.getStackTrace();
		}
		return response;
	}
	
	@RequestMapping(path = "/getdataandinsert", method = RequestMethod.GET)
	public String getRdsData() {
		String response = "";
	
		try
		{
			RDSSecret secret = getSecretObject(this.rdsSecretName, this.region);
		
			String JDBC_PREFIX = "jdbc:mysql://"; 
			String dbEndpoint = secret.getHost();
			String portNumber = secret.getPort();
			String databasename = database;
			String username = secret.getUsername();
			String password = secret.getPassword();
			String url = JDBC_PREFIX + dbEndpoint+":"+portNumber+"/"+databasename;
			Connection connection = null;

			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(url, username, password); 

			if (connection!=null)
			{
				//Try to read from the rds database
				ResultSet rs;

				Statement statement = connection.createStatement();
				rs = statement.executeQuery("SELECT ID, DESCRIPTION, LOCATION, CREATED FROM TREASURE");
				int numberRecords = 0;
				String descriptions = "";
				while ( rs.next() ) 
				{
					numberRecords++;
					String description = rs.getString("DESCRIPTION");
					descriptions += description+",";
				}
				response = "Records before insert: "+numberRecords+ " (";
				response += descriptions+ ") ";
		    
				//Let's insert a record
				String insertStatement = " INSERT into TREASURE (DESCRIPTION, LOCATION, CREATED) values (?,?, now())";

				// create the mysql insert preparedstatement
				PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
				preparedStatement.setString (1, "LUCKY CHARM");
				preparedStatement.setString (2, "AT THE BOTTOM OF THE WELL");
				preparedStatement.execute();
		               
				response += " Successfully inserted new record as well.";
				connection.close();
			}
		}
		catch (Exception exc)
		{
			response += "Outside: "+exc.getMessage()+":"+exc.getStackTrace();
		}	    
		return "response="+response;
	}
		
	private RDSSecret getSecretObject(String secretName, String regionName)
	{
		GetSecretValueResponse getSecretValueResponse;		
		RDSSecret rdsSecret = null;		
		Region region = Region.of(regionName);

// ADD BOUNCY CASTLE		
		Security.addProvider(new BouncyCastleJsseProvider());

		try {
			SSLContext context = SSLContext.getInstance("TLS","BCJSSE");
			try {
				context.init(null, null,new SecureRandom());
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SSLContext.setDefault(context);
		} catch (NoSuchAlgorithmException | NoSuchProviderException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
// END BOUNCY CASTLE (ADD .fipsEnabled(true) BELOW)
)
		// Create a Secrets Manager client
		SecretsManagerClient client = SecretsManagerClient.builder()
// OPTIONAL: IF RUNNING WITH AWS PROFILE, IF USING INSTANCE CREDENTIALS, LEAVE COMMENTED OUT
		// .credentialsProvider(ProfileCredentialsProvider.create("PROFILE-NAME"))		
		.region(region)
		.fipsEnabled(true)
		.build();
		
		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
		.secretId(secretName)
		.build();
		
		getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
	    ObjectMapper objectMapper = new ObjectMapper();
	    try
	    {
		    rdsSecret = objectMapper.readValue(getSecretValueResponse.secretString(), RDSSecret.class);
	    }
	    catch (Exception exc)
	    {
	    	
	    }
		return rdsSecret;
	}	
	

	
}