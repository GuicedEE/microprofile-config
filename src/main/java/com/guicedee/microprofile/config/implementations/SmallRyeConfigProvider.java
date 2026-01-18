package com.guicedee.microprofile.config.implementations;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.guicedee.microprofile.config.MicroProfileConfigContext;
import io.smallrye.config.SmallRyeConfig;

/**
 * Guice provider that returns the shared {@link SmallRyeConfig} instance.
 */
@Singleton
public class SmallRyeConfigProvider implements Provider<SmallRyeConfig>
{
	/**
	 * Provides the configuration instance initialized by
	 * {@link MicroProfileConfigContext}.
	 *
	 * @return the current {@link SmallRyeConfig}
	 */
	@Override
	public SmallRyeConfig get()
	{
		return MicroProfileConfigContext.getConfig();
	}
	
}
