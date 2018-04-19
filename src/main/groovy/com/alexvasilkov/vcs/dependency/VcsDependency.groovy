package com.alexvasilkov.vcs.dependency

import com.alexvasilkov.vcs.VcsProperties
import com.alexvasilkov.vcs.util.CredentialsHelper
import org.gradle.api.GradleException
import org.gradle.api.initialization.ProjectDescriptor

abstract class VcsDependency {

    private static final String DEFAULT_DIR = 'libraries'
    private static final String DEFAULT_AUTH_GROUP = 'VCS'
    private static final String DEFAULT_CONFIG_NAME = 'implementation'

    final String name
    final String url
    final String path
    final File dir

    final String authGroup
    final String username, password

    final boolean includeProject
    final boolean addDependency
    final boolean keepUpdated
    final String configName

    final File repoDir

    VcsDependency(ProjectDescriptor project, Map map) {
        name = map.name
        url = map.url
        path = map.path

        File mapDir = map.dir instanceof String || map.dir instanceof File ? map.dir as File : null
        dir = (mapDir ? mapDir : getDefaultDir(project)).canonicalFile

        authGroup = map.authGroup ? map.authGroup : DEFAULT_AUTH_GROUP

        username = map.username ? map.username : CredentialsHelper.username(name, authGroup)
        password = map.password ? map.password : CredentialsHelper.password(name, authGroup)

        includeProject = map.includeProject instanceof Boolean ? map.includeProject : true
        addDependency = map.addDependency instanceof Boolean ? map.addDependency : true
        keepUpdated = map.keepUpdated instanceof Boolean ? map.keepUpdated : true
        configName = map.configName ? map.configName : DEFAULT_CONFIG_NAME

        repoDir = new File(dir, name)
    }

    void check() {
        if (!name) throw new GradleException("Vcs 'name' was not specified")
        if (!url) throw new GradleException("Vcs 'url' was not specified for ${name}")
        if (dir.exists() && !dir.isDirectory()) {
            throw new GradleException("Vcs 'dir' is not a directory '${dir.path}' for ${name}")
        }
        if (!authGroup) {
            throw new GradleException("Vcs 'authGroup' cannot be empty for ${name}")
        }
        if (!configName) {
            throw new GradleException("Vcs 'authGroup' cannot be empty for ${name}")
        }
        if (!username) {
            throw new GradleException("Vcs 'username' is not specified for '${name}'\n" +
                    "${CredentialsHelper.usernameHelp(name, authGroup)}")
        }
        if (!password) {
            throw new GradleException("Vcs 'password' is not specified for '${name}'\n" +
                    "${CredentialsHelper.passwordHelp(name, authGroup)}")
        }
    }

    File getProjectDir() {
        return repoDir
    }

    void checkEquals(VcsDependency d) {
        if (d.name != name) {
            throw new RuntimeException(
                    "Method checkEquals should be called only for dependencies with same name")
        }
        if (d.url != url) throwEqualCheckFail('url', url, d.url)
        if (!Objects.equals(d.dir, dir)) throwEqualCheckFail('dir', dir.path, d.dir.path)
        if (d.path != path) throwEqualCheckFail('path', path, s.path)
        if (d.username != username) throwEqualCheckFail('username', username, d.username)
        if (d.password != password) throwEqualCheckFail('passwords', '***', '***')
    }

    protected void throwEqualCheckFail(String paramName, Object val1, Object val2) {
        throw new GradleException("Found 2 incompatible dependencies '${name}' with different" +
                " '${paramName}' values: '${val1}' and '${val2}'")
    }

    static File getDefaultDir(ProjectDescriptor project) {
        if (VcsProperties.instance.dir) return VcsProperties.instance.dir as File

        ProjectDescriptor root = project
        while (root.parent != null) root = root.parent
        return new File(root.projectDir, DEFAULT_DIR)
    }
}
