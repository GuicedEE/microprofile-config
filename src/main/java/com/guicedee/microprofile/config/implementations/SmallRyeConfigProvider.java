package com.guicedee.microprofile.config.implementations;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.guicedee.microprofile.config.MicroProfileConfigContext;
import io.smallrye.config.SmallRyeConfig;

@Singleton
public class SmallRyeConfigProvider implements Provider<SmallRyeConfig>
{
	@Override
	public SmallRyeConfig get()
	{
		return MicroProfileConfigContext.getConfig();
	}
	
}
