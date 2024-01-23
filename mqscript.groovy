import com.ibm.msg.client.jms.JmsConnectionFactory
import com.ibm.msg.client.jms.JmsFactoryFactory
import com.ibm.msg.client.wmq.WMQConstants
import javax.jms.DeliveryMode
import javax.jms.Session
import javax.jms.TextMessage

log.info("### Script execution started")

def hostName = "hostName"
def hostPort = hostPort
def channelName = "channelName"
def queueManagerName = "queueManagerName"
def queueName = "queueName"

def factoryFactoryInstance = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER)
def connectionFactory = factoryFactoryInstance.createConnectionFactory()

def jmsConnection
def jmsSession
def messageProducer

try {
    connectionFactory.setStringProperty(WMQConstants.WMQ_HOST_NAME, hostName)
    connectionFactory.setIntProperty(WMQConstants.WMQ_PORT, hostPort)
    connectionFactory.setStringProperty(WMQConstants.WMQ_CHANNEL, channelName)
    connectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT)
    connectionFactory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManagerName)

    jmsConnection = connectionFactory.createConnection()
    jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    // Create the destination queue with targetClient=1
    def destinationQueue = jmsSession.createQueue("queue:///STL.OUT.CBS.GTX.SWF?targetClient=1")

    log.info("### MQ setup completed")

    messageProducer = jmsSession.createProducer(destinationQueue)
    def textMessage = jmsSession.createTextMessage()

    // Clear existing properties
    textMessage.clearProperties()

    // Set the content type explicitly to plain text
    textMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT)
    textMessage.setJMSTimestamp(System.currentTimeMillis())

    // Set your Swift message content
    textMessage.text = """{your Swift message}"""
    // Set the JMS_IBM_MQMD_FORMAT property to "MQSTR"
    textMessage.setStringProperty(WMQConstants.JMS_IBM_MQMD_FORMAT, "MQSTR")

    // Send the message
    messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
    messageProducer.send(textMessage)
    log.info("### Message sent successfully")

} catch (Exception e) {
    log.error("Exception: " + e.toString())
    e.printStackTrace()
} finally {
    messageProducer?.close()
    jmsSession?.close()
    jmsConnection?.close()
}