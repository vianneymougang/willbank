package com.willbank.notificationservice;

import com.willbank.notificationservice.repository.NotificationRepository;
import com.willbank.notificationservice.service.EmailService;
import com.willbank.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for Notification Service Application
 * Mocks external dependencies (MongoDB, RabbitMQ, MailTrap) to test application
 * context loading
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration"
})
class NotificationServiceApplicationTests {

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private EmailService emailService;

	@MockBean
	private NotificationService notificationService;

	@Test
	void contextLoads() {
		// Application context should load successfully with mocked external
		// dependencies
	}

}
