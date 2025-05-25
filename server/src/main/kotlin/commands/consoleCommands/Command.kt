package commands.consoleCommands

import kotlinx.serialization.Serializable
import java.nio.channels.SocketChannel



@Serializable
abstract class Command {


    abstract fun getInfo(): String
    abstract fun getArgsTypes(): Map<String, String>


    abstract fun execute(args: Map<String, String>,channel: SocketChannel,username: String)
}