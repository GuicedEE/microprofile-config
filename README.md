# 🔧 GuicedEE MicroProfile Config

[![JDK](https://img.shields.io/badge/JDK-25%2B-0A7?logo=java)](https://openjdk.org/projects/jdk/25/)
[![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

GuicedEE Config is a MicroProfile Config implementation. It follows the MicroProfile Config specification for configuration lookups, CDI-based injection, type conversion, and config source ordering. Environment variables, system properties, and standard `META-INF/microprofile-config.properties` files are supported, with an optional bridge for `.env` files used in development.

Key points
- Standards-compliant: org.eclipse.microprofile.config APIs and annotations
- CDI-friendly: inject values using `@ConfigProperty` and `@Inject Config`
- Deterministic sources and priorities: Env vars, System props, and classpath `microprofile-config.properties`
- Type-safe conversion via MicroProfile `Converter<T>` SPI

## 📦 Install (Maven)
```
<dependency>
  <groupId>com.guicedee</groupId>
  <artifactId>guiced-config</artifactId>
</dependency>
```

## 🚀 Quick Start (CDI + Annotations)
```
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class MessagingService {
  @Inject
  @ConfigProperty(name = "messaging.enabled", defaultValue = "true")
  boolean enabled;

  @Inject
  @ConfigProperty(name = "messaging.bootstrap.servers")
  String bootstrapServers;

  public void start() {
    if (enabled) {
      // use bootstrapServers
    }
  }
}
```

## Programmatic Access (MicroProfile API)
```
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;

public class HealthProbe {
  @Inject
  Config config;

  public int livenessPort() {
    return config.getOptionalValue("liveness.port", Integer.class).orElse(8081);
  }
}
```

## Configuration Sources and Resolution
MicroProfile Config defines a composite of sources with numeric ordinals. Higher ordinal wins when keys overlap. Typical built-in order:
- Environment Variables (e.g., `MESSAGING_ENABLED`) — high ordinal
- System Properties (e.g., `-Dmessaging.enabled=true`)
- Classpath resources `META-INF/microprofile-config.properties`

Additional notes
- Development convenience: this module can bridge a local `.env` file into the config system; values appear as MicroProfile properties but must not be committed to VCS.
- Secrets: prefer environment variables or your CI secret manager; do not store secrets in `microprofile-config.properties`.

## Type Conversion and Custom Converters
MicroProfile Config converts common types automatically (boolean, int, Duration, etc.). You can register custom converters via SPI.

```
// Converter implementation
import org.eclipse.microprofile.config.spi.Converter;

public class MemorySizeConverter implements Converter<java.time.Duration> {
  @Override
  public java.time.Duration convert(String value) {
    // example only — parse "10s" or "500ms"
    if (value.endsWith("ms")) return java.time.Duration.ofMillis(Long.parseLong(value.replace("ms", "")));
    if (value.endsWith("s")) return java.time.Duration.ofSeconds(Long.parseLong(value.replace("s", "")));
    throw new IllegalArgumentException("Unsupported duration format: " + value);
  }
}

// Register via ServiceLoader
// file: META-INF/services/org.eclipse.microprofile.config.spi.Converter
//   your.pkg.MemorySizeConverter
```

## Defaults, Profiles, and Namespaces
- Defaults: use `@ConfigProperty(defaultValue = "...")` for safe fallbacks.
- Namespaces: group related keys with a common prefix (e.g., `messaging.` or `db.`). This keeps `microprofile-config.properties` readable.
- Profiles: if your runtime supports MicroProfile profiles (e.g., `mp.config.profile`), profile-specific files like `microprofile-config-dev.properties` can override defaults. Treat this as optional; consult GUIDES for the chosen approach in this repository.

## MicroProfile Config Files
Place one or more `microprofile-config.properties` files on the classpath under `META-INF/`:
```
# META-INF/microprofile-config.properties
messaging.enabled=true
messaging.bootstrap.servers=localhost:9092
liveness.port=8081
```

## JPMS & SPI
- JPMS-friendly; the implementation exposes MicroProfile Config services via ServiceLoader.
- If you ship custom `Converter<T>` or `ConfigSource` implementations, register them under `META-INF/services` and add JPMS `provides ... with ...` entries if your module is named.

## 📚 Docs & Rules
- Pact: `PACT.md`
- Rules: `RULES.md`
- Guides: `GUIDES.md` (see the configuration guide and secrets policy)
- Architecture: `docs/architecture/README.md`
- MicroProfile Config Spec: https://github.com/eclipse/microprofile-config

## 📝 License & Contributions
- License: Apache 2.0
- PRs welcome; keep docs updated. When adding new configuration keys, also update `.env.example`, the configuration guide, and any CI references.
