package commands.consoleCommands

import kotlinx.serialization.Serializable



@Serializable
abstract class Command {
    private var executionFlag = true
    fun setFlag(flag:Boolean) {
        this.executionFlag = flag
    }

    fun getExecutionFlag(): Boolean {
        return executionFlag
    }

    abstract fun getInfo(): String


    abstract fun execute(args: List<String>,username: String,token: String)
}