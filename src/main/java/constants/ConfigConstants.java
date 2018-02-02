package constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigConstants {

	private static Properties prop = new Properties(); 
	
	public static String env;
	public static final String devEnv = "dev";
	public static final String prodEnv = "prod";
	
	public String ROOTURL, ROOTPATH, IMAGES_ROOTPATH;
	public int FILE_UPLOAD_MAX_SIZE_MB, LIST_SIZE_PER_PAGE, SESSION_TIMEOUT_MS;
	
	static {
		System.out.println("********** ConfigConstants start");
		
		if(System.getenv("env") != null && System.getenv("env") != ""){
			env = System.getenv("env");
		} else {
			env = "prod";
		}
		String propertiesFile = "";
		try {
			if(env.equals(devEnv)){
				propertiesFile = "dev-config.properties";
			} else {
				propertiesFile = "prod-config.properties";
			}
			System.out.println("********** " +propertiesFile);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream  input = classLoader.getResourceAsStream(propertiesFile);
			prop.load(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception");
			e.printStackTrace();
		}  
	}
	public static String get(String key){
		key = key.toLowerCase();
		return prop.getProperty(key);
	}
}
