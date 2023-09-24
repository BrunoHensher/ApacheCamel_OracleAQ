import oracle.jms.*;
import oracle.jdbc.pool.OracleDataSource;
import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.ConnectionFactory;
import java.util.Properties;
import javax.jms.Connection;
/*import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;*/
import javax.jms.JMSException;
import org.apache.camel.CamelContext;
/*import org.apache.camel.ProducerTemplate;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Message;*/
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
/*import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsConfiguration.CamelJmsTemplate;*/
import org.apache.camel.impl.DefaultCamelContext;
//import org.apache.camel.impl.SimpleRegistry;
//import org.springframework.jms.core.JmsTemplate;

/**
 * An example class for demonstrating some of the basics behind Camel. This
 * example sends some text messages on to a JMS Queue, consumes them and
 * persists them to disk
 */
public final class CamelOraJmsToFileExampleC {
    static String queueName = "jq1";
static String queueOwner =          "c##indiehacker";
static String queueOwnerPassword = "IndieHacker";
static Connection c = null;
static int numberOfMessages = 25000;
static int messageCount = 0;
static String jdbcURL = "jdbc:oracle:thin:@//localhost:1539/REESTRS";   
static Exchange exchange;
static AQjmsConnectionFactory connectionFactory;               
    private CamelOraJmsToFileExampleC() {        
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
        CamelContext context = new DefaultCamelContext();
        context.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        
		Exchange exchange = context.getEndpoint("test-jms:queue:jq1").createExchange();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                
                from("test-jms:queue:jq1")
				.process(
				   new Processor() {
                        public void process(Exchange exchange) throws Exception {
                    String body = exchange.getIn().getBody(String.class);
                    String timestamp = Long.toString(System.currentTimeMillis());
                    String newBody = timestamp + ":" + body;
                    exchange.getIn().setBody(newBody);
                }
				  }
				)
				.to("file://test");
            }
        });
        
		System.out.println("Get ready...");
       
        context.start();
		System.out.println("Context started");
        
        Thread.sleep(5000);
		
        context.stop();
       
    }
	
}
