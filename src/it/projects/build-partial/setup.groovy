import eu.zalvari.maven.changed.resources.mocks.ITHelper

def testProjectBasedir = basedir as File
def pbpBaseDir = sourceDir as String
def pbpVersion = projectVersion as String
new ITHelper(testProjectBasedir, pbpBaseDir, pbpVersion).setupTest()

return true