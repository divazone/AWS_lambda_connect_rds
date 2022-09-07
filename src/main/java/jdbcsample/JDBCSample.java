package jdbcsample;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class JDBCSample {

  public String getCurrentTime(Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Invoked JDBCSample.getCurrentTime\n");

    String currentTime = "unavailable";

    // Get time from DB server
    try {
		
		
        String secretName = "RDS_secret";
        Region region = Region.AP_NORTHEAST_1;
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);

        String secret = valueResponse.secretString();

        JsonObject convertedObject = new Gson().fromJson(secret, JsonObject.class);


        String url = "jdbc:mysql://" + convertedObject.get("host").getAsString() + ":" +
                convertedObject.get("port").getAsString() + "/" +
                convertedObject.get("dbname").getAsString();

        String username = convertedObject.get("username").getAsString();
        
        String password = convertedObject.get("password").getAsString();

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
      ResultSet resultSet = stmt.executeQuery("SELECT NOW()");

      if (resultSet.next()) {
        currentTime = resultSet.getObject(1).toString();
      }

      logger.log("Successfully executed query.  Result: " + currentTime + "\n");

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage() + "\n");
    }

    return currentTime;
  }
}
