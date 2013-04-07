Java Connector for Coalmine
===========================

This connector allows you to easily send messages to the Coalmine API.

[Coalmine](https://www.getcoalmine.com) is a cloud-based exception and error tracking service for your web apps.

Source
------

You can always find the latest source code on [GitHub](https://github.com/coalmine/coalmine_java).

Setup
-----

### Maven

    <dependency>
        <groupId>com.coalmine</groupId>
        <artifactId>connector</artifactId>
        <!-- Check maven central for latest version. -->
        <version>0.1.0</version>
    </dependency>

### Everyone Else

Download the JAR from the build directory of this repository and add it to your project's classpath.

Configuration
-------------

### Standard Java Project

The following code is typically placed in your main method before other application initialization. This ensures that even application configuration errors are caught by Coalmine.

    Connector connector = new SimpleConnector("MY_COALMINE_SIGNATURE");
    connector.setApplicationEnvironment("Production");
    connector.setVersion("1.0.0");
    Thread.setDefaultUncaughtExceptionHandler(new CoalmineUncaughtExceptionHandler(connector));

### Java Web Application

For web applications, you can simply add a filter to your web.xml. We recommend making this filter first so that all possible errors are caught by Coalmine.

    <filter>
        <filter-name>coalmine</filter-name>
        <filter-class>com.coalmine.connector.servlet.filter.CoalmineFilter</filter-class>
        <!-- Required: The signature assigned to this application by Coalmine. -->
      <init-param>
        <param-name>signature</param-name>
        <param-value>MY_COALMINE_SIGNATURE</param-value>
      </init-param>
      <!-- Optional: The environment of the application. Defaults to "Production" -->
      <init-param>
        <param-name>environment</param-name>
        <param-value>Production</param-value>
      </init-param>
      <!-- Optional: The version of this application. Defaults to "1.0.0" -->
      <init-param>
        <param-name>version</param-name>
        <param-value>1.0.0</param-value>
      </init-param>
    </filter>
    <filter-mapping>
        <filter-name>coalmine</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

### Google App Engine

Coalmine comes ready to use on GAE for Java. Simply add the below to your web.xml. This sets up a servlet filter which will automatically log all uncaught exceptions in your application. The filter automatically detects the version and environment from the GAE version system properties. The filter also sets up a java.util.logging.Handler to listen for ERROR and WARN level log messages.

    <filter>
        <filter-name>coalmine</filter-name>
        <filter-class>com.coalmine.connector.servlet.filter.GaeCoalmineFilter</filter-class>
        <!-- Required: The signature assigned to this application by Coalmine. -->
        <init-param>
        <param-name>signature</param-name>
        <param-value>MY_COALMINE_SIGNATURE</param-value>
      </init-param>
      <!-- Optional: Whether to auto add a JUL Handler to listen for log messages. Defaults to true -->
      <init-param>
        <param-name>jul-handler</param-name>
        <param-value>true|gwt|false</param-value>
      </init-param>
    </filter>
    <filter-mapping>
        <filter-name>coalmine</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

The GWT jul-handler is a special GAE handler for normalizing Google Web Toolkit stack traces.

Configuration for Loggers
-------------------------

Coalmine can be configured to listen to your existing logging framework and automatically send notifications based on log messages.

### java.util.logging

In `logging.properties` add the Coalmine Handler. This line is comma separated, so you may add additional loggers such as `java.util.logging.ConsoleHandler`

    handlers = com.coalmine.connector.logging.CoalmineHandler
