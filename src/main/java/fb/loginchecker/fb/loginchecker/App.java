package fb.loginchecker.fb.loginchecker;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Converter;

/**
 * Hello world!
 *
 */
public class App 
{
	private static String HOSTNAME_WITH_PORT = "localhost:8080"; 
	
	static {
		System.setProperty("webdriver.chrome.driver", "E:\\development\\ChromeDriver\\chromedriver.exe");
	}
	
    public static void main( String[] args ) throws Exception
    {
    	HOSTNAME_WITH_PORT = args[0];
    	
    	if (args[1].equalsIgnoreCase("Encode") && args[2].equalsIgnoreCase("Email")) {
    		String emailSalt = getSalt("Email");
        	getEncryptedValue(args[3], emailSalt);
    	} else if (args[1].equalsIgnoreCase("Encode") && args[2].equalsIgnoreCase("Passwd")) {
    		String passSalt = getSalt("Passwd");
        	getEncryptedValue(args[3], passSalt);
    	} else {
    		
    		String encryptedEmail = args[1];
    		String encryptedPasswd = args[2];
    		
    		String emailSalt = getSalt("Email");
    		String passSalt = getSalt("Passwd");
    		
    		String email = decrypt(encryptedEmail, emailSalt);
    		String passwd = decrypt(encryptedPasswd, passSalt);
    		
	    	final WebDriver driver = new ChromeDriver(); // new HtmlUnitDriver(BrowserVersion.CHROME, true);
	    	driver.get(MessageFormat.format("http://{0}/SelfFBOperation/resources/wsoperation/appRunningStatus", HOSTNAME_WITH_PORT));
	        
	    	String pageSource = driver.findElement(By.tagName("body")).getText();
	        
	        if (Boolean.parseBoolean(pageSource)) {
	        	driver.get(MessageFormat.format("http://{0}/SelfFBOperation/Login.html", HOSTNAME_WITH_PORT));
	        	String parentWindowsHandler = driver.getWindowHandle();
	        	Thread.sleep(10000);
	        	System.out.println(driver.getWindowHandles().size());
	        	
	        	driver.getWindowHandles().forEach(k -> {
	        		driver.switchTo().window(k);
	        		if (driver.getTitle().toUpperCase().contains("FACEBOOK")) {
	        			driver.findElement(By.id("email")).sendKeys(email);
	        			driver.findElement(By.id("pass")).sendKeys(passwd);
	        			driver.findElement(By.id("offline_access")).click();
	        			driver.findElement(By.name("login")).click();
	        			driver.switchTo().window(parentWindowsHandler);
	        			return;
	        		}
	        	});
	        }
	        driver.quit();
    	}
	    
    }
    
    private static void getEncryptedValue(String data, String salt) {
    	System.out.println(encrypt(data, salt));
    }
    
    private static String getSalt(String type) {
    	final WebDriver driver = new ChromeDriver(); // new HtmlUnitDriver(BrowserVersion.CHROME, true);
    	driver.get(MessageFormat.format("http://{0}/SelfFBOperation/resources/wsoperation/saltDetail?type={1}", 
    			HOSTNAME_WITH_PORT, 
    			(type.equalsIgnoreCase("Email") ? "Email" : "Passwd")));
        
    	String salt = driver.findElement(By.tagName("body")).getText();
    	driver.quit();
    	return salt;
    }
    
    private static String encrypt(String data, String key) {
        
        try {
        	Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k =  new SecretKeySpec(key.getBytes(), "AES");
            c.init(Cipher.ENCRYPT_MODE, k);
            byte[] encryptedData = "".getBytes();
            encryptedData = c.doFinal(data.getBytes());
            byte[] encryptedByteValue = new Base64().encode(encryptedData);
            return  new String(encryptedByteValue);//.toString();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException 
                | InvalidKeyException | BadPaddingException
            | IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
        
    }
    
    private static String decrypt(String data, String key){

        byte[] encryptedData  = new Base64().decode(data);
        
        Cipher c = null;
        try {
            c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key.getBytes(), "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decrypted = null;
            decrypted = c.doFinal(encryptedData);
            return new String(decrypted);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException 
                | InvalidKeyException | BadPaddingException
            | IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
        
        
    }
    
}
