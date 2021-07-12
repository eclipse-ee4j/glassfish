properties = new Properties()
new File(basedir, '../../dependency/glassfish/config/branding/glassfish-version.properties').withInputStream {
  properties.load(it)
}

buildId = properties['build_id']

assert !buildId.matches('.*\\$\\{.*\\}.*') :
  "Build ID (${buildId}) contains unresolved property"

assert buildId.matches('.+-b\\p{Digit}+-g\\p{XDigit}+ [\\p{Digit}-]+T[\\p{Digit}:]+\\+\\p{Digit}+') :
  "Build ID (${buildId}) does not match expected pattern"

