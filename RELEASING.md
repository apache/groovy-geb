<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
# Required accounts and credentials

1. Generate a GPG key pair and distribute the public key as per [this blog post](http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven). Add the following entries to `~/.gradle/gradle.properties`:
	* signing.keyId=«key id»
	* signing.password=«key password»
	* signing.secretKeyRingFile=«path to the secure gpg keyring (not public)»
1. Create an account at https://central.sonatype.org/register/legacy/. Do not use Github or other social logins. Add your Sonatype credentials to `~/.gradle/gradle.properties`:
	* sonatypeOssUsername=«Jira@Sontype username»
	* sonatypeOssPassword=«Jira@Sontype password»

# Releasing (PENDING CHANGES for ASF)

1. Ensure that the revision you're about to promote has been successfully built on [CI](https://github.com/apache/groovy-geb/actions).
2. Checkout the release branch for the version you're building (e.g. `git checkout GEB_8_0_X`)
3. Update the version to the required one (usually just dropping -SNAPSHOT) in `buildSrc/src/main/groovy/geb.coordinates.gradle` file.
4. Change `{geb-version}` expression used in `History` section in `140-project.adoc` to a fixed version (the one that you're about to release).
5. Commit with message "Version «number»" (don't push yet)
6. Tag commit with name "v«number»" (still don't push yet)
7. Run `./gradlew distSrc signDistSrc`
8. If you haven't already, checkout or update a local copy of the groovy-dev subversion repository from https://dist.apache.org/repos/dist/release/groovy-dev to a local directory of your choice.
    1. For example, `svn checkout https://dist.apache.org/repos/dist/dev/groovy ~/groovy-dev` to check it out for the first time.
    2. If you have already checked it out, `cd` into that directory and update it with `svn update`.
9. Copy the artifacts from build/distributions to the appropriate subdirectory of your groovy-dev checkout (from the command above).
    1. For example: `cp build/distributions/apache-groovy-geb* ~/groovy-dev/geb/${GEB_VERSION}`
10. Update the subversion repository with the appropriate subversion commands. For example:
    1. `cd ~/work/groovy-dev`
    2. `svn add geb/${GEB_VERSION}`
    3. `svn commit --username yourusernamegoeshere@apache.org -m "New Geb version ${GEB_VERSION} added to staging area"`
11. `git push` the version branch and tag to GitHub
12. `read -s APACHE_PW` and enter your password at the prompt
13. Run `./gradlew --no-build-cache publishJarsAndManual -x :integration:geb-gradle:publishPlugins -PapacheUser=jonnybot -PapachePassword="${APACHE_PW}"`
14. Verify the staged release. Run the automated verification script from the project root:
    ```bash
    etc/bin/verify.sh dev «version» /tmp/geb-«version»-verify
    ```
    This downloads the staged artifacts, verifies checksums and GPG signatures, checks for required files, and runs the RAT license audit. The individual scripts in `etc/bin/` can also be run separately — see their header comments for details.
15. Verify the build is reproducible per [ASF Security policy](https://cwiki.apache.org/confluence/display/SECURITY/Reproducible+Builds) by running `etc/bin/test-reproducible-builds.sh`; this will run the build twice to ensure that the built outputs are identical.
16. Start the vote process on the groovy-dev mailing list. It will need at least 72 hours of remaining open and receive at least three affirmative votes from the Groovy PMC. See the [Apache Voting process](https://www.apache.org/foundation/voting.html) for more detail. Mention significant breaking changes if there are any.
17. Assuming the vote passes (at least three +1 votes from the PMC), you can take the following steps to finalize the release.
18. Email the vote thread to note that the vote has passed, with a final tally of the votes.
19. Ask a member of the PMC to copy the artifacts from the staging directory in subversion to `groovy-release/geb/${VERSION}` and commit them to subversion, as above.
20. Ask a member of the PMC to release the staging repository at https://repository.apache.org/#stagingRepositories
21. Release the Gradle plugins with `./gradlew :integration:geb-gradle:publishPlugins`

# Post-release actions
1. Bump the version to a snapshot of the next planned version.
2. Remove the oldest version from `manuals.include()` call in `site.gradle` and append the newly released one.
3. Add a placeholder above the newest version in `History` section in `140-project.adoc` using `{geb-version}` expression.
4. Commit with message 'Begin version «version»'
5. Push (make sure you push the tag as well).
6. Merge the release branch back into the master branch.
6. Bump Geb versions in example projects:
    * [geb-example-gradle](https://github.com/geb/geb-example-gradle)
    * [geb-example-maven](https://github.com/geb/geb-example-maven)
7. Update issues and milestones in GitHub tracker:
    * Find all unresolved issues in the tracker that have the fix version set to the recently released version and bulk edit them to have the fix version set to the next version.
    * Find the recently released milestone, change the version number if it's different from the one that was released and close it.
8. Wait for the build of the next version to pass and the site including manual for the released version to be published.
9. Send an email to the mailing list announcing the release. You can use [this one]() as a template.
