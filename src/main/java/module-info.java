import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.microprofile.config.*;
import com.guicedee.microprofile.config.implementations.*;

module com.guicedee.microprofile.config {
	
	exports com.guicedee.microprofile.config;
	requires transitive io.smallrye.config.core;
	requires static lombok;
	requires transitive com.guicedee.vertx;

    provides com.guicedee.guicedinjection.interfaces.IGuicePreStartup with MicroProfileConfigContext;
	provides IGuiceModule with MicroProfileConfigBinder;
	provides com.google.inject.gee.InjectionPointProvider with InjectionPointProvision;
	provides IGuiceConfigurator with ScanConfig;
	
	opens com.guicedee.microprofile.config.implementations to com.google.guice;
}