# Releasing a new GlassFish version

In this example we assume 7.0.19.

1. Check beforehand that this version does NOT exist in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/main/distributions/glassfish/) else bump the version
2. Go to [GlassFish CI](https://ci.eclipse.org/glassfish/)
3. [Log in](https://ci.eclipse.org/glassfish/login?from=%2Fglassfish%2F)
4. Open [glassfish_1-build-and-stage](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/)
5. Click [Build with parameters](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage//build) in menu 
    - `RELEASE_VERSION` = `7.0.19`
    - `USE_STAGING_REPO` = `false` (uncheck)
    - click [Build] button
6. Wait for it to finish successfully
7. Drill down into this build e.g. [build 79](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/79/)
8. Click [Console Output](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/79/console) in menu
9. Ctrl+F 'orgglassfish', to find release ID, e.g. `Created staging repository with ID "orgglassfish-1230"`, remember this for `STAGING_RELEASE_ID` in a later step
   In case the release ID is not in the log (sometimes it just isn't, we don't know why), use
   go to [jsftemplating_1_build-and-stage]()https://ci.eclipse.org/glassfish/view/JSFTemplating/job/jsftemplating_1_build-and-stage/build
    - `LIST_FIRST` = `true` (check)
    - click [Build] button
   In the output, look for a line like the following:
   ```
   [INFO] orgglassfish-1352    CLOSED   org.glassfish.main:glassfish-main-aggregator:7.0.19
   ```
10. Verify that 7.0.19 is present in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/main/distributions/glassfish/)
11. Verify that a new [7.0.19](https://github.com/eclipse-ee4j/glassfish/tree/7.0.19-BRANCH) branch is created 
12. Run the TCKs against the staged build at https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/
13. Run the [platform TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/10/job/eftl-jakartaeetck-run-100/)
14. Run the [standalone TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/eftl-jakartaeetck-run-standalone/)
15. Wait for it to finish successfully
16. Open [3_staging-to-release](https://ci.eclipse.org/glassfish/job/3_glassfish-staging-to-release/)
17. Click [Build with parameters](https://ci.eclipse.org/glassfish/job/3_glassfish-staging-to-release/build) in menu
    - `STAGING_RELEASE_ID` = `orgglassfish-1352`
    - click [Build] button
18. Wait for it to finish successfully
19. Verify that it's present in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/) (might take up to a hour)
20. If everything is OK, then merge 7.0.19 branch into master via PR
21. Delete the 7.0.19 branch after merge
22. Upload the new release to the Eclipse download folder. 
    Go to [glassfish_copy-staging-to-downloads](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_copy-staging-to-downloads/build?delay=0sec)
    - Enter the version to copy; 7.0.19
    - click [Build] button 
23. Create the release on Github: https://github.com/eclipse-ee4j/glassfish/releases click "draft a new release"
24. Create the release on Eclipse: https://projects.eclipse.org/projects/ee4j.glassfish click "create a new release"
25. Create the release on Glassfish.org. Do a PR for the **master** branch with: 
    -  an update for the website in [`docs/website/src/main/resources`](https://github.com/eclipse-ee4j/glassfish/tree/master/docs/website/src/main/resources):
        - in `download_gf7.md`, create a section for the new version at the top, based on the previous version. Update the info based on the release notes in github, e.g. https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.19
        - in `download.md`, replace information in the "Eclipse GlassFish 7.x" section at the top with info for the new version in `download_gf7.md`
        - in `README.md`, add a new piece into "Latest News", with the date of the release in Github, based on the info in `download.md`
    - with an update for the docs:
        - Update the property `glassfish.version.7x` with the released version in [docs/pom.xml](https://github.com/eclipse-ee4j/glassfish/blob/master/docs/pom.xml)
