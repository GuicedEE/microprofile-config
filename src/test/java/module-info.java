module com.guicedee.microprofile.config.test {
	requires transitive com.guicedee.microprofile.config;
	requires org.junit.jupiter.api;
	requires static lombok;
	
	exports com.guicedee.microprofile.config.test to com.google.guice;
	opens com.guicedee.microprofile.config.test to org.junit.platform.commons,com.google.guice;
	
}