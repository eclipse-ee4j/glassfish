# Pull Request Acceptance Workflow

## One Time Setup
* [Fork](https://help.github.com/articles/fork-a-repo/) the
[repository](https://github.com/eclipse-ee4j/glassfish/).
* [Clone](https://help.github.com/articles/cloning-a-repository/)
your forked repository.
```
$ git clone https://github.com/YOUR-USERNAME/glassfish.git
```
* [Configure](https://help.github.com/articles/configuring-a-remote-for-a-fork/)
the remote for your fork.
```
$ git remote add upstream https://github.com/eclipse-ee4j/glassfish.git
$ git remote -v
origin    https://github.com/YOUR-USERNAME/glassfish.git (fetch)
origin    https://github.com/YOUR-USERNAME/glassfish.git (push)
upstream    https://github.com/eclipse-ee4j/glassfish.git (fetch)
upstream    https://github.com/eclipse-ee4j/glassfish.git (push)
```
## Raising a Pull Request
* Sync the master of your fork with upstream master.
```
$ git fetch upstream
$ git checkout master
$ git merge upstream/master
$ git push origin master # push local master to github fork.
```
* Create a local topic branch in your fork from your master.
```
$ git checkout -b issue_1234
```
* Do the development in your branch.
* Commit all the changes.
```
$ git commit -s -m "my commit message"
```
* Push your changes in a remote branch of your fork.
```
$ git push origin issue_1234
```
* Before raising a Pull Request, please raise an
[issue](https://github.com/eclipse-ee4j/glassfish/issues)
if it doesn't exist. We would like every Pull Request to be associated
with an issue. Submit the Pull Request referring to the issue number.
* Raise a [Pull Request](https://github.com/eclipse-ee4j/glassfish/pulls).
* Make sure you put a proper 'title' for the Pull Request. The title of
the Pull Request would become the commit message. Instead of giving
'title' like "Iss xxxx" or "Fixes #xxxxx", consider giving a proper one
line 'title' for the Pull Request like "Fixes xxx : <brief description
about the issue/fix>"
* In the Pull Request description (body), please mention "Fixes #xxxxx"
in order to link the Pull Request with the Issue you are fixing.
* If you have signed the [ECA](https://www.eclipse.org/legal/ECA.php),
one of the project team members will review your Pull Request.
