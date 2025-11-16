package com.guicedee.microprofile.config.test;

import lombok.*;
import org.eclipse.microprofile.config.inject.*;

@Getter
@Setter
public class ConfigTest
{
	@ConfigProperty(name = "test")
	private String test;
	@ConfigProperty(name = "test2")
	private String test2;
	@ConfigProperty(name = "booleanTest")
	private boolean booleanTest;
	@ConfigProperty(name = "test3")
	private long numeric1;
	@ConfigProperty(name = "test3")
	private int numeric2;
	@ConfigProperty(name = "test3")
	private Integer numeric3;
	@ConfigProperty(name = "test3")
	private Double numeric4;
	@ConfigProperty(name = "test3")
	private float numeric5;
}
