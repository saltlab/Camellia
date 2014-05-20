Camillia
==========

Camellia assists developers in identifying the origins of DOM-based test failures (e.g. Selenium) for web applications. Through a combination of automated selective code transformation and dynamic backwards slicing, Camellia is able to link assertion failures to an application's responsible JavaScript code. The selective instrumentation is based on a static analysis algorithm and allows our tool to mantain a low runtime overhead while collecting a more concise execution trace. In addition to easing the fault localizaiton process, Camellia also helps developers understand existing test cases.

After exercising a target web application, Camellia generates an interactive  visualization that relates each test case assertion to the related asynchronous events (DOM events, timing events, etc.), executed JavaScript code, and DOM mutations.

More technical documentation to come on [wiki](https://github.com/saltlab/camellia/wiki).

## Using Camellia

Currrently, the Camellia project is designed to run from within the Eclipse IDE. Additionally, both [Mozilla Firefox](http://www.mozilla.org/en-US/firefox/new/) and [Apache Maven](http://maven.apache.org/download.cgi) will be needed to successfully run this project.

### Installation

To install, checkout the project from GitHub and import it into Eclipse as an existing Maven project (File > Import... > Maven > Existing Maven Projects). In order to do this you will need the [m2e plugin for Eclipse](http://eclipse.org/m2e/download/). This provides Maven integration for Eclipse and simplifies the handling of project dependencies. Please note that it may take a few minutes to compile Camellia after the first import.

### Configuration

Camellia has been tested with the photo gallery application [Phormer](http://p.horm.org/er/) and the [WSO2 Enterprise Store](http://wso2.com/products/enterprise-store/). These example applications contain some basic synchronous and asynchronous JavaScript events. To use Camellia with the Phormer gallery application, download Phormer and deploy it locally using a personal webserver such as [MAMP](http://www.mamp.info/en/index.html). Then, try executing the [sample test case](https://github.com/saltlab/Camellia/blob/master/src/main/java/com/clematis/selenium/SlideShowTest.java) provided for the application by running the [SimpleExample class](https://github.com/saltlab/Camellia/blob/master/src/main/java/com/clematis/core/SimpleExample.java) as the project's main class.

To better utilize Camellia with Phormer, please add a few photos to the application before running our tool. 

Camellia can be tested with the Enterprise Store in a similar manner. First, download the WSO2 application binary files from the above linked URL and then follow the provided instructions to deploy the application locally (running the appropriate script from within the (``bin``) folder).

In order to test your own web-application using Camellia, deploy the application and provide a Selenium test case for the application to Camellia in a similar fashion as shown above.

### Running the Tool 

The Jetty server must be started before running Camellia. First, navigate to the root directory of Camellia (where you checked-out Camellia) and execute the following from your command-line (Terminal, etc.):

```
mvn jetty:run
```

If successful, a notification should appear confirming that the server is up-and-running  
(``[INFO] Started Jetty Server``). Next, run the Camellia project as a Java application from Eclipse by setting [com.camellia.core.SimpleExample](https://github.com/saltlab/Camellia/blob/master/src/main/java/com/clematis/core/SimpleExample.java) as the Main class. 

Lastly, the outputted visualization can be viewed at the following address while the Jetty server is running:

```
http://localhost:8080/fish-eye-zoom-camera/view.html
```

## Contributing

Your feedback is valued! Please use the [Issue tracker](https://github.com/saltlab/camellia/issues) for reporting bugs or feature requests.

