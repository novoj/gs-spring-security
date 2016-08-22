package sample.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import sample.security.UserRepositoryUserDetailsService;

@Configuration
@EnableWebMvcSecurity
@ComponentScan(basePackageClasses = UserRepositoryUserDetailsService.class)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Autowired
        private PermissionEvaluator permissionEvaluator;

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            DefaultMethodSecurityExpressionHandler handler =
                    new DefaultMethodSecurityExpressionHandler();
            handler.setPermissionEvaluator(permissionEvaluator);
            return handler;
        }
    }

    @Order(1)
    @Configuration
    static class H2WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

        // @formatter:off
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .headers()
                    .cacheControl()
                    .contentTypeOptions()
                    .httpStrictTransportSecurity()
                    .and()
                .requestMatchers()
                    .antMatchers("/h2/**")
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .and()
                .authorizeRequests()
                    .anyRequest().hasRole("ADMIN");
        }
        // @formatter:on
    }

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
		final SwitchUserFilter filter = new SwitchUserFilter();
		filter.setSwitchUserUrl("/switchUser");
		filter.setUsernameParameter("username");
		filter.setExitUserUrl("/restoreUserBack");
		http
			.addFilterAfter(filter, FilterSecurityInterceptor.class)
            .authorizeRequests()
				.antMatchers("/switchUser").hasAnyAuthority("ROLE_ADMIN")
				.antMatchers("/resources/**","/signup").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }
    // @formatter:on

    // @formatter:off
    @Autowired
    public void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService);
    }
    // @formatter:on

}
