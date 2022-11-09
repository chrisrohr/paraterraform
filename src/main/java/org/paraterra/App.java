package org.paraterra;

import static org.kiwiproject.json.JsonHelper.newDropwizardJsonHelper;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.paraterra.config.AppConfig;
import org.paraterra.dao.StateLockDao;
import org.paraterra.dao.TerraformStateDao;
import org.paraterra.resource.StateResource;
import org.paraterra.resource.TerraformBackendResource;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

public class App extends Application<AppConfig> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new MigrationsBundle<>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AppConfig configuration) {
                return configuration.getDataSourceFactory();
            }

        });
        bootstrap.addBundle(new AssetsBundle("/ui", "/", "index.html"));
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) {
        // Setting the base url pattern for the REST endpoints to /api as the UI will be on the root path
        environment.jersey().setUrlPattern("/api/*");

        configureCors(environment);

        final var factory = new JdbiFactory();
        final var jdbi = factory.build(environment, appConfig.getDataSourceFactory(), "paraterra-datasource");

        var terraformStateDao = jdbi.onDemand(TerraformStateDao.class);
        var stateLockDao = jdbi.onDemand(StateLockDao.class);

        var jsonHelper = newDropwizardJsonHelper();

        environment.jersey().register(new StateResource(terraformStateDao, jsonHelper));
        environment.jersey().register(new TerraformBackendResource(terraformStateDao, stateLockDao));
    }

    private static void configureCors(Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS params
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
