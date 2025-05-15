package serverUtils

class Validator {
    companion object {
        fun verifyArgs (size:Int, args: Map<String, String>):Boolean {
            return args.size == size
        }

    }

}