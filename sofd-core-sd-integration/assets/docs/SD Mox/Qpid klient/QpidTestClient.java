package dk.sd.org.broker.util.qpid.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.apache.commons.io.IOUtils;
import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.jms.Session;
import org.apache.qpid.url.URLSyntaxException;

/**
 * 
 * Class created to show one way of connecting Silkeborg Data QPID. Note that
 * this client is setup to connect to SD test environment.
 * 
 * 
 */
public class QpidTestClient {
	public static void main(String[] args) {
		System.out.println("Run with args:\n ");
		String virtualhost = "8N";
		String username = "qpid-client-test";
		String password = "qpid-client-test";
		String brokerURL = "tcp://jblinux120.jyskebank.dk:5672";
		String exchangeName = "org-struktur-changes-topic";
		
		for(int i = 0; i < args.length; i++){
			String currentArg = args[i];
			System.out.println((i+1) + ". " + currentArg);
			if(currentArg.contains("virtualhost=")){
				int index = currentArg.indexOf("=")+1;
				virtualhost = currentArg.substring(index).trim();
			}
		}
		
		System.out.println("Sending to virtualhost: " + virtualhost);
		
		System.out.println("\n\n");
		
		List<File> xmlFiles = new ArrayList<File>();
		try {
			String path = new File(".").getCanonicalPath();
			File inputFolder = new File(path + "/input");
			System.err.println("Input: " + inputFolder);
			if (inputFolder.isDirectory()) {
				File[] listFiles = inputFolder.listFiles();
				xmlFiles = findXmlFiles(listFiles);
				if (xmlFiles.size() > 0) {
					System.out.println(xmlFiles.size() + " filer sendes til message broker");
					for (int i = 0; i < xmlFiles.size(); i++) {
						System.out.println(xmlFiles.get(i));
					}
				} else {
					System.out.println("Ingen xml filer fundet");
				}
			} else {
				System.out.println("Ingen input folder fundet.");
			}
		} catch (IOException e1) {
			throw new RuntimeException("Auto generated exception", e1);
		}
		System.out.println("\n\nKlar til at sende beskeder til message broker\n\n");
		try {
			String connectionurl = "amqp://" + username + ":" + password + "@/"+virtualhost+"?brokerlist='"+ brokerURL + "'";
			System.out.println("Connectionurl: " + connectionurl + "\n");
			Connection connection = new AMQConnection(connectionurl);
			javax.jms.Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination queue = new AMQTopic(exchangeName, "#");
			MessageProducer producer = session.createProducer(queue);
			
			for(int i = 0; i < xmlFiles.size(); i++){
				BytesMessage message = session.createBytesMessage();
				message.writeBytes(getMessageBody(xmlFiles.get(i)).getBytes("UTF-8"));
				producer.send(message);
			}
			connection.close();
		} catch (AMQException  e) {
			throw new RuntimeException("Auto generated exception", e);
		} catch (JMSException e) {
			throw new RuntimeException("Auto generated exception", e);
		} catch (URLSyntaxException e) {
			throw new RuntimeException("Auto generated exception", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Auto generated exception", e);
		}
	}

	private static List<File> findXmlFiles(File[] listFiles) {
		List<File> xml = new ArrayList<File>();
		for (int i = 0; i < listFiles.length; i++) {
			String name = listFiles[i].getName();
			if (name.contains(".xml")) {
				System.out.println("Tilfojer: " + name);
				xml.add(listFiles[i]);
			} else {
				System.out.println("Ignorerer: " + name);
			}
		}
		return xml;
	}

	private static String getMessageBody(File file) {
	
		 try {
			InputStream fileInputStream = new FileInputStream(file);
			 String xml = new String(IOUtils.toByteArray(fileInputStream), "UTF-8");
			 fileInputStream.close();
			 return xml;
		} catch (IOException e) {
			throw new RuntimeException("Auto generated exception", e);
		}
	}
	}