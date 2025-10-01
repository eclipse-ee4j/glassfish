# Releasing a new GlassFish version

In this example we assume 7.1.0.
If any step failed, you have to resolve the issue and start from the appropriate step again.

1. Create a release branch RELEASE_7.1.0 and push it to the Eclipse GlassFish Github repository.
2. Create a Draft PR based on this branch.
3. Wait for the validation of the PR.
4. Run the [TCKs](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/) against the result artifact - alternatively
you can deploy to Maven Central Snapshots using [glassfish-deploy-snapshots](https://ci.eclipse.org/glassfish/job/glassfish-deploy-snapshot/)
and refer zip from the build or from Maven Central Snapshots (same file).
    1. Run the [platform TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/10/job/eftl-jakartaeetck-run-100/)
    2. Run the [standalone TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/eftl-jakartaeetck-run-standalone/)
5. Open [glassfish-release](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish-release)
6. Click [Build with parameters](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish-release/build) in menu
    - `releaseVersion` = `7.1.0`
    - `nextVersion` = `7.1.1-SNAPSHOT`
    - click [Build] button
7. Wait for it to finish successfully
8. Verify that everything was done:
    1. Verify that the deployment is present in [Maven Central Deployments](https://central.sonatype.com/publishing/deployments)
       - It is possible that you will not have permissions to visit the namespace. Ask project leads or
         check if expected artifacts made it to Maven Central, then this was obviously successful.
    2. Verify that a new [7.1.0 tag](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.1.0) was created
    3. Verify that it's present in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/) (usually takes few minutes now)
9. If everything is OK, then merge the PR.
10. Delete the branch after merge, only tag will remain.
11. Upload the new release to the Eclipse download folder.
    Go to [glassfish-copy-to-downloads](https://ci.eclipse.org/glassfish/job/glassfish-copy-to-downloads)
    - Enter the version to copy; 7.1.0
    - click [Build] button
12. Create the release on Github: https://github.com/eclipse-ee4j/glassfish/releases click "Draft a new release"
13. Create the release on Eclipse: https://projects.eclipse.org/projects/ee4j.glassfish click "Create a new release"
14. Create the release on Glassfish.org. Do a PR for the **master** branch with:
    -  an update for the website in [`docs/website/src/main/resources`](https://github.com/eclipse-ee4j/glassfish/tree/master/docs/website/src/main/resources):
        - in [`download_gf7.md`](https://github.com/eclipse-ee4j/glassfish/tree/master/docs/website/src/main/resources/download_gf7.md), create a section for the new version at the top, based on the previous version. Update the info based on the release notes in github, e.g. https://github.com/eclipse-ee4j/glassfish/releases/tag/7.1.0
        - in [`download.md`](https://github.com/eclipse-ee4j/glassfish/tree/master/docs/website/src/main/resources/download.md), replace information in the "Eclipse GlassFish 7.x" section at the top with info for the new version in `download_gf7.md`
        - in [`README.md`](https://github.com/eclipse-ee4j/glassfish/blob/master/docs/website/src/main/resources/README.md), add a new piece into "Latest News", with the date of the release in Github, based on the info in `download.md`
    - with an update for the docs:
        - Update the property `glassfish.version.7x` with the released version in [docs/pom.xml](https://github.com/eclipse-ee4j/glassfish/blob/master/docs/pom.xml)
15. Create a new Eclipse GlassFish Docker Image - follow [Eclipse GlassFish Docker Image Wiki](https://github.com/eclipse-ee4j/glassfish.docker/wiki/How-To-Release)
