# How to run the dev test cases

## Annotation
All of the test cases can't be run on the Windows platform because
Firefox doesn't support JSF on the Windows platform very well.
If you want to run the tests, please check out all of the code to the Linux, Ubuntu, or
Mac platform.

## Preparation and Steps:
1. Download Firefox and install it. On my platform, I have downloaded and installed
   Firefox version 19.0

2. Download the Selenium IDE plugin and install it. On my platform, I have installed
   Selenium IDE 2.4.0

3. Check out the tests from GitHub (https://github.com/LvSongping/GLASSFISH_ADMIN_CONSOLE_DEVTESTS/tree/master/auto-test)
   to your hard disk.

4. Before running the tests, you need to restart the GlassFish domain and try to access the admin console's page (http://localhost:4848/common/index.jsf)
   to make sure the GUI is available.

5. Open a terminal window and access the root directory of auto-tests. Then execute the command
   "mvn test" to run all of the tests.

6. If some of the test cases fail, you can also rerun the error or failed test cases
   using the command "mvn test -Dtest=[ClassName]#[MethodName]" to confirm related test
   cases. (If the failed test cases pass the second time, we can regard the failed test
   case as a passed case.)

## Note:
The expected test results are as follows:
- Test cases number: 110
- Passed number: 110
- Failed number: 0
- Error number: 0

