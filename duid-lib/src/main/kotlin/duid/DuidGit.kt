package duid

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.transport.CredentialsProvider
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException


open class JGitDuidDatabaseContext(
        basedir: File? = null,
        var originUrl: String? = null,
        var originBranch: String = "master",
        var credentialsProvider: CredentialsProvider? = null,
        var transportConfigCallback: TransportConfigCallback? = null)
    : DuidDatabaseContext(basedir)

class JGitDuidDatabase(context: JGitDuidDatabaseContext) : FsDuidDatabase(context) {
    val originUrl = context.originUrl
    val originBranch = context.originBranch
    val credentialsProvider = context.credentialsProvider
    val transportConfigCallback = context.transportConfigCallback

    override fun pull() {
        try {
            Git.open(basedir).use { repo ->
                repo.fetch()
                        .setCredentialsProvider(credentialsProvider)
                        .setTransportConfigCallback(transportConfigCallback)
                        .call()
            }
        } catch (e: RepositoryNotFoundException) {
            basedir.mkdirs()
            Git.cloneRepository()
                    .setURI(originUrl)
                    .setDirectory(basedir)
                    .setBranch(originBranch)
                    .setCredentialsProvider(credentialsProvider)
                    .setTransportConfigCallback(transportConfigCallback)
                    .call()
        }
        super.pull();
    }

    override fun reset() {
        Git.open(basedir).use { repo ->
            repo.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .call()
        }
        super.reset()
    }

    private fun getHostName(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (ex: UnknownHostException) {
            "unknownHost"
        }
    }

    private fun getUserName(): String {
        return System.getProperty("user.name", "unknownUser")
    }

    override fun commit() {
        super.commit()
        Git.open(basedir).use { repo ->
            repo.add()
                    .addFilepattern(".")
                    .setUpdate(true)
                    .call()
            val status = repo.status().call()

            if (status.untracked.isNotEmpty()) {
                val addCommand = repo.add()
                for (s in status.untracked) {
                    addCommand.addFilepattern(s)
                }
                addCommand.call()
            }

            val newStatus = repo.status().call()
            if (newStatus.changed.isNotEmpty() || newStatus.added.isNotEmpty()) {
                repo.commit()
                        .setAll(true)
                        .setMessage("updated from ${getHostName()}")
                        .setCommitter(getUserName(), "grandfather@country.club")
                        .call()
            }
        }
    }

    override fun push() {
        super.push()
        Git.open(basedir).use { repo ->
            repo.push()
                    .setCredentialsProvider(credentialsProvider)
                    .setTransportConfigCallback(transportConfigCallback)
                    .setForce(false)
                    .call()
        }
    }
}

