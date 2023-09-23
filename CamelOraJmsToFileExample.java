import oracle.jms.*;
import oracle.jdbc.pool.OracleDataSource;
import javax.jms.ConnectionFactory;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;


// You should provide your values for queueName, queueOwner and jdbcURL
public final class CamelOraJmsToFileExample {
    static String queueName = "myqueue";
    static String queueOwner =          "test1";
    static String queueOwnerPassword = "test1";
    static Connection c = null;
    static int numberOfMessages = 25000;
    static int messageCount = 0;
    static String jdbcURL = "jdbc:oracle:thin:@//localhost:1521/ORCL";   
    static AQjmsConnectionFactory connectionFactory;               
    private CamelOraJmsToFileExample() {        
    }
    static Connection getConnection(String jdbcUrl) throws JMSException {
           Properties prop = new Properties();
           prop.put("user", queueOwner);
           prop.put("password", queueOwnerPassword);
           connectionFactory = (AQjmsConnectionFactory) AQjmsFactory
                      .getConnectionFactory(jdbcUrl, prop);
           Connection conn = connectionFactory.createConnection();
           return conn;
    }
    public static void main(String args[]) throws Exception {
        
        c = getConnection(jdbcURL);
         // Note we can explicit name the component
		     CamelContext context = new DefaultCamelContext();
        context.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("test-jms:queue:myqueue").to("file://test");
            }
        });
        
        ProducerTemplate template = context.createProducerTemplate();
		
        // Now everything is set up - lets start the context
        context.start();
		  
		for (int i = 0; i < 10; i++) {
            template.sendBody("test-jms:queue:myqueue", "Test Message: " + i);
        }
		  

        // wait a bit and then stop
        Thread.sleep(5000);
		
        context.stop();
       
    }
	
 
}
