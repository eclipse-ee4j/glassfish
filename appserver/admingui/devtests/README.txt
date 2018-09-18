To Run these tests:

    Start your server (admin port should be on port 4848)
    > .../asadmin start-domain
    > mvn test

To Run individual test: mvn -Dtest=<TEST_CLASSNAME>  test 
    > mvn -Dtest=JdbcTest test 


To Run single or multiple method within the same class: mvn -Dtest=<TEST_CLASSNAME> -Dmethod=<method-name>,<method-name>
    > mvn -Dtest=ClusterTest -Dmethod=testClusterInstancesTab,testMigrateEjbTimers

To learn how to write the devtests,  refer to this blog:
http://blogs.steeplesoft.com/2010/03/writing-selenium-tests-for-the-glassfish-admin-console/
