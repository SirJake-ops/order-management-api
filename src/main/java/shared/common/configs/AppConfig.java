package shared.common.configs;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.PropertySource;

@Configurable
@PropertySource("classpath:application.properties")
public class AppConfig {

}
