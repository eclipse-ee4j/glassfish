Thank you for downloading Eclipse GlassFish 6.0.0!

Here are a few short steps to get you started...


0. Prerequisite
===============

Eclipse GlassFish 6.0.0 requires JDK 8.


1. Installing Eclipse GlassFish
===============================

Installing Eclipse GlassFish is just a matter of unzipping the GlassFish archive in the desired directory. Since you are reading this, you have probably already unzipped GlassFish. If not, just type the following command in the directory where you want GlassFish to be installed : jar xvf glassfish.zip


The default domain called 'domain1' is installed and preconfigured.


2. Starting GlassFish
=====================

The 'asadmin' command-line utility is used to control and manage GlassFish (start, stop, configure, deploy applications, etc).

To start GlassFish, just go in the directory where GlassFish is located and type:
        On Unix: glassfish6/glassfish/bin/asadmin start-domain
        On Windows: glassfish6\glassfish\bin\asadmin start-domain

After a few seconds, GlassFish will be up and ready to accept requests. The default 'domain1' domain is configured to listen on port 8080. In your browser, go to http://localhost:8080 to see the default landing page.

To manage GlassFish, just go to web administration console: http://localhost:4848


3. Stopping GlassFish 
=====================

To stop GlassFish, just issue the following command :
        On Unix: glassfish6/glassfish/bin/asadmin stop-domain
        On Windows: glassfish6\glassfish\bin\asadmin stop-domain


4. Where to go next?
====================

Open the following local file in your browser: glassfish6/glassfish/docs/quickstart.html. It contains useful information such as the details about the pre-configured 'domain1', links to the GlassFish Documentation, etc.

Make sure to also check the GlassFish 6.0.0 Documentation contains important information : https://glassfish.org/docs/#current

If you are new to Jakarta EE, the Jakarta EE Tutorial (https://eclipse-ee4j.github.io/jakartaee-tutorial/) is a good way to learn more. The examples are tailored to run with GlassFish and this will help you get oriented.

The Jakarta EE Examples Project also has useful code samples https://projects.eclipse.org/projects/ee4j.jakartaee-examples


5. Documentation 
================

Eclipse GlassFish Documentation : https://glassfish.org/docs/#current

Jakarta EE Information : https://jakarta.ee/


6. Follow us
============

Eclipse GlassFish is deeloped at the Eclipse Foundation see https://projects.eclipse.org/projects/ee4j.glassfish for project details

Eclipse GlassFish is developed on GitHub to view the source code or raise bugs see https://github.com/eclipse-ee4j/glassfish

Join the mailing list https://projects.eclipse.org/projects/ee4j.glassfish/contact

